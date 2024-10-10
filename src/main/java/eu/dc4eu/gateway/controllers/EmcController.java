package eu.dc4eu.gateway.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import eu.dc4eu.gateway.EwpResponse;
import eu.dc4eu.gateway.elmo.ElmoTojava;
import eu.dc4eu.gateway.elmo.api.Elmo;
import eu.dc4eu.gateway.service.EmregCache;
import eu.dc4eu.gateway.service.GatewaySession;

@Controller
@RequestMapping("/emc")
public class EmcController {

	Logger logger = LoggerFactory.getLogger(EmcController.class);

	@PostMapping(value = "/onReturn", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public void onReturn(EwpResponse emrexResponse) {
		logger.info("Received response from EMREX: " + emrexResponse);

		ElmoTojava elmoTojava = new ElmoTojava();
		String elmo = elmoTojava.frånGz64(emrexResponse.getElmo());
		Elmo elmoparsed = elmoTojava.transformeraFrånXml(elmo);

		logger.info("parsed  Elmo: " + elmoparsed.getLearner().getGivenNames());

	}

	@GetMapping(value = "/createSession/acronym/{acronym}")
	public String createSession(final Model model,
			@PathVariable("acronym") String acronym) {

		logger.info("Creating session");
		GatewaySession session = EmregCache.createSession(acronym);
		model.addAttribute("sessionId", session.getSessionId());
		model.addAttribute("url", session.getUrl());
		model.addAttribute("returnUrl", "http://localhost:8080/emc/onReturn");

		return "session";
	}


}
