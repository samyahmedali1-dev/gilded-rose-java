package com.gildedrose.interfaces;

import com.gildedrose.Entity.Item;

/** Strategy: updates a single item for one business day. */
public interface UpdatePolicy {
    void update(Item item);
}