package eu.dc4eu.gateway.emreg;

import org.joda.time.DateTime;

import java.util.UUID;

public class GatewaySession {
    String sessionId;

    String countryCode;

    String acronym;

    DateTime created;

    public GatewaySession(String countryCode, String acronym) {
        this.countryCode = countryCode;
        this.acronym = acronym;

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
}
