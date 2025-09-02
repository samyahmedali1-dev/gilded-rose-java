package com.gildedrose.Policies;

import com.gildedrose.Entity.Item;
import com.gildedrose.Helpers.Helpers;
import com.gildedrose.interfaces.UpdatePolicy;

/** "Aged Brie": +1 pre-expiry, +1 post-expiry; then clamp. */
public final class BriePolicy implements UpdatePolicy {
    private final DegradingPolicy delegate = new DegradingPolicy(1, true);
    @Override public void update(Item i) { delegate.update(i); }
    }
