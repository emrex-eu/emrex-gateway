package eu.dc4eu.gateway.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.dc4eu.gateway.elm.AddressDTO;
import eu.dc4eu.gateway.elm.ConceptDTO;
import eu.dc4eu.gateway.elm.LocationDTO;
import eu.dc4eu.gateway.elm.OrganisationDTO;
import eu.dc4eu.gateway.elmo.CertificateHelper;
import eu.dc4eu.gateway.emreg.AcronymRepresentation;
import eu.dc4eu.gateway.issuer.Apiv1NotificationReply;
import eu.dc4eu.gateway.model.CredentialOfferReply;
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
	public String onReturn(Model model, EwpResponse emrexResponse) throws IOException {
		logger.info("Received response from EMREX: " + emrexResponse.toString().substring(0,100)+"...");

		ElmoTojava elmoTojava = new ElmoTojava();
		String elmo = elmoTojava.fromGz64(emrexResponse.getElmo());
		Elmo elmoparsed = elmoTojava.transformFromXml(elmo);

		boolean signatureOK = verifyElmoSignature(emrexResponse, elmo);

		logger.warn("Signature OK: " + signatureOK);

		if (! signatureOK) {
			logger.error("Signature verification failed");
			return "error";
		}

		logger.info("parsed  Elmo: " + elmoparsed.getLearner().getGivenNames());


		byte[] elmo64 = Base64.getEncoder().encode(elmo.getBytes(StandardCharsets.UTF_8));

		Response response = converterService.convertElmoToElm(new String(elmo64));

		//logger.warn("Response: {}", response);

		String elmAsBase64 = response.getContent();

		byte[] data = Base64.getDecoder().decode(elmAsBase64.getBytes(StandardCharsets.UTF_8));

		//logger.warn("ELM: {}", new String(data));

		String person_id=getPersonId(elmoparsed);
		String given_name=getGivenName(elmoparsed);
		String family_name=getFamilyName(elmoparsed);
		String birth_date=getBirthDate(elmoparsed);

		String document_id= UUID.randomUUID().toString();
		String collect_id= UUID.randomUUID().toString();
		elmAsBase64 = addMissingProperties(data, document_id);  // add missing properties to elm to make it validate

		String issuerResponse = issuerService.upload(document_id, person_id, collect_id, given_name, family_name, birth_date, elmAsBase64);
		logger.warn("Issuer response:"+issuerResponse);
		// TODO: currently returning to "index"
		Apiv1NotificationReply notification = issuerService.notification(document_id);

		notification.getData().getQrBase64();
		logger.warn("Got deeplink: " + notification.getData().getCredentialOfferUrl());

		model.addAttribute("title", "Emrex Gateway");
		model.addAttribute("emps",new EmregRepresentation());
		model.addAttribute("qr_code", notification.getData().getQrBase64());
		model.addAttribute("qr_url", notification.getData().getCredentialOfferUrl());

		String tdata = notification.getData().getCredentialOfferUrl();

		String walletURL = notification.getData().getCredentialOfferUrl().replace("openid-credential-offer://", "");

		//String[] credentialOfferIdVec = notification.getData().getCredentialOfferUrl().split("%2F");
		//String credentialOfferId = credentialOfferIdVec[credentialOfferIdVec.length-1];
		//credentialOfferId = credentialOfferId.replace("elm","urn:eudi:elm:1");
		//String credentialOfferReply = issuerService.getCredentialOffer(credentialOfferId);

		//logger.warn("Got wallet url: " + credentialOfferReply);

		//String walletURL = buildUrlForWebWallets(credentialOfferReply);

		logger.warn("Got wallet url: " + walletURL);

		model.addAttribute("demo_wallet_url", "https://wallet.dc4eu.eu/"+walletURL);

		model.addAttribute("dc4eu_wallet_url", "https://dc4eu.wwwallet.org/"+walletURL);

		return "success";
	}

	private String getBirthDate(Elmo elmoparsed) {
		if (elmoparsed.getLearner() != null && elmoparsed.getLearner().getBday() != null) {
			XMLGregorianCalendar bday = elmoparsed.getLearner().getBday();
			LocalDateTime localDateTime = bday.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
			return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
		}
		return "";
	}

	private String getGivenName(Elmo elmoparsed) {
		if (elmoparsed.getLearner() != null && elmoparsed.getLearner().getGivenNames() != null) {
			return elmoparsed.getLearner().getGivenNames();
		}
		return "";
	}

	private String getFamilyName(Elmo elmoparsed) {
		if (elmoparsed.getLearner() != null && elmoparsed.getLearner().getFamilyName() != null) {
			return elmoparsed.getLearner().getFamilyName();
		}
		return "";
	}

	private String buildUrlForWebWallets(String credentialOfferReply) {
		return "cb?credential_offer"+credentialOfferReply;
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

	/*
	add some constant properties to elm for validation, just some fixed pilot values.
	 */
	String addMissingProperties(byte[] elm, String id) throws IOException {

		JsonNode root = new ObjectMapper().readTree(elm);

		ZonedDateTime now = ZonedDateTime.now();
		((ObjectNode) root).put("id", "uri:" + id);
		((ObjectNode) root).put("issuanceDate", now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
		AddressDTO address = new AddressDTO(new ConceptDTO());
		LocationDTO locationDTO = new LocationDTO(address);
		OrganisationDTO issuerOrg = new OrganisationDTO(locationDTO);
		issuerOrg.getLegalName().put("sv", "Ladok/Sikt pilot");
		issuerOrg.getLegalName().put("no", "Ladok/Sikt pilot");
		((ObjectNode) root).putPOJO("issuer", issuerOrg);
		return new String(Base64.getEncoder().encode(new ObjectMapper().writeValueAsBytes(root)));
	}
}
