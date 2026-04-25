package com.exchange.model;

public class Trade {

    public final long buyOrderId;
    public final long sellOrderId;
    public final long price;
    public final long quantity;
    public final long timestamp;

    public Trade(long buy,long sell,long price,long qty){
        this.buyOrderId=buy;
        this.sellOrderId=sell;
        this.price=price;
        this.quantity=qty;
        this.timestamp=System.nanoTime();
    }
}
