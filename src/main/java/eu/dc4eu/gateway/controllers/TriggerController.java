package eu.dc4eu.gateway.controllers;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/socket") // Correctly define the base path for the controller
public class TriggerController {

	private static final Logger logger = LoggerFactory.getLogger(TriggerController.class);

	private final MyWebSocketHandler handler;

	public TriggerController(MyWebSocketHandler handler) {
		this.handler = handler;
	}

	@GetMapping("/send")
	public ResponseEntity<?> sendUpdate(@RequestParam String message) throws IOException {
		logger.info("Received request to send message: {}", message);
		handler.broadcast("🔔 Server says: " + message);
		logger.info("Message broadcasted successfully.");
		return ResponseEntity.ok().build();
	}
}

