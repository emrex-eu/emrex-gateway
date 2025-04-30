package eu.dc4eu.gateway.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificationRecord {
	@JsonProperty("sequence")
	@NotBlank
	private long sequence;

	@JsonProperty("id")
	@NotBlank
	private String id;

	@JsonProperty("verification_meta")
	@NotBlank
	private VerificationMeta verificationMeta;

	@JsonProperty("vp_results")
	private List<VPResult> vpResults;
}