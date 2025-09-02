package com.gildedrose.Helpers;

import com.gildedrose.Entity.Item;

/** Static utility helpers shared across policies. */
public final class Helpers {
    private Helpers() {} // no instances

    /** Clamp quality into [0..50]. Call at end of update for non-legendary items. */
    public static void clamp0to50(Item i) {
        if (i.quality < 0)      i.quality = 0;
        else if (i.quality > 50) i.quality = 50;
    }
}