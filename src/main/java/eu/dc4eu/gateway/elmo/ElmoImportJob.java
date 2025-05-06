package eu.dc4eu.gateway.elmo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import eu.dc4eu.gateway.ConverterService;
import eu.dc4eu.gateway.converter.Response;
import eu.dc4eu.gateway.elmo.api.Elmo;
import eu.dc4eu.gateway.model.Disclosure;
import eu.dc4eu.gateway.model.PaginatedVerificationRecordsReply;
import eu.dc4eu.gateway.model.PaginatedVerificationRecordsRequest;
import jakarta.inject.Inject;

@Component
public class ElmoImportJob {

	private static final Logger logger = LoggerFactory.getLogger(ElmoImportJob.class);
	private static long currentMaxIndex = -1;
	private static final boolean FAKE_ELM=true; // TODO: Remove this when real ELM keys are avail

	@Inject
	ElmoImportRW elmoImportRW;

	@Inject
	ConverterService converterService;

	@Inject
	ElmoTojava elmoTojava;

	public void importElmoFiles() {
		logger.info("Running scheduled Elmo import job...");
		// Check already imported files
		List<ElmoImportData> existingImportData = elmoImportRW.importFromDir();
		long existingMaxId = ElmoImportRW.getMaxId(existingImportData);


		logger.info("Existing max ID: " + existingMaxId);

		long currentId = retrieveCurrentIdFromFeed();

		logger.info("Current ID from feed: " + currentId);

		// Fake elm import (only once)
		if (FAKE_ELM && currentMaxIndex == -1) {
			existingMaxId = 0;
			currentId = 1;
			logger.info("Faking existingMaxId="+existingMaxId+" and currentId="+currentId);
		}

		// Update currentMaxIndex with max id from files
		if (currentMaxIndex == -1) {
			currentMaxIndex = existingMaxId;
		}

		if (currentMaxIndex >= currentId) {
			logger.info("No new Elmo files to import.");
			return;
		}
		logger.info("New Elmo files found. Importing...");

		for (long i=currentMaxIndex+1; i<=currentId; i++) {
			ElmoImportData importedData = retrieveFeed(i);

			// There might be holes in the feed, so we need to check if the data is null
			if (importedData != null) {
				saveImportedData(importedData);
			}
		}
		currentMaxIndex = currentId;
	}

	private void saveImportedData(ElmoImportData importedData) {
		elmoImportRW.writeToDir(importedData);
	}

	private ElmoImportData retrieveFeed(long i) {
		List<Disclosure> disclosures = new ArrayList<>();
		if (FAKE_ELM) {
			Disclosure disclosure = new Disclosure();
			disclosure.setKey("elm");

			logger.info("Converting sample Elmo to ELM");
			ClassPathResource resource = new ClassPathResource("sampleElmo.b64");
			try {
				String elmoB64 = Files.readString(Path.of(resource.getURI()));
				Response response = converterService.convertElmoToElm(elmoB64);
				if (response == null) {
					logger.error("Error converting Elmo to Elm (response is empty)");
					return null;
				}
				logger.info("Setting elm value");
				disclosure.setValue(response.getContent());
			} catch (IOException ioe) {
				logger.error("Error reading sample ELM file: " + ioe.getMessage());
				return null;
			}
			disclosures.add(disclosure);
		} else {
			disclosures = retrieveFeedFromREST(i);
		}

		for (Disclosure disclosure : disclosures) {
			if (disclosure.getKey().equals("elm")) {
				logger.info("Found ELM key - converting to elmo");
				return parseOutElmoImportData((String) disclosure.getValue(), i);
			}
			logger.info("Found disclosure key: " + disclosure.getKey());
		}
		logger.info("No disclosure with key elm found");

		return null;
	}

	private PaginatedVerificationRecordsReply callREST(PaginatedVerificationRecordsRequest request) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<PaginatedVerificationRecordsRequest> entity = new HttpEntity<>(request, headers);
		// TODO: hardcoded URL
		String url="https://vc-interop-3.sunet.se:444/vp-datastore/verification-records";
		ResponseEntity<PaginatedVerificationRecordsReply> responseEntity = restTemplate.exchange(
				url, HttpMethod.POST, entity, PaginatedVerificationRecordsReply.class);

		return responseEntity.getBody();
	}

	private long retrieveCurrentIdFromFeed() {
		PaginatedVerificationRecordsRequest request = new PaginatedVerificationRecordsRequest();
		request.setRequestedSequenceStart(1);
		request.setRequestedSequenceEnd(1);
		PaginatedVerificationRecordsReply reply = callREST(request);

		if (reply != null) {
			return reply.getSequenceMax();
		} else {
			return -1;
		}
	}

	private ElmoImportData parseOutElmoImportData(String elmB64, long id) {
		logger.info("Converting elm to elmo");
		Response response = converterService.convertElmToElmo(elmB64);
		if (response == null) {
			logger.error("Error converting Elm to Elmo (response is empty)");
			return null;
		}
		String contentB64 = response.getContent();
		if (contentB64 == null) {
			logger.error("Error converting Elm to Elmo: " + response.getErrors().get(0).getMessage());
			return null;
		}
		byte[] contentBytes = java.util.Base64.getDecoder().decode(contentB64);
		String elmoXml = new String(contentBytes, java.nio.charset.StandardCharsets.UTF_8);
		Elmo elmo = elmoTojava.transformFromXml(elmoXml);
		if (elmo == null) {
			logger.error("Error parseing elmo: ");
			return null;
		}
		logger.info("Parsed elmo with name: " + elmo.getLearner().getGivenNames());
		return new ElmoImportData(elmoXml, elmo, id);
	}

	// Only pick first vp_result and first vc_result
	private List<Disclosure> retrieveFeedFromREST(long i) {

		PaginatedVerificationRecordsRequest request = new PaginatedVerificationRecordsRequest();
		request.setRequestedSequenceStart(i);
		request.setRequestedSequenceEnd(i);
		PaginatedVerificationRecordsReply reply = callREST(request);

		if (reply != null) {
			return reply.getItems().get(0).getVpResults().get(0).getVcResults().get(0).getValidSelectiveDisclosures();
		}

		return null;
	}
}