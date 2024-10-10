package eu.dc4eu.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import eu.dc4eu.gateway.service.EmregCache;


@Component
public class StartupActivites {


	private EmregReader emregReader;

	@Autowired
	public StartupActivites(EmregReader emregReader) {
		this.emregReader = emregReader;
	}

	@EventListener
	public void handleContextRefresh(ContextRefreshedEvent event) {
		this.emregReader.fetchEmreg();
		EmregCache.setEmregRepresentationcahce(this.emregReader.getEmregRepresentation());
	}
}