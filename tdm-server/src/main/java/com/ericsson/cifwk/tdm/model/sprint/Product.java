package com.ericsson.cifwk.tdm.model.sprint;

import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;

public class Product {

    private static final String PRODUCT_NAME = "ENM";

    private final String name;

    private List<String> drops = newArrayList();

    @SuppressWarnings("unused")
    public Product() {
        this(newArrayList());
    }

    public Product(List<String> drops) {
        this.name = PRODUCT_NAME;
        this.drops = drops;
    }

    public String getName() {
        return name;
    }

    public List<String> getDrops() {
        return drops;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("name", name)
                .add("drops", drops)
                .toString();
    }
}
