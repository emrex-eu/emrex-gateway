package eu.dc4eu.gateway.elmo;

import java.time.LocalDateTime;

import javax.xml.datatype.XMLGregorianCalendar;

import eu.dc4eu.gateway.elmo.api.Elmo;

public class ElmoImportData {
	private String elmoXml; // The elmo XML
	private long id; // Id from the "REST FEED"
	private LocalDateTime timestamp; // Elmo created timestamp
	private String name; // Name of the person

	public ElmoImportData(String elmoXml, long id, LocalDateTime timestamp, String name) {
		this.elmoXml = elmoXml;
		this.id = id;
		this.timestamp = timestamp;
		this.name = name;
	}

	public ElmoImportData(String elmoXml, Elmo elmo, long id) {
		this.elmoXml = elmoXml;
		this.id = id;
		setNameAndTimestampFromElmo(elmo);
	}

	private void setNameAndTimestampFromElmo(Elmo elmo) {
		XMLGregorianCalendar timestampGreg = elmo.getGeneratedDate();
		this.timestamp = timestampGreg.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
		this.name = elmo.getLearner().getGivenNames();
	}

	public String getElmoXml() {
		return elmoXml;
	}

	public long getId() {
		return id;
	}

	public String getFilename() {
		return String.valueOf(id) + ".xml";
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public String getName() {
		return name;
	}
}