package eu.dc4eu.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CredentialOfferAuthorizationCode {
	@NotBlank
	@JsonProperty("issuer_state")
	private String issuerState;
}
