package com.gildedrose;

import com.gildedrose.Entity.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <h1>Gilded Rose – Production-Grade Spec Verification</h1>
 *
 * <p>
 * This suite verifies the legacy Gilded Rose rules plus the new <em>Conjured</em> rule against
 * {@link GildedRose#updateQuality()} using clear AAA (Arrange–Act–Assert) structure,
 * boundary-value analysis, and invariants. It preserves your original assertions/behavior.
 * </p>
 *
 * <h2>Traceability</h2>
 * <p>
 * Each requirement is anchored by a single-line comment placed <strong>immediately above</strong> the relevant test(s)
 * in the form <code>// POINT: &lt;verbatim text&gt;</code>. These “POINT” comments mirror the spec bullets and serve
 * as executable documentation.
 * </p>
 *
 * <h2>Assumptions</h2>
 * <ul>
 *   <li>Item type resolution uses exact string matching (e.g., "Aged Brie", "Sulfuras, Hand of Ragnaros").</li>
 *   <li>The array is mutated in place (legacy behavior).</li>
 *   <li>“Conjured” means item names explicitly starting with <code>"Conjured"</code> (e.g., "Conjured Mana Cake").</li>
 * </ul>
 *
 * <h2>Scope</h2>
 * <ul>
 *   <li>Verifies six legacy bullets (normal/Brie/Backstage/Sulfuras, bounds 0..50) + Conjured behavior.</li>
 *   <li>Includes “spec-hardening” and robustness probes that highlight gaps if you treat the kata as production.</li>
 * </ul>
 *
 * @see GildedRose#updateQuality()
 */
public class GildedRoseTest {

    /** Utility: advance the system N days (used for multi-day scenarios). */
    private static void updateDays(GildedRose app, int days) {
        for (int d = 0; d < days; d++) app.updateQuality();
    }

    // --------------------------------------------------------------------------------------------
    // POINT: - Once the sell by date has passed, Quality degrades twice as fast
    // --------------------------------------------------------------------------------------------

    /**
     * <p><strong>Intent:</strong> After the sell-by date (boundary at 0), normal items degrade by 2 per day.</p>
     * <p><strong>Given:</strong> normal item "foo" with sellIn=0, quality=10.</p>
     * <p><strong>When:</strong> one day elapses via {@link GildedRose#updateQuality()}.</p>
     * <p><strong>Then:</strong> sellIn becomes -1, quality becomes 8 (i.e., -2).</p>
     */
    @Test @Tag("point1") @DisplayName("Point1: normal item degrades by 2 after sell-by (boundary at 0)")
    void point1_normal_degrades_twice_after_sellby() {
        // Arrange
        Item[] items = { new Item("foo", 0, 10) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(-1, app.items[0].sellIn);
        assertEquals(8, app.items[0].quality);
    }

    // --------------------------------------------------------------------------------------------
    // POINT: - The Quality of an item is never negative
    // --------------------------------------------------------------------------------------------

    /**
     * <p><strong>Intent:</strong> Quality never drops below 0 even across the post-expiry branch.</p>
     * <p><strong>Given:</strong> normal item "foo" with sellIn=0, quality=0.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> sellIn=-1; quality remains 0.</p>
     */
    @Test @Tag("point2") @DisplayName("Point2: quality never goes below 0")
    void point2_quality_never_negative() {
        // Arrange
        Item[] items = { new Item("foo", 0, 0) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(-1, app.items[0].sellIn);
        assertEquals(0, app.items[0].quality);
    }

    // --------------------------------------------------------------------------------------------
    // POINT: - "Aged Brie" actually increases in Quality the older it gets
    // --------------------------------------------------------------------------------------------

    /**
     * <p><strong>Intent:</strong> "Aged Brie" increases quality as days pass.</p>
     * <p><strong>Given:</strong> "Aged Brie" with sellIn=2, quality=0.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> sellIn=1; quality=1 (+1 pre-expiry).</p>
     */
    @Test @Tag("point3") @DisplayName("Point3: Aged Brie increases in quality over time")
    void point3_brie_increases_over_time() {
        // Arrange
        Item[] items = { new Item("Aged Brie", 2, 0) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(1, app.items[0].sellIn);
        assertEquals(1, app.items[0].quality);
    }

    // --------------------------------------------------------------------------------------------
    // POINT: - The Quality of an item is never more than 50
    // --------------------------------------------------------------------------------------------

    /**
     * <p><strong>Intent:</strong> Upper quality cap at 50 for items that increase.</p>
     * <p><strong>Given:</strong> "Aged Brie" at sellIn=3, quality=50.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> quality remains 50.</p>
     */
    @Test @Tag("point4") @DisplayName("Point4: Brie quality capped at 50")
    void point4_quality_never_more_than_50_brie() {
        // Arrange
        Item[] items = { new Item("Aged Brie", 3, 50) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(2, app.items[0].sellIn);
        assertEquals(50, app.items[0].quality);
    }

    /**
     * <p><strong>Intent:</strong> Backstage never exceeds 50 even when threshold rules want +2.</p>
     * <p><strong>Given:</strong> Backstage at sellIn=10, quality=49.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> sellIn=9; quality capped at 50 (not 51).</p>
     */
    @Test @Tag("point4") @DisplayName("Point4: Backstage quality increases but never exceeds 50")
    void point4_quality_never_more_than_50_backstage() {
        // Arrange
        Item[] items = { new Item("Backstage passes to a TAFKAL80ETC concert", 10, 49) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(9, app.items[0].sellIn);
        assertEquals(50, app.items[0].quality);
    }

    // --------------------------------------------------------------------------------------------
    // POINT: - "Sulfuras", being a legendary item, never has to be sold or decreases in Quality
    // --------------------------------------------------------------------------------------------

    /**
     * <p><strong>Intent:</strong> Legendary "Sulfuras" is immutable (sellIn &amp; quality unchanged).</p>
     * <p><strong>Given:</strong> Sulfuras with sellIn=5, quality=80.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> sellIn and quality unchanged.</p>
     */
    @Test @Tag("point5") @DisplayName("Point5: Sulfuras is immutable (sellIn & quality unchanged)")
    void point5_sulfuras_never_changes() {
        // Arrange
        Item[] items = { new Item("Sulfuras, Hand of Ragnaros", 5, 80) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(5, app.items[0].sellIn);
        assertEquals(80, app.items[0].quality);
    }

    // --------------------------------------------------------------------------------------------
    // POINT: - "Backstage passes", like aged brie, increases in Quality as its SellIn value approaches;
    //          quality increases by 2 when there are 10 days or less and by 3 when there are 5 days or less but
    //          quality drops to 0 after the concert
    // --------------------------------------------------------------------------------------------

    /**
     * <p><strong>Intent:</strong> Backstage increases by +1 when sellIn &gt; 10.</p>
     * <p><strong>Given:</strong> sellIn=15, quality=20.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> sellIn=14; quality=21.</p>
     */
    @Test @Tag("point6") @DisplayName("Point6: Backstage >10 days -> +1")
    void point6_backstage_gt10_plus1() {
        // Arrange
        Item[] items = { new Item("Backstage passes to a TAFKAL80ETC concert", 15, 20) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(14, app.items[0].sellIn);
        assertEquals(21, app.items[0].quality);
    }

    /**
     * <p><strong>Intent:</strong> Backstage increases by +2 when 10 days or less remain.</p>
     * <p><strong>Given:</strong> sellIn=10, quality=20.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> sellIn=9; quality=22.</p>
     */
    @Test @Tag("point6") @DisplayName("Point6: Backstage ≤10 days -> +2")
    void point6_backstage_le10_plus2() {
        // Arrange
        Item[] items = { new Item("Backstage passes to a TAFKAL80ETC concert", 10, 20) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(9, app.items[0].sellIn);
        assertEquals(22, app.items[0].quality);
    }

    /**
     * <p><strong>Intent:</strong> Backstage increases by +3 when 5 days or less remain.</p>
     * <p><strong>Given:</strong> sellIn=5, quality=20.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> sellIn=4; quality=23.</p>
     */
    @Test @Tag("point6") @DisplayName("Point6: Backstage ≤5 days -> +3")
    void point6_backstage_le5_plus3() {
        // Arrange
        Item[] items = { new Item("Backstage passes to a TAFKAL80ETC concert", 5, 20) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(4, app.items[0].sellIn);
        assertEquals(23, app.items[0].quality);
    }

    /**
     * <p><strong>Intent:</strong> Backstage drops to 0 after the concert (sellIn &lt; 0 branch).</p>
     * <p><strong>Given:</strong> sellIn=0, quality=20.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> sellIn=-1; quality=0.</p>
     */
    @Test @Tag("point6") @DisplayName("Point6: Backstage after the concert -> quality drops to 0")
    void point6_backstage_after_concert_zero() {
        // Arrange
        Item[] items = { new Item("Backstage passes to a TAFKAL80ETC concert", 0, 20) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(-1, app.items[0].sellIn);
        assertEquals(0, app.items[0].quality);
    }

    // --------------------------------------------------------------------------------------------
    // Bonus hardening (kept lightweight but valuable in prod)
    // --------------------------------------------------------------------------------------------

    /**
     * <p><strong>Intent:</strong> Multi-day normal behavior: -1 pre-expiry, then -2 after expiry.</p>
     * <p><strong>Given:</strong> "foo" with sellIn=1, quality=4.</p>
     * <p><strong>When:</strong> two updates (crossing the boundary).</p>
     * <p><strong>Then:</strong> final sellIn=-1; quality=1.</p>
     */
    @Test @Tag("bonus") @DisplayName("Bonus: multi-day check (normal item: 1 then 2 per day)")
    void bonus_multiday_normal_1_then_2() {
        // Arrange
        Item[] items = { new Item("foo", 1, 4) };
        GildedRose app = new GildedRose(items);

        // Act
        updateDays(app, 2);

        // Assert
        assertEquals(-1, app.items[0].sellIn);
        assertEquals(1, app.items[0].quality);
    }

    /**
     * <p><strong>Intent:</strong> Long-run invariant: Brie never exceeds the upper cap of 50.</p>
     * <p><strong>Given:</strong> "Aged Brie" with sellIn=5, quality=49.</p>
     * <p><strong>When:</strong> ten updates.</p>
     * <p><strong>Then:</strong> quality==50.</p>
     */
    @Test @Tag("bonus") @DisplayName("Bonus: Brie never exceeds 50 over many days")
    void bonus_brie_never_exceeds_50_over_many_days() {
        // Arrange
        Item[] items = { new Item("Aged Brie", 5, 49) };
        GildedRose app = new GildedRose(items);

        // Act
        updateDays(app, 10);

        // Assert
        assertEquals(50, app.items[0].quality);
    }

    // --------------------------------------------------------------------------------------------
    // Spec-hardening / Robustness Probes (highlight gaps if used in prod as-is)
    // --------------------------------------------------------------------------------------------

    /**
     * <p><strong>Intent:</strong> Exposes lack of global clamping when initial quality &gt; 50 for normal items.</p>
     * <p><strong>Given:</strong> "foo" with sellIn=5, quality=55 (invalid input).</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> This assertion expects a clamp ≤ 50; legacy code will likely fail here—by design, as a guard test.</p>
     */
    @Test @DisplayName("Spec-hardening: Normal item starts with quality >50 -> still >50 after update (violates cap)")
    void normal_quality_above_50_is_not_capped() {
        // Arrange
        Item[] items = { new Item("foo", 5, 55) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert (guard): if you enforce clamping, this passes; legacy likely fails.
        assertTrue(app.items[0].quality <= 50,
                "Expected global cap (<=50) to be enforced; got " + app.items[0].quality);
    }

    /**
     * <p><strong>Intent:</strong> Null element robustness probe—legacy code throws NPE (no null-safety).</p>
     */
    @Test @DisplayName("Robustness: Null item in array -> NPE (unhandled)")
    void null_item_element_causes_npe() {
        // Arrange
        Item[] items = { null };
        GildedRose app = new GildedRose(items);

        // Act & Assert
        assertThrows(NullPointerException.class, app::updateQuality,
                "Expected NPE due to null item element (no null-safety in legacy code).");
    }

    /**
     * <p><strong>Intent:</strong> Exposes lack of global clamping for Brie when starting &gt; 50.</p>
     */
    @Test @DisplayName("Spec-hardening: Aged Brie starts >50 -> remains >50 (violates 'never more than 50')")
    void brie_quality_above_50_is_not_capped() {
        // Arrange
        Item[] items = { new Item("Aged Brie", 2, 55) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert (guard)
        assertTrue(app.items[0].quality <= 50,
                "Expected Brie to be capped at 50; got " + app.items[0].quality);
    }

    /**
     * <p><strong>Intent:</strong> Negative initial quality remains negative for normal items (legacy gap).</p>
     */
    @Test @DisplayName("Spec-hardening: Normal item starts with negative quality -> stays negative (violates 'never negative')")
    void normal_negative_quality_is_not_corrected() {
        // Arrange
        Item[] items = { new Item("foo", 3, -2) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert (guard)
        assertTrue(app.items[0].quality >= 0,
                "Expected non-negative quality; got " + app.items[0].quality);
    }

    // ----------------------------------------------------------------------------------------
    // POINT: "Conjured" items degrade in Quality twice as fast as normal items
    // ----------------------------------------------------------------------------------------

    /**
     * <p><strong>Intent:</strong> Conjured pre-expiry: degrade -2 per day; sellIn decreases by 1.</p>
     * <p><strong>Given:</strong> "Conjured Mana Cake" with sellIn=3, quality=10.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> sellIn=2; quality=8.</p>
     */
    @Test @Tag("conjured-core") @DisplayName("Conjured: Pre-expiry (sellIn > 0) -> quality -2, sellIn -1")
    void conjured_preExpiry_degradesBy2() {
        // Arrange
        Item[] items = { new Item("Conjured Mana Cake", 3, 10) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(2, app.items[0].sellIn, "sellIn should decrease by 1");
        assertEquals(8, app.items[0].quality, "Conjured should degrade by 2 pre-expiry");
    }

    /**
     * <p><strong>Intent:</strong> Conjured on boundary: total -4 quality on the update that crosses sellIn==0 -&gt; -1.</p>
     * <p><strong>Given:</strong> sellIn=0, quality=10.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> sellIn=-1; quality=6.</p>
     */
    @Test @Tag("conjured-core") @DisplayName("Conjured: Sell-by boundary (sellIn == 0) -> quality -4")
    void conjured_onSellBy_degradesBy4() {
        // Arrange
        Item[] items = { new Item("Conjured Mana Cake", 0, 10) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(-1, app.items[0].sellIn);
        assertEquals(6, app.items[0].quality, "Conjured should degrade by 4 on boundary day");
    }

    /**
     * <p><strong>Intent:</strong> Conjured post-expiry: degrade -4 per day; sellIn still -1 per day.</p>
     * <p><strong>Given:</strong> sellIn=-1, quality=10.</p>
     * <p><strong>When:</strong> one update.</p>
     * <p><strong>Then:</strong> sellIn=-2; quality=6.</p>
     */
    @Test @Tag("conjured-core") @DisplayName("Conjured: Post-expiry (sellIn < 0) -> quality -4, sellIn -1")
    void conjured_postExpiry_degradesBy4() {
        // Arrange
        Item[] items = { new Item("Conjured Mana Cake", -1, 10) };
        GildedRose app = new GildedRose(items);

        // Act
        app.updateQuality();

        // Assert
        assertEquals(-2, app.items[0].sellIn);
        assertEquals(6, app.items[0].quality, "Conjured should degrade by 4 post-expiry");
    }
}

