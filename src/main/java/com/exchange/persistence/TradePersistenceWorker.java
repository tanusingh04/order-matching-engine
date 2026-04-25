package com.exchange.persistence;

import com.exchange.model.Trade;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class TradePersistenceWorker implements Runnable {

    private final TradeEventQueue queue;

    public TradePersistenceWorker(TradeEventQueue queue){
        this.queue=queue;
    }

    @PostConstruct
    public void start(){
        Thread.startVirtualThread(this);
    }

    @Override
    public void run(){
        while(true){
            try{
                Trade t=queue.take();
                // Persist to DB / File / Kafka
                System.out.println("Persisted trade "+t.price+" qty "+t.quantity);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

