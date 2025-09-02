package com.gildedrose.Policies;


import com.gildedrose.Entity.Item;
import com.gildedrose.interfaces.UpdatePolicy;

import java.util.Objects;

public  class PolicyFactory {
    static final String SULFURAS = "Sulfuras, Hand of Ragnaros";
    static final String BRIE = "Aged Brie";
    static final String BACKSTAGE = "Backstage passes to a TAFKAL80ETC concert";

    private final UpdatePolicy normal = new NormalPolicy();
    private final UpdatePolicy conjured = new ConjuredPolicy();
    private final UpdatePolicy brie = new BriePolicy();
    private final UpdatePolicy backstage = new BackstagePolicy();
    private final UpdatePolicy sulfuras = new SulfurasPolicy();

    public UpdatePolicy createFor(Item i) {
        Objects.requireNonNull(i, "item must not be null");
        Objects.requireNonNull(i.name, "item.name must not be null");

        if (SULFURAS.equals(i.name)) return sulfuras;
        if (BRIE.equals(i.name)) return brie;
        if (BACKSTAGE.equals(i.name)) return backstage;
        if (i.name.startsWith("Conjured")) return conjured;
        return normal;
    }
}
