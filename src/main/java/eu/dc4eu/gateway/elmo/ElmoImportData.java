package eu.dc4eu.gateway.elmo;

import java.time.LocalDateTime;

public class ElmoImportData {
	private String elmoXml; // The elmo XML
	private int id; // Id from the "REST FEED"
	private LocalDateTime timestamp; // Elmo created timestamp
	private String name; // Name of the person

	public ElmoImportData(String elmoXml, int id, LocalDateTime timestamp, String name) {
		this.elmoXml = elmoXml;
		this.id = id;
		this.timestamp = timestamp;
		this.name = name;
	}

	public String getElmoXml() {
		return elmoXml;
	}

	public int getId() {
		return id;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public String getName() {
		return name;
	}
}