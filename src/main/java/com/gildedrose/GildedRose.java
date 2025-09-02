package com.gildedrose;

/**
 * Gilded Rose inventory updater (legacy semantics, decomposed into small methods).
 *
 * <p>One call to {@link #updateQuality()} advances the system by one business day and mutates each
 * {@link Item} in-place following the original kata rules:
 * <ul>
 *   <li>Normal items degrade in quality by 1 per day; after the sell-by date they degrade twice as fast.</li>
 *   <li>{@code "Aged Brie"} increases in quality as it gets older (up to 50).</li>
 *   <li>{@code "Backstage passes"} increase faster as the concert approaches (+2 at ≤10 days, +3 at ≤5 days),
 *       and drop to 0 after the concert.</li>
 *   <li>{@code "Sulfuras, Hand of Ragnaros"} is legendary: never sold, never changes.</li>
 *   <li>Quality is never negative; non-legendary quality never exceeds 50.</li>
 * </ul>
 *
 */
class GildedRose {
    Item[] items;

    /**
     * Constructs the engine with the given inventory (mutated in-place by {@link #updateQuality()}).
     * @param items non-null array of non-null {@link Item} instances with non-null {@code name}.
     */
    public GildedRose(Item[] items) {
        this.items = items;
    }

    /**
     * Advances the inventory by one day. Delegates to per-type update methods while preserving
     * the legacy order of operations (pre-change → sellIn decrement → post-expiry rules).
     */
    public void updateQuality() {
        for (int i = 0; i < items.length; i++) {
            Item it = items[i];
            updateSingleItem(it);
        }
    }

    /* ================== Dispatch ================== */

    /**
     * Routes an item to the corresponding legacy branch, preserving short-circuit order.
     * <p>Legendary {@code Sulfuras} is handled first (no-op), then {@code Aged Brie}, then
     * {@code Backstage passes}, and finally the normal-items branch.</p>
     */
    private void updateSingleItem(Item it) {
        if (isSulfuras(it)) {
            return;
        }
        if (isAgedBrie(it)) {
            updateAgedBrie(it);
            return;
        }
        if (isBackstage(it)) {
            updateBackstage(it);
            return;
        }
        updateNormal(it);
    }

    /* ================== Per-type legacy branches ================== */

    /**
     * Normal items:
     * <ol>
     *   <li>Decrease quality by 1 if &gt; 0.</li>
     *   <li>Decrement sellIn by 1.</li>
     *   <li>If sellIn &lt; 0, decrease quality again by 1 if &gt; 0 (twice as fast after sell-by).</li>
     * </ol>
     * <p><em>Note:</em> Upper/lower bounds are enforced by the guarded increments/decrements
     * exactly as in the original code.</p>
     */
    private void updateNormal(Item it) {
        decreaseQualityIfAboveZero(it);
        decrementSellIn(it);
        if (it.sellIn < 0) {
            decreaseQualityIfAboveZero(it);
        }
    }

    /**
     * {@code Aged Brie}:
     * <ol>
     *   <li>Increase quality by 1 if &lt; 50.</li>
     *   <li>Decrement sellIn by 1.</li>
     *   <li>If sellIn &lt; 0, increase quality again by 1 if &lt; 50.</li>
     * </ol>
     */
    private void updateAgedBrie(Item it) {
        increaseQualityIfBelow50(it);
        decrementSellIn(it);
        if (it.sellIn < 0) {
            increaseQualityIfBelow50(it);
        }
    }

    /**
     * {@code Backstage passes}:
     * <ol>
     *   <li>Always increase quality by 1 if &lt; 50.</li>
     *   <li>If sellIn &lt; 11, increase quality by 1 if &lt; 50 (total +2).</li>
     *   <li>If sellIn &lt; 6, increase quality by 1 if &lt; 50 (total +3).</li>
     *   <li>Decrement sellIn by 1.</li>
     *   <li>If sellIn &lt; 0, set quality to 0 (after the concert).</li>
     * </ol>
     */
    private void updateBackstage(Item it) {
        increaseQualityIfBelow50(it);
        if (it.sellIn < 11) {
            increaseQualityIfBelow50(it);
        }
        if (it.sellIn < 6) {
            increaseQualityIfBelow50(it);
        }
        decrementSellIn(it);
        if (it.sellIn < 0) {
            dropQualityToZero(it);
        }
    }


    /** @return true if item is exactly {@code "Aged Brie"}. */
    private static boolean isAgedBrie(Item it) {
        return "Aged Brie".equals(it.name);
    }

    /** @return true if item is exactly {@code "Backstage passes to a TAFKAL80ETC concert"}. */
    private static boolean isBackstage(Item it) {
        return "Backstage passes to a TAFKAL80ETC concert".equals(it.name);
    }

    /** @return true if item is exactly {@code "Sulfuras, Hand of Ragnaros"}. */
    private static boolean isSulfuras(Item it) {
        return "Sulfuras, Hand of Ragnaros".equals(it.name);
    }

    /** Decrease quality by 1 only if it is &gt; 0 (matches original guards). */
    private static void decreaseQualityIfAboveZero(Item it) {
        if (it.quality > 0) {
            it.quality = it.quality - 1;
        }
    }

    /** Increase quality by 1 only if it is &lt; 50 (matches original guards). */
    private static void increaseQualityIfBelow50(Item it) {
        if (it.quality < 50) {
            it.quality = it.quality + 1;
        }
    }

    /** Decrement {@code sellIn} by 1 (already excluded for Sulfuras in the dispatcher). */
    private static void decrementSellIn(Item it) {
        it.sellIn = it.sellIn - 1;
    }

    private static void dropQualityToZero(Item it) {
        it.quality = 0;
    }
}
