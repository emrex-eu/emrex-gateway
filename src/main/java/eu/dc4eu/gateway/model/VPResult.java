package eu.dc4eu.gateway.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class VPResult {
	@JsonProperty("vc_results")
	private List<VCResult> vcResults;
}