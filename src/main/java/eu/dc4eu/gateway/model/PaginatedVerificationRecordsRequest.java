package eu.dc4eu.gateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaginatedVerificationRecordsRequest {
	@JsonProperty("requested_sequence_start")
	@NotBlank
	private long requestedSequenceStart;

	@JsonProperty("requested_sequence_end")
	@NotBlank
	private long requestedSequenceEnd;
}