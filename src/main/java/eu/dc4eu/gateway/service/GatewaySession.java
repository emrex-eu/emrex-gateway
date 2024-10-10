package eu.dc4eu.gateway.service;

import org.joda.time.DateTime;

import java.util.UUID;

import lombok.Getter;

@Getter
public class GatewaySession {
    String sessionId;

    String countryCode;

    String acronym;

    DateTime created;

    String url;

    public GatewaySession(String countryCode, String acronym, String url) {
        this.countryCode = countryCode;
        this.acronym = acronym;
        this.url = url;

        sessionId = UUID.randomUUID().toString();
        created = DateTime.now();
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getSessionId() {
        return sessionId;
    }

    public DateTime getCreated() {
        return created;
    }

    public String getUrl() {
        return url;
    }
}
