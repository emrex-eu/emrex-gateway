package eu.dc4eu.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 */
@Data
public class CredentialOfferReply {
	@NotBlank
	@JsonProperty("credential_issuer")
	private String credentialIssuer;

	@NotBlank
	@JsonProperty("credential_configuration_ids")
	private String credentialConfigurationIds[];

	@NotBlank
	@JsonProperty("grants")
	private CredentialOfferGrant grants;
}
