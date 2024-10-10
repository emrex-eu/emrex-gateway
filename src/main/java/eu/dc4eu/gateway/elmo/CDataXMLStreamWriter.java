package eu.dc4eu.gateway.elmo;

import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Implementation which is able to decide to use a CDATA section for a string.
 */
class CDataXMLStreamWriter extends DelegatingXMLStreamWriter {

	private static final String CDATA_END = "]]>";
	private static final String CDATA_BEGIN = "<![CDATA[";
	private static final Pattern XML_CHARS = Pattern.compile("[&<>]");

	CDataXMLStreamWriter(XMLStreamWriter del) {
		super(del);
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		if (text.startsWith(CDATA_BEGIN) && text.endsWith(CDATA_END)) {
			super.writeCData(text.substring(CDATA_BEGIN.length(), text.length() - CDATA_END.length()));
		} else if (XML_CHARS.matcher(text).find()) {
			super.writeCData(text);
		} else {
			super.writeCharacters(text);
		}
	}
}