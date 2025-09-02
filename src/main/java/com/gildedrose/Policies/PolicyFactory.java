package com.gildedrose.Policies;

import com.gildedrose.Entity.Item;
import com.gildedrose.interfaces.UpdatePolicy;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

/**
 * PolicyFactory with fuzzy matching (Apache Commons Text - Jaro–Winkler).
 * Order:
 *  1) Exact matches (fast path)
 *  2) "Conjured" prefix (exact/fuzzy)
 *  3) Fuzzy matches for canonical names
 *  4) Fallback: Normal
 */
public final class PolicyFactory {
    // Canonical names (as per kata)
    public static final String SULFURAS  = "Sulfuras, Hand of Ragnaros";
    public static final String BRIE      = "Aged Brie";
    public static final String BACKSTAGE = "Backstage passes to a TAFKAL80ETC concert";
    public static final String CONJURED  = "Conjured"; // prefix semantic

    // Similarity thresholds (tune if you see false positives/negatives)
    private static final double T_STRICT   = 0.94; // Brie / Backstage / Sulfuras
    private static final double T_CONJURED = 0.90; // "Conjured" (prefix-ish)

    // Reusable strategies (stateless)
    private final UpdatePolicy normal    = new NormalPolicy();
    private final UpdatePolicy conjured  = new ConjuredPolicy();
    private final UpdatePolicy brie      = new BriePolicy();
    private final UpdatePolicy backstage = new BackstagePolicy();
    private final UpdatePolicy sulfuras  = new SulfurasPolicy();

    private static final JaroWinklerDistance JW = new JaroWinklerDistance();

    public UpdatePolicy createFor(Item i) {
        Objects.requireNonNull(i, "item must not be null");
        Objects.requireNonNull(i.name, "item.name must not be null");

        // 0) Exact matches first (fast path)
        if (SULFURAS.equals(i.name))  return sulfuras;
        if (BRIE.equals(i.name))      return brie;
        if (BACKSTAGE.equals(i.name)) return backstage;
        if (i.name.startsWith(CONJURED)) return conjured;

        // Normalize once
        final String normName = normalize(i.name);
        final String normConj = normalize(CONJURED);

        // 1) Conjured (prefix or fuzzy)
        if (normName.startsWith(normConj) || sim(normName, normConj) >= T_CONJURED) {
            return conjured;
        }

        // 2) Fuzzy canonical names (strict)
        if (sim(i.name, SULFURAS) >= T_STRICT)  return sulfuras;
        if (sim(i.name, BRIE)     >= T_STRICT)  return brie;
        if (sim(i.name, BACKSTAGE)>= T_STRICT)  return backstage;

        // 3) Fallback
        return normal;
    }

    /* -------------------- helpers -------------------- */

    /** Jaro–Winkler similarity in [0..1], 1.0 = identical. */
    private static double sim(String a, String b) {
        String na = normalize(a);
        String nb = normalize(b);
        Double dist = JW.apply(na, nb);            // distance in [0..1], 0.0 = identical
        double d = (dist == null) ? 1.0 : dist;
        return 1.0 - d;                            // convert to similarity
    }

    /** Normalize: lowercase, trim, remove diacritics/punctuations, collapse spaces. */
    private static String normalize(String s) {
        if (s == null) return "";
        String lower = s.toLowerCase(Locale.ROOT).trim();
        String noDia = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noDia.replaceAll("[^\\p{Alnum}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
