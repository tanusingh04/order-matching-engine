package com.exchange.engine;

import com.exchange.model.*;
import com.exchange.orderbook.*;
import com.exchange.persistence.TradeEventQueue;

public class MatchingEngine {

    private final OrderBook book;
    private final TradeEventQueue queue;

    public MatchingEngine(OrderBook book, TradeEventQueue queue){
        this.book = book;
        this.queue = queue;
    }

    public MatchResult process(Order order){
        MatchResult result = new MatchResult();
        book.lock().lock();
        try {
            if (order.side == OrderSide.BUY) {
                matchBuy(order, result);
            } else {
                matchSell(order, result);
            }
            
            // If the Taker still has shares left, it becomes a Maker
            if (order.remaining() > 0) {
                book.add(order);
            }
        } finally {
            book.lock().unlock();
        }
        return result;
    }

    private void matchBuy(Order buy, MatchResult res){
        while (buy.remaining() > 0 && !book.sell().isEmpty()) {
            var entry = book.sell().firstEntry();
            long price = entry.getKey();
            
            if (price > buy.price) break; // Crossing the spread check

            PriceLevel level = entry.getValue();
            Order sell = level.peek(); // The resting Maker

            // 1. Do the math
            execute(buy, sell, price, res);

            // 2. Manage the Queue: Kick the Maker out if they are out of shares
            if (sell.remaining() == 0) {
                level.poll(); 
            }

            // 3. Manage the Book: Delete the PriceLevel if the line is empty
            if (level.isEmpty()) {
                book.sell().remove(price);
            }
        }
    }

    private void matchSell(Order sell, MatchResult res){
        while (sell.remaining() > 0 && !book.buy().isEmpty()) {
            var entry = book.buy().firstEntry();
            long price = entry.getKey();
            
            if (price < sell.price) break; // Crossing the spread check

            PriceLevel level = entry.getValue();
            Order buy = level.peek(); // The resting Maker

            // 1. Do the math
            execute(buy, sell, price, res);

            // 2. Manage the Queue: Kick the Maker out if they are out of shares
            if (buy.remaining() == 0) {
                level.poll(); 
            }

            // 3. Manage the Book: Delete the PriceLevel if the line is empty
            if (level.isEmpty()) {
                book.buy().remove(price);
            }
        }
    }

    // The clean, refactored execute method
    private void execute(Order buy, Order sell, long price, MatchResult res){
        long qty = Math.min(buy.remaining(), sell.remaining());

        buy.deduct(qty);
        sell.deduct(qty);

        Trade t = new Trade(buy.orderId, sell.orderId, price, qty);
        res.trades.add(t);
        queue.publish(t); // Fire and forget to the background worker
    }
}