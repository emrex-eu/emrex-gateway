package eu.dc4eu.gateway.elmo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.springframework.stereotype.Component;


import eu.dc4eu.gateway.elmo.api.Elmo;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

@Component
public class ElmoTojava {

	public String transformToXml(Elmo elmo) {
		try {
			JAXBContext context = JAXBContext.newInstance(Elmo.class);
			Marshaller marshaller = context.createMarshaller();
			try (StringWriter stringWriter = new StringWriter();) {
				XMLOutputFactory xof = XMLOutputFactory.newInstance();
				CDataXMLStreamWriter cdataWriter = new CDataXMLStreamWriter(xof.createXMLStreamWriter(stringWriter));
				marshaller.marshal(elmo, cdataWriter);
				cdataWriter.flush();
				cdataWriter.close();
				return stringWriter.toString();
			}
		} catch (JAXBException | XMLStreamException | IOException e) {
			throw new RuntimeException("Could not convert representation", e);
		}
	}

	public Elmo transformFromXml(String elmoXml) {
		return unmarshal(elmoXml.getBytes());
	}

	public Elmo transformFromXmlGz64(String elmoXmlGz64) {
		byte[] decompressBytes = new byte[0];
		try {
			decompressBytes = GzipUtil.gzipDecompressBytes(Base64.getDecoder().decode(elmoXmlGz64));
		} catch (IOException e) {
			throw new RuntimeException("Could not unzip and docode", e);
		}
		return unmarshal(decompressBytes);
	}

	private Elmo unmarshal(byte[] elmoXmlDecompressBytes) {
		try {
			JAXBContext context = JAXBContext.newInstance(Elmo.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			InputStream is = new ByteArrayInputStream(elmoXmlDecompressBytes);
			Elmo elmo = (Elmo) unmarshaller.unmarshal(is);
			return elmo;
		} catch (JAXBException e) {
			throw new RuntimeException("Could not convert from  xml", e);
		}
	}

	public String toGz64(String xml) {
		try {
			byte[] compressedXml = GzipUtil.compress(xml);
			return Base64.getEncoder().encodeToString(compressedXml);
		} catch (IOException e) {
		throw new RuntimeException("Could not compress xml", e);
		}
	}

	public String fromGz64(String elmoXmlGz64) {
		try {
			return GzipUtil.gzipDecompress(Base64.getDecoder().decode(elmoXmlGz64));
		} catch (IOException e) {
			throw new RuntimeException("Could not decode xml", e);
		}
	}

	public String fromGz(String s) {
		try {
			return GzipUtil.gzipDecompress(s.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {

			throw new RuntimeException("Could not decode and unzip XML", e);
		}
	}

	public String to64(String s) {
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}
}
