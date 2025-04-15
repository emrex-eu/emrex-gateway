package eu.dc4eu.gateway.elmo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import eu.dc4eu.gateway.elmo.api.Elmo;
import jakarta.inject.Inject;

@Component
public class ElmoImportReader {

	Logger logger = LoggerFactory.getLogger(ElmoImportReader.class);

	@Value("${imported.elmo.directory}")
	private String importDirectory;

	@Inject
	private ElmoTojava elmoTojava;

	private List<String> getFilesInDirectory() {
		String pattern = "classpath:/" + importDirectory + "/*.xml";
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = null;
		try {
			resources = resolver.getResources(pattern);
		} catch (IOException e) {
			logger.warn("Could not read directory: " + importDirectory);
		}
		List<String> fileNames = new ArrayList<>();
		for (Resource resource : resources) {
			fileNames.add(resource.getFilename());
		}
		return fileNames;
/*
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		File path = new File(classloader.getResource(importDirectory).getFile());
		logger.warn("Reading directory: " + path.getAbsolutePchatgpt.ath());
		return Stream.of(path.listFiles())
					 .filter(file -> !file.isDirectory())
					 .map(File::getName)
					 .collect(Collectors.toList());

 */
	}

	public List<ElmoImportData> importFromDir() {
		List<ElmoImportData> elmoList = new ArrayList<>();
		for (String fileName : getFilesInDirectory()) {
			int id = getIdFromFilename(fileName);

			String elmoXml = null;
			Elmo elmo = null;

			try {
				ClassPathResource resource = new ClassPathResource(importDirectory + File.separator + fileName);
				elmoXml = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
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
		return elmoList;
	}

	// fileName is of the form n.xml (where n is a number)
	private int getIdFromFilename(String fileName) {
		return Integer.parseInt(fileName.substring(0, fileName.indexOf('.')));
	}
}
