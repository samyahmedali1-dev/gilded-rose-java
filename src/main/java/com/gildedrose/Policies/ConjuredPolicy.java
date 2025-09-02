package com.gildedrose.Policies;

import com.gildedrose.Entity.Item;
import com.gildedrose.interfaces.UpdatePolicy;

final class ConjuredPolicy implements UpdatePolicy {
    private final DegradingPolicy delegate = new DegradingPolicy(2,false);
    @Override public void update(Item i) { delegate.update(i); }
}
