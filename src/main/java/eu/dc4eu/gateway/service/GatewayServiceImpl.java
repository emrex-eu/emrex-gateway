package eu.dc4eu.gateway.service;

import eu.dc4eu.gateway.emreg.GatewaySession;
import eu.dc4eu.gateway.emreg.SessionHelper;
import org.springframework.stereotype.Component;

@Component
public class GatewayServiceImpl implements GatewayService{
    @Override
    public GatewaySession createSession(String countryCode, String acronym) {
        GatewaySession session = new GatewaySession(countryCode, acronym);
        SessionHelper.addSession(session);
        return session;
    }
}
