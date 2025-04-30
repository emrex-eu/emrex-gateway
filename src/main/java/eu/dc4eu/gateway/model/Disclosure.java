package eu.dc4eu.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Disclosure {
	@JsonProperty("salt")
	@NotBlank
	private String salt;

	@JsonProperty("key")
	@NotBlank
	private String key;

	@JsonProperty("value")
	@NotBlank
	private Object value;
}