package com.exchange.orderbook;

import com.exchange.model.Order;
import com.exchange.model.OrderSide;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.concurrent.locks.ReentrantLock;

public class OrderBook {

    private final NavigableMap<Long,PriceLevel> buyBook=
            new TreeMap<>(Comparator.reverseOrder());

    private final NavigableMap<Long,PriceLevel> sellBook=
            new TreeMap<>();

    private final ReentrantLock lock=new ReentrantLock();

    public void add(Order o){
        lock.lock();
        try{
            var book=o.side==OrderSide.BUY?buyBook:sellBook;
            book.computeIfAbsent(o.price,p->new PriceLevel()).add(o);
        }finally{
            lock.unlock();
        }
    }

    public NavigableMap<Long,PriceLevel> buy(){
        return buyBook;
    }

    public NavigableMap<Long,PriceLevel> sell(){
        return sellBook;
    }

    public ReentrantLock lock(){
        return lock;
    }
}
