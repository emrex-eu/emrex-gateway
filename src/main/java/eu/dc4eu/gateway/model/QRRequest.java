package eu.dc4eu.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class QRRequest {
	@NotBlank
	@Pattern(regexp = "EHIC|ELM|PDA1")
	@JsonProperty("document_type")
	private String documentType;
}
