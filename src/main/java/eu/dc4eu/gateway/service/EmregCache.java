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
											   return acronymRepresentation.getInstitutions().getFirst().equals(acronym);
										   }
									   })
									   .findFirst()
									   .orElse(null);
	}

	private static EmregCountryRepresentation getCountryRepresentationFrom(String acronym) {

		return emregRepresentationcahce.getEmregCountryRepresentations().stream()
									   .flatMap(emregCountryRepresentation -> emregCountryRepresentation.getEmps().stream()
																										.filter(acronymRepresentation -> {
																											if (emregCountryRepresentation.isSingleFetch()) {
																												return (acronymRepresentation.getInstitutions() == null || acronymRepresentation.getInstitutions()
																																																.isEmpty()) ?
																													   acronymRepresentation.getName()
																																			.equals(acronym) :
																													   acronymRepresentation.getInstitutions()
																																			.getFirst()
																																			.equals(acronym);
																											} else {
																												return true;
																											}
																										})
																										.map(acronymRepresentation -> emregCountryRepresentation))
									   .findFirst()
									   .orElse(null);
	}

	public static GatewaySession createSession(String acronym) {
		AcronymRepresentation acronymRepresentation = getAcronymFor(acronym);
		String acronymName = null;
		if (acronymRepresentation != null) {
			if (acronymRepresentation.getInstitutions() == null || acronymRepresentation.getInstitutions()
																						.isEmpty()) {
				acronymName = acronymRepresentation.getName();
			} else {
				acronymName = acronymRepresentation.getInstitutions().getFirst();
			}
		}
		GatewaySession session = new GatewaySession(getCountryRepresentationFrom(acronym).getCountryCode(), acronymName, Objects.requireNonNull(getAcronymFor(acronym))
																																.getUrl());
		SessionHelper.addSession(session);
		return session;
	}

}
