package eu.dc4eu.gateway.controllers;

import eu.dc4eu.gateway.emreg.GatewaySession;
import eu.dc4eu.gateway.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class EmcController {

    private final GatewayService gatewayService;

    @Autowired
    public EmcController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping("/createSession/{countyCode}/{acronym}/{url}")
    public String session(final Model model,
                        @PathVariable("countryCode") String countyCode,
                        @PathVariable("acronym") String acronym,
                        @PathVariable("url") String url) {

        GatewaySession session = gatewayService.createSession(countyCode, acronym);
        model.addAttribute("session", session.getSessionId());
        model.addAttribute("url", url);
        model.addAttribute("returnUrl", "http://localhost:8080/return");

        return "session";
    }
}
