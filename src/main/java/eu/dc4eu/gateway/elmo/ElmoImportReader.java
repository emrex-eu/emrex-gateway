package eu.dc4eu.gateway.elmo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.dc4eu.gateway.elmo.api.Elmo;
import jakarta.inject.Inject;

@Component
public class ElmoImportReader {

	Logger logger = LoggerFactory.getLogger(ElmoImportReader.class);

	@Value("${imported.elmo.directory}")
	private String importDirectory;

	@Inject
	private ElmoTojava elmoTojava;

	private List<File> getFilesInDirectory() {
		File directory = new File(importDirectory);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		if (!directory.isDirectory()) {
			logger.warn("Provided path is not a directory: " + importDirectory);
			return null;
		}
		File [] files = directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});

		return Arrays.stream(files).toList();
	}

	public List<ElmoImportData> importFromDir() {
		List<ElmoImportData> elmoList = new ArrayList<>();
		for (File file : getFilesInDirectory()) {
			long id = getIdFromFilename(file);

			String elmoXml = null;
			Elmo elmo = null;

			try {
				elmoXml = Files.readString(file.toPath(), StandardCharsets.UTF_8);
				elmo = elmoTojava.transformeraFrånXml(elmoXml);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (elmo != null) {
				XMLGregorianCalendar timestampGreg = elmo.getGeneratedDate();
				LocalDateTime timestamp = timestampGreg.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
				ElmoImportData elmoImportData = new ElmoImportData(elmoXml, id, timestamp, elmo.getLearner().getGivenNames());
				elmoList.add(elmoImportData);
			}
		}

		elmoList.sort(Comparator.comparingLong(ElmoImportData::getId).reversed());

		return elmoList;
	}

	public static long getMaxId(List<ElmoImportData> elmoList) {
		return elmoList.stream()
				.mapToLong(ElmoImportData::getId)
				.max()
				.orElse(0);
	}

	// file is of the form n.xml (where n is a number)
	private long getIdFromFilename(File file) {
		String fileName = file.getName();
		return Long.parseLong(fileName.substring(0, fileName.indexOf('.')));
	}
}
