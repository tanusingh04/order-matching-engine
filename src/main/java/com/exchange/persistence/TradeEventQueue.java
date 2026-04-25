package com.exchange.persistence;

import com.exchange.model.Trade;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.context.annotation.Configuration;

@Configuration
public class TradeEventQueue {

    private final BlockingQueue<Trade> queue=new LinkedBlockingQueue<>();

    public void publish(Trade t){
        queue.offer(t);
    }

    public Trade take() throws InterruptedException{
        return queue.take();
    }
}
