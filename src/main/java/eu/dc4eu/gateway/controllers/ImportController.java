package eu.dc4eu.gateway.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dc4eu.gateway.elmo.ElmoImportData;
import eu.dc4eu.gateway.elmo.ElmoImportRW;
import jakarta.inject.Inject;

@Controller
@RequestMapping("/import")
public class ImportController {

	@Value("${imported.elmo.directory}")
	private String importDirectory;

	@Inject
	private ElmoImportRW elmoImportRW;

	Logger logger = LoggerFactory.getLogger(ImportController.class);

	@GetMapping(value = "/readDir")
	public String onReturn(Model model) throws IOException {
		logger.info("Reading directory...");

		List<ElmoImportData> elmoList = elmoImportRW.importFromDir();

		logger.warn("Got elmoList size=" + elmoList.size());


		model.addAttribute("elmos", elmoList);

		return "import_directory";
	}

	@GetMapping(value="/download/{id}")
	@ResponseBody
	public ResponseEntity<Resource> downloadFile(@PathVariable("id") Long id) throws IOException {
		logger.info("Downloading file with id: " + id);

		File file = new File(importDirectory + File.separator + id + ".xml");
        if (!file.exists()) {
			throw new RuntimeException("File not found: " + file.getName());
        }

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");

		return ResponseEntity.ok()
							 .headers(headers)
							 .contentLength(file.length())
							 .contentType(MediaType.APPLICATION_XML)
							 .body(new InputStreamResource(Files.newInputStream(file.toPath())));
	}
}
