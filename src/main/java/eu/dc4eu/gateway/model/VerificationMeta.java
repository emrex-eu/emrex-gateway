package eu.dc4eu.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificationMeta {
	@JsonProperty("verification_result")
	@NotBlank
	private String verificationResult;

	@JsonProperty("verified_at_unix")
	@NotBlank
	private long verifiedAtUnix;

	@JsonProperty("error")
	private String error;

	@JsonProperty("error_description")
	private String errorDescription;

	@JsonProperty("error_uri")
	private String errorUri;
}
