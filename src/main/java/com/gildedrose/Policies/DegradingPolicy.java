package com.gildedrose.Policies;

import com.gildedrose.Entity.Item;
import com.gildedrose.Helpers.Helpers;
import com.gildedrose.interfaces.*;

/**
 * Generic degrading policy with one step:
 *  - Normal:   step = 1
 *  - Conjured: step = 2
 *
 * Flow:
 *  1) degrade once by step (if quality > 0)
 *  2) sellIn--
 *  3) if expired (sellIn < 0): degrade once more by step (if quality > 0)
 *  4) clamp via Helpers.clamp0to50
 */
final class DegradingPolicy implements UpdatePolicy {
    private final int magnitude;   // 1 = normal | 2 = conjured
    private final boolean growth;  // false = decay, true = growth (Brie)

    DegradingPolicy(int magnitude, boolean growth) {
        this.magnitude = magnitude;
        this.growth = growth;
    }

    @Override
    public void update(Item i) {
        applyOnce(i, magnitude, growth); // pre
        i.sellIn -= 1;                   // day passes
        if (i.sellIn < 0) {
            applyOnce(i, magnitude, growth); // post (twice after sell-by)
        }
        Helpers.clamp0to50(i);
    }

    private static void applyOnce(Item i, int by, boolean growth) {
        if (growth) {
            if (i.quality < 50) i.quality += by;   // increase with guard
        } else {
            if (i.quality > 0) i.quality -= by;   // decrease with guard
        }
    }
}
