package eu.dc4eu.gateway.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class VCResult {
	@JsonProperty("valid_selective_disclosures")
	private List<Disclosure> validSelectiveDisclosures;

	@JsonProperty("claims")
	private Map<String, Object> claims;
}