package com.exchange;

import com.exchange.engine.*;
import com.exchange.model.*;
import com.exchange.orderbook.*;
import com.exchange.persistence.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

public class MatchingEngineConcurrencyTest {

    @Test
    void stressTest() throws Exception {

        OrderBook book=new OrderBook();
        TradeEventQueue q=new TradeEventQueue();
        MatchingEngine engine=new MatchingEngine(book,q);

        ExecutorService es=Executors.newFixedThreadPool(5000);

        for(long i=0;i<1000L;i++){
            long id=i;
            es.submit(() -> {
                Order o=new Order(id,id,
                        id%2==0?OrderSide.BUY:OrderSide.SELL,
                        100,10);
                engine.process(o);
            });
        }

        es.shutdown();
        es.awaitTermination(10,TimeUnit.SECONDS);
    }
}
