package eu.dc4eu.gateway.service;

import eu.dc4eu.gateway.emreg.AcronymRepresentation;
import eu.dc4eu.gateway.emreg.EmregCountryRepresentation;
import eu.dc4eu.gateway.emreg.EmregRepresentation;

public class EmregCache {

	private static EmregRepresentation emregRepresentationcahce;


	public static AcronymRepresentation getAcronymFor(String acronym) {

		return emregRepresentationcahce.getEmregCountryRepresentations().stream()
									   .flatMap(emregCountryRepresentation -> emregCountryRepresentation.getEmps().stream())
									   .filter(acronymRepresentation -> acronymRepresentation.getInstitutions()
																					   .contains(acronym))
									   .findFirst()
									   .orElse(null);
	}

	private static EmregCountryRepresentation getCountryRepresentationFrom(String acronym) {
		return emregRepresentationcahce.getEmregCountryRepresentations().stream()
									   .filter(emregCountryRepresentation -> emregCountryRepresentation.getEmps().stream()
																									   .anyMatch(acronymRepresentation -> {
																										   if (acronymRepresentation.getInstitutions()
																																	.isEmpty()) {
																											   return false;
																										   } else {
																											   return acronymRepresentation.getInstitutions()
																																		   .getFirst()
																																		   .equals(acronym);
																										   }
																									   }))
									   .findFirst()
									   .orElse(null);
	}

	public static GatewaySession createSession(String acronym) {
		GatewaySession session = new GatewaySession(getCountryRepresentationFrom(acronym).getCountryCode(), getAcronymFor(acronym).getName(), getAcronymFor(acronym).getUrl());
		SessionHelper.addSession(session);
		return session;
	}

	public static void setEmregRepresentationcahce(EmregRepresentation emregRepresentation) {
		emregRepresentationcahce = emregRepresentation;
	}
}
