package eu.dc4eu.gateway.elmo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

	// TODO Implement!
	private void saveImportedData(ElmoImportData importedData) {
		String fileName = importedData.getFilename();

	}

	private ElmoImportData retrieveFeed(long i) {
		String data = retrieveFeedFromREST(i);
		return parseOutElmoImportData(data);
	}

	private long retrieveCurrentIdFromFeed() {
		String data = retrieveFeedFromREST(1);
		return parseOutLastFeedId(data);
	}

	// TODO Implement!
	private ElmoImportData parseOutElmoImportData(String data) {
		return null;
	}

	// TODO Implement!
	private String retrieveFeedFromREST(long i) {
		return null;
	}

	// TODO Implement!
	private long parseOutLastFeedId(String data) {
		return 0;
	}
}