package eu.dc4eu.gateway.controllers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import eu.dc4eu.gateway.elmo.CertificateHelper;
import eu.dc4eu.gateway.emreg.AcronymRepresentation;
import eu.dc4eu.gateway.issuer.Apiv1NotificationReply;
import eu.dc4eu.gateway.service.IssuerService;
import eu.dc4eu.gateway.service.SessionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

	@Inject
	private IssuerService issuerService;

	@Value("${dc4eu.return.url}")
	private String returnHost = "http://localhost:8080";

	Logger logger = LoggerFactory.getLogger(EmcController.class);

	@PostMapping(value = "/onReturn", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String onReturn(Model model, EwpResponse emrexResponse) {
		logger.info("Received response from EMREX: " + emrexResponse.toString().substring(0,100)+"...");

		ElmoTojava elmoTojava = new ElmoTojava();
		String elmo = elmoTojava.frånGz64(emrexResponse.getElmo());
		Elmo elmoparsed = elmoTojava.transformeraFrånXml(elmo);

		boolean signatureOK = verifyElmoSignature(emrexResponse, elmo);

		logger.warn("Signature OK: " + signatureOK);

		if (! signatureOK) {
			logger.error("Signature verification failed");
			return "error";
		}

		logger.info("parsed  Elmo: " + elmoparsed.getLearner().getGivenNames());


		byte[] elmo64 = Base64.getEncoder().encode(elmo.getBytes(StandardCharsets.UTF_8));

		Response response = converterService.convertElmoToElm(new String(elmo64));

		logger.warn("Response: {}", response);

		byte[] data = Base64.getDecoder().decode(response.getContent().getBytes(StandardCharsets.UTF_8));

		logger.warn("ELM: {}", new String(data));

		String person_id=getPersonId(elmoparsed);
		String document_id= UUID.randomUUID().toString();
		String collect_id= UUID.randomUUID().toString();

		String issuerResponse = issuerService.upload(document_id, person_id, collect_id);
		logger.warn("Issuer response:"+issuerResponse);
		// TODO: currently returning to "index"
		Apiv1NotificationReply notification = issuerService.notification(document_id);

		notification.getData().getBase64Image();
		logger.warn("Got deeplink: " + notification.getData().getCredentialOffer());

		model.addAttribute("title", "Emrex Gateway");
		model.addAttribute("emps",new EmregRepresentation());
		model.addAttribute("qr_code", notification.getData().getBase64Image());
		model.addAttribute("qr_url", notification.getData().getCredentialOffer());

		return "success";
	}

	private String getPersonId(Elmo elmoparsed) {
		for (Elmo.Learner.Identifier identifier : elmoparsed.getLearner().getIdentifier()) {
			if (identifier.getType().equals("nationalIdentifier")) {
				return identifier.getValue();
			}
		}
		return elmoparsed.getLearner().getBday().toString();
	}

	private boolean verifyElmoSignature(EwpResponse emrexResponse, String elmo) {
		String sessionId = emrexResponse.getSessionId();
		if (sessionId == null) {
			logger.error("No session id in response");
			return false;
		}
		GatewaySession session = SessionHelper.getSession(sessionId);
		if (session == null) {
			logger.error("No session found for session id: " + sessionId);
			return false;
		}
		AcronymRepresentation acronymRepresentation = EmregCache.getAcronymFor(session.getAcronym());
		if (acronymRepresentation == null) {
			logger.error("No acronym representation found for acronym: " + session.getAcronym());
			return false;
		}

		String publicKey = acronymRepresentation.getPubKey();

		return CertificateHelper.verifySignature(publicKey,elmo);
	}

	@GetMapping(value = "/createSession/acronym/{acronym}")
	public String createSession(final Model model,
			@PathVariable("acronym") String acronym) {

		logger.info("Creating session");
		GatewaySession session = EmregCache.createSession(acronym);
		model.addAttribute("sessionId", session.getSessionId());
		model.addAttribute("url", session.getUrl());
		model.addAttribute("returnUrl", returnHost + "/emc/onReturn");

		return "session";
	}


}
