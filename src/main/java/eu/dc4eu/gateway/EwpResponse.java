package eu.dc4eu.gateway;


import lombok.Data;

@Data
public class EwpResponse {
	private String sessionId;
	private String returnCode;
	private String returnMessage;
	private String elmo;
}
