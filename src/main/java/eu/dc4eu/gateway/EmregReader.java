package eu.dc4eu.gateway;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.dc4eu.gateway.emreg.AcronymRepresentation;
import eu.dc4eu.gateway.emreg.Country;
import eu.dc4eu.gateway.emreg.Emreg;
import eu.dc4eu.gateway.emreg.EmregCountryRepresentation;
import eu.dc4eu.gateway.emreg.EmregRepresentation;
import lombok.Getter;


@Component
@Getter
public class EmregReader {

	private Emreg emreg;

	@Value("${emreg.registry.url}")
	private String emregUrl;

	public void fetchEmreg() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			URI uri = new URI(emregUrl);
			this.emreg = objectMapper.readValue(uri.toURL(), Emreg.class);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public EmregRepresentation getEmregRepresentation() {
		EmregRepresentation emregRepresentation = new EmregRepresentation();
		emregRepresentation.setEmregCountryRepresentations(new ArrayList<>());
		emreg.getEmps().forEach(emp -> {
			EmregCountryRepresentation emregCountryRepresentation = countryCodeExists(emp.getCountryCode(), emregRepresentation);

			if (emregCountryRepresentation == null) {
				emregCountryRepresentation = new EmregCountryRepresentation();
				emregCountryRepresentation.setCountryCode(emp.getCountryCode());
				Country country = emreg.getCountries().stream()
									   .filter(c -> c.getCountryCode().equals(emp.getCountryCode()))
									   .findFirst()
									   .orElse(null);

				emregCountryRepresentation.setCountryName(country.getCountryName());
				emregCountryRepresentation.setSingleFetch(country.getSingleFetch());

				AcronymRepresentation acronymRepresentation = new AcronymRepresentation();
				acronymRepresentation.setName(emp.getAcronym());
				acronymRepresentation.setUrl(emp.getUrl().toString());
				acronymRepresentation.setInstitutions(emp.getInstitutions());
				acronymRepresentation.setPubKey(emp.getPubKey());

				List<AcronymRepresentation> acronymRepresentations = new ArrayList<>();
				acronymRepresentations.add(acronymRepresentation);
				emregCountryRepresentation.setEmps(acronymRepresentations);
				emregRepresentation.getEmregCountryRepresentations().add(emregCountryRepresentation);
			} else {
				AcronymRepresentation acronymRepresentation = new AcronymRepresentation();
				acronymRepresentation.setName(emp.getAcronym());
				acronymRepresentation.setUrl(emp.getUrl().toString());
				acronymRepresentation.setInstitutions(emp.getInstitutions());
				acronymRepresentation.setPubKey(emp.getPubKey());
				emregCountryRepresentation.getEmps().add(acronymRepresentation);
			}
		});


		return emregRepresentation;
	}

	private EmregCountryRepresentation countryCodeExists(String countryCode, EmregRepresentation emregRepresentation) {
		return emregRepresentation.getEmregCountryRepresentations().stream()
								  .filter(emregCountryRepresentation -> emregCountryRepresentation.getCountryCode()
																								  .equals(countryCode)).findFirst()
								  .orElse(null);
	}
}


