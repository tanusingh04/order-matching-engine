package com.exchange.orderbook;

import com.exchange.model.Order;
import java.util.ArrayDeque;
import java.util.Queue;

public class PriceLevel {

    private final Queue<Order> orders=new ArrayDeque<>();

    public void add(Order o){
        orders.offer(o);
    }

    public Order peek(){
        return orders.peek();
    }

    public Order poll(){
        return orders.poll();
    }

    public boolean isEmpty(){
        return orders.isEmpty();
    }
}
