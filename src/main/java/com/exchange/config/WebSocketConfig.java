package com.exchange.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import com.exchange.websocket.MarketWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MarketWebSocketHandler handler;

    public WebSocketConfig(MarketWebSocketHandler h){
        this.handler=h;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry r){
        r.addHandler(handler,"/ws/market").setAllowedOrigins("*");
    }
}
