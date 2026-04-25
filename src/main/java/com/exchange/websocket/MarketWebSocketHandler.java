package com.exchange.websocket;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class MarketWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArrayList<WebSocketSession> sessions=new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession s){
        sessions.add(s);
    }

    public void broadcast(String msg){
        sessions.forEach(s->{
            try{
                s.sendMessage(new TextMessage(msg));
            }catch(Exception ignored){}
        });
    }
}

