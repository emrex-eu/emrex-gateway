package eu.dc4eu.gateway.service;

import eu.dc4eu.gateway.emreg.GatewaySession;

public interface GatewayService {
    GatewaySession createSession(String countryCode, String acronym);
}
