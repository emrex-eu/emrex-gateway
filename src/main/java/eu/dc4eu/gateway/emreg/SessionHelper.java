package eu.dc4eu.gateway.emreg;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class SessionHelper {

    public static final int MAX_MINUTES=15;

    static private List<GatewaySession> sessions = new ArrayList<>();

    public static GatewaySession getSession(String sessionId) {
        for (GatewaySession session : sessions) {
            if (session.getSessionId().equals(sessionId)) {
                DateTime timelimit = DateTime.now().minusMinutes(MAX_MINUTES);
                if (timelimit.isBefore(session.getCreated())) {
                    return session;
                } else {
                    return null; // remove session?
                }
            }
        }
        return null;
    }

    public static void addSession(GatewaySession session) {
        sessions.add(session);
    }
}
