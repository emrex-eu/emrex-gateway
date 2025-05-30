package eu.dc4eu.gateway.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import eu.dc4eu.gateway.elmo.ElmoImportJob;
import jakarta.inject.Inject;

@Configuration
@EnableScheduling
public class Dc4euEmrexGatewayConfig {

	@Inject
	private ElmoImportJob elmoImportJob;


	// TODO: remove?
	@Scheduled(initialDelay = 15000, fixedDelay = 600000)
	public void updateEmpsFromEmregRegistry() {

	}

	/**
	 * This method is currently scheduled to run every 20 seconds after an initial delay of 40 seconds
	 * (so that the converter have time to read its cache).
	 *
	 * It imports Elmo files from the wallet.
	 */
	@Scheduled(initialDelay = 60000, fixedDelay = 20000)
	public void importElmoFromWallet() throws IOException {

		elmoImportJob.importElmoFiles();
	}

}
