package com.gildedrose.Policies;

import com.gildedrose.Entity.Item;
import com.gildedrose.interfaces.UpdatePolicy;
/** "Sulfuras": legendary (no changes). No clamping to preserve 80. */
public final class SulfurasPolicy implements UpdatePolicy {
    @Override public void update(Item i) {
        i.quality = 80;
    }}


