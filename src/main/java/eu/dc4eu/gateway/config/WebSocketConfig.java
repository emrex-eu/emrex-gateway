package eu.dc4eu.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import eu.dc4eu.gateway.controllers.MyWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {



	private final MyWebSocketHandler myWebSocketHandler;

	public WebSocketConfig(MyWebSocketHandler myWebSocketHandler) {
		this.myWebSocketHandler = myWebSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry
				.addHandler(myWebSocketHandler, "/ws/updates")
				.setAllowedOrigins("*"); // Or specify your frontend origin
	}
}

