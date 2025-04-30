package eu.dc4eu.gateway.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaginatedVerificationRecordsReply {
	@JsonProperty("requested_sequence_start")
	@NotBlank
	private long requestedSequenceStart;

	@JsonProperty("requested_sequence_end")
	@NotBlank
	private long requestedSequenceEnd;

	@JsonProperty("sequence_max")
	@NotBlank
	private long sequenceMax;

	@JsonProperty("verification_records")
	private List<VerificationRecord> items;
}