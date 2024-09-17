package eu.dc4eu.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class Dc4euEmrexGatewayConfig {

	@Scheduled(initialDelay = 15000, fixedDelay = 600000)
	public void updateEmpsFromEmregRegistry() {


	}

}
