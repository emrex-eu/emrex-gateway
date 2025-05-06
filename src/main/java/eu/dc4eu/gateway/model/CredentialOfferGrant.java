package eu.dc4eu.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CredentialOfferGrant {
	@NotBlank
	@JsonProperty("authorization_code")
	private CredentialOfferAuthorizationCode authorizationCode;
}
