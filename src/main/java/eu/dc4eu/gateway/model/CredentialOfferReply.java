package eu.dc4eu.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * {"credential_issuer":"https://vc-interop-3.sunet.se","credential_configuration_ids":[""],"grants":{"authorization_code":{"issuer_state":"collect_id=05095cbb-b516-4324-ba35-232c5db66e36\u0026document_type=ELM\u0026authentic_source=LADOK"}}}
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
