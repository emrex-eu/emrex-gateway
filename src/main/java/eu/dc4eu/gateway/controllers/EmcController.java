package eu.dc4eu.gateway.controllers;


import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

import eu.dc4eu.gateway.ConverterService;
import eu.dc4eu.gateway.EwpResponse;
import eu.dc4eu.gateway.converter.Response;
import eu.dc4eu.gateway.elmo.ElmoTojava;
import eu.dc4eu.gateway.elmo.api.Elmo;
import eu.dc4eu.gateway.emreg.EmregRepresentation;
import eu.dc4eu.gateway.service.EmregCache;
import eu.dc4eu.gateway.service.GatewaySession;
import jakarta.inject.Inject;

@Controller
@RequestMapping("/emc")
public class EmcController {

	@Inject
	private ConverterService converterService;

	Logger logger = LoggerFactory.getLogger(EmcController.class);

	@PostMapping(value = "/onReturn", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String onReturn(Model model, EwpResponse emrexResponse) {
		logger.info("Received response from EMREX: " + emrexResponse);

		ElmoTojava elmoTojava = new ElmoTojava();
		String elmo = elmoTojava.frånGz64(emrexResponse.getElmo());
		Elmo elmoparsed = elmoTojava.transformeraFrånXml(elmo);
		// TODO: Verifiera signatur

		logger.info("parsed  Elmo: " + elmoparsed.getLearner().getGivenNames());

		byte[] elmo64 = Base64.getEncoder().encode(elmo.getBytes(StandardCharsets.UTF_8));

		Response response = converterService.convertElmoToElm(new String(elmo64));

		logger.warn("Converted response"+response);

		// TODO: currently returning to "index"

		model.addAttribute("title", "Emrex Gateway");
		model.addAttribute("emps",new EmregRepresentation());
		return "index";
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
