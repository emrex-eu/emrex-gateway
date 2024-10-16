package eu.dc4eu.gateway.service;

import eu.dc4eu.gateway.emreg.AcronymRepresentation;
import eu.dc4eu.gateway.emreg.EmregCountryRepresentation;
import eu.dc4eu.gateway.emreg.EmregRepresentation;

public class EmregCache {

	private static EmregRepresentation emregRepresentationcahce;


	public static AcronymRepresentation getAcronymFor(String acronym) {

		for (EmregCountryRepresentation emregCountryRepresentation : emregRepresentationcahce.getEmregCountryRepresentations()) {
			for (AcronymRepresentation acronymRepresentation : emregCountryRepresentation.getEmps()) {
				if (acronymRepresentation.getInstitutions() == null || acronymRepresentation.getInstitutions()
						.isEmpty()) {
					if (acronymRepresentation.getName()
							.equals(acronym)) {
						return acronymRepresentation;
					}
				}
				else if (acronymRepresentation.getInstitutions()
						.get(0)
						.equals(acronym)) {
					return acronymRepresentation;
				}
			}
		}
		return null;
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
		AcronymRepresentation acronymRepresentation = getAcronymFor(acronym);
		String acronymName=null;
		if (acronymRepresentation != null) {
			if (acronymRepresentation.getInstitutions() == null || acronymRepresentation.getInstitutions()
					.isEmpty()) {
				acronymName = acronymRepresentation.getName();
			} else {
				acronymName = acronymRepresentation.getInstitutions().get(0);
			}
		}
		GatewaySession session = new GatewaySession(getCountryRepresentationFrom(acronym).getCountryCode(), acronymName, getAcronymFor(acronym).getUrl());
		SessionHelper.addSession(session);
		return session;
	}

	public static void setEmregRepresentationcahce(EmregRepresentation emregRepresentation) {
		emregRepresentationcahce = emregRepresentation;
	}
}
