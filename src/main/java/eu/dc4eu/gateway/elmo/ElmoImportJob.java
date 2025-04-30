package eu.dc4eu.gateway.elmo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import eu.dc4eu.gateway.model.Disclosure;
import eu.dc4eu.gateway.model.PaginatedVerificationRecordsReply;
import eu.dc4eu.gateway.model.PaginatedVerificationRecordsRequest;
import jakarta.inject.Inject;

@Component
public class ElmoImportJob {

	private static final Logger logger = LoggerFactory.getLogger(ElmoImportJob.class);

	@Inject
	ElmoImportReader elmoImportReader;

	public void importElmoFiles() {
		logger.info("Running scheduled Elmo import job...");
		// Check already imported files
		List<ElmoImportData> existingImportData = elmoImportReader.importFromDir();
		long existingMaxId = ElmoImportReader.getMaxId(existingImportData);

		logger.info("Existing max ID: " + existingMaxId);

		long currentId = retrieveCurrentIdFromFeed();

		logger.info("Current ID from feed: " + currentId);

		if (existingMaxId >= currentId) {
			logger.info("No new Elmo files to import.");
			return;
		}
		logger.info("New Elmo files found. Importing...");

		for (long i=existingMaxId+1; i<=currentId; i++) {
			ElmoImportData importedData = retrieveFeed(i);
			// There might be holes in the feed, so we need to check if the data is null
			if (importedData != null) {
				saveImportedData(importedData);
			}
		}
	}

	private void saveImportedData(ElmoImportData importedData) {
		String fileName = importedData.getFilename();
	}

	private ElmoImportData retrieveFeed(long i) {
		List<Disclosure> disclosures = retrieveFeedFromREST(i);

		for (Disclosure disclosure : disclosures) {
			if (disclosure.getKey().equals("ELM")) {
				return parseOutElmoImportData((String) disclosure.getValue());
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

	// TODO Implement!
	private ElmoImportData parseOutElmoImportData(String data) {
		return null;
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