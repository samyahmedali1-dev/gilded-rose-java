package com.gildedrose;

import com.gildedrose.Entity.Item;
import com.gildedrose.Policies.PolicyFactory;

/**
 * Engine using Strategy + Factory.
 * Delegates per-item logic to the factory-picked policy; no business logic inside.
 */
class GildedRose {
    final Item[] items;
    private final PolicyFactory factory;

    GildedRose(Item[] items) {
        this(items, new PolicyFactory());
    }

    GildedRose(Item[] items, PolicyFactory factory) {
        this.items = items;
        this.factory = factory;
    }

    public void updateQuality() {
        for (Item item : items) {
            factory.createFor(item).update(item);
        }
    }
}
