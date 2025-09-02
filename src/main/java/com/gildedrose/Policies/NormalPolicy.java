package com.gildedrose.Policies;

import com.gildedrose.Entity.Item;
import com.gildedrose.interfaces.UpdatePolicy;

/** Normal items: degrade step=1 pre-expiry and again post-expiry; then clamp. */
public final class NormalPolicy implements UpdatePolicy {
    private final DegradingPolicy delegate = new DegradingPolicy(1, false);
    @Override public void update(Item i) { delegate.update(i); }


}
