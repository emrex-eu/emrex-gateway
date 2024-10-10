package eu.dc4eu.gateway.service;



public interface GatewayService {
    GatewaySession createSession(String countryCode, String acronym);
}
