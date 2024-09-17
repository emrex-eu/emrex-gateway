package eu.dc4eu.gateway;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dc4eu.gateway.emreg.Emreg;


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
	}
}