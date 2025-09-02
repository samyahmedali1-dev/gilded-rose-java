package com.gildedrose.Policies;


import com.gildedrose.Entity.Item;
import com.gildedrose.Helpers.Helpers;
import com.gildedrose.interfaces.UpdatePolicy;

/** "Backstage": +1 (>10), +2 (<=10), +3 (<=5); after concert -> 0; then clamp. */
public final class BackstagePolicy implements UpdatePolicy {

    @Override
    public void update(Item i) {
        // linear readable increment: 1 + (<=10 ? 1) + (<=5 ? 1)
        int inc = 1
                + (i.sellIn <= 10 ? 1 : 0)
                + (i.sellIn <= 5  ? 1 : 0);

        i.quality += inc;     // based on current sellIn
        i.sellIn  -= 1;       // a day passes

        if (i.sellIn < 0) {
            i.quality = 0;    // after the concert
        }
        Helpers.clamp0to50(i);
    }
}
