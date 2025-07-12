package com.mcp.demo.server.data;

import java.util.concurrent.atomic.AtomicInteger;

public record Item(String name, int quantity, int id) {
    private static final AtomicInteger counter = new AtomicInteger(0);

    public Item {
        if (id ==0 ){
            id = counter.incrementAndGet();
        }
    }
    public Item(String name, int quantity){
        this(name,quantity,counter.incrementAndGet());
    }
}
