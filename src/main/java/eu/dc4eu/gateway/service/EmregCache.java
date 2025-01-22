package eu.dc4eu.gateway.service;

import java.util.Objects;

import eu.dc4eu.gateway.emreg.AcronymRepresentation;
import eu.dc4eu.gateway.emreg.EmregCountryRepresentation;
import eu.dc4eu.gateway.emreg.EmregRepresentation;
import lombok.Setter;

public class EmregCache {


	@Setter
	private static EmregRepresentation emregRepresentationcahce;


	public static AcronymRepresentation getAcronymFor(String acronym) {

		return emregRepresentationcahce.getEmregCountryRepresentations().stream()
									   .flatMap(emregCountryRepresentation -> emregCountryRepresentation.getEmps().stream())
									   .filter(acronymRepresentation -> {
										   if (acronymRepresentation.getInstitutions() == null || acronymRepresentation.getInstitutions()
																													   .isEmpty()) {
											   return acronymRepresentation.getName().equals(acronym);
										   } else {
											   return acronymRepresentation.getInstitutions().get(0).equals(acronym);
										   }
									   })
									   .findFirst()
									   .orElse(null);
	}

	private static EmregCountryRepresentation getCountryRepresentationFrom(String acronym) {

		for (EmregCountryRepresentation emregCountryRepresentation : emregRepresentationcahce.getEmregCountryRepresentations()) {
			for (AcronymRepresentation acronymRepresentation : emregCountryRepresentation.getEmps()) {
				if (acronymRepresentation.getInstitutions() == null || acronymRepresentation.getInstitutions()
																												.isEmpty()) {
					if (acronymRepresentation.getName().equals(acronym)) {
						return emregCountryRepresentation;
					}
				} else {
					if (acronymRepresentation.getInstitutions().get(0).equals(acronym)) {
						return emregCountryRepresentation;
					}
				}
			}
		}

		return null;
	}

	public static GatewaySession createSession(String acronym) {
		AcronymRepresentation acronymRepresentation = getAcronymFor(acronym);
		String acronymName = null;
		if (acronymRepresentation != null) {
			if (acronymRepresentation.getInstitutions() == null || acronymRepresentation.getInstitutions()
																						.isEmpty()) {
				acronymName = acronymRepresentation.getName();
			} else {
				acronymName = acronymRepresentation.getInstitutions().get(0);
			}
		}
		GatewaySession session = new GatewaySession(getCountryRepresentationFrom(acronym).getCountryCode(), acronymName, Objects.requireNonNull(getAcronymFor(acronym))
																																.getUrl());
		SessionHelper.addSession(session);
		return session;
	}

}
