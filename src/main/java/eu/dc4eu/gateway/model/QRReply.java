package eu.dc4eu.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QRReply {
	@NotBlank
	@JsonProperty("base64_image")
	private String base64Image;

	@NotBlank
	private String uri;

	@NotBlank
	private String requestUri;

	@NotBlank
	@JsonProperty("client_id")
	private String clientId;

	// Only for dev/test
	@NotBlank
	@JsonProperty("session_id")
	private String sessionId;
}
