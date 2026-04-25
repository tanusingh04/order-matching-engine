package com.exchange.model;

import java.util.concurrent.atomic.AtomicLong;

public class Order {

    public final long orderId;
    public final long userId;
    public final OrderSide side;
    public final long price;
    public final long quantity;

    private final AtomicLong remaining;

    public Order(long orderId,long userId,OrderSide side,long price,long quantity){
        this.orderId=orderId;
        this.userId=userId;
        this.side=side;
        this.price=price;
        this.quantity=quantity;
        this.remaining=new AtomicLong(quantity);
    }

    public long remaining(){
        return remaining.get();
    }

    public void deduct(long qty){
        remaining.addAndGet(-qty);
    }
}

