package eu.dc4eu.gateway.controllers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import eu.dc4eu.gateway.ConverterService;
import eu.dc4eu.gateway.converter.Response;
import eu.dc4eu.gateway.elmo.ElmoTojava;
import eu.dc4eu.gateway.elmo.api.Elmo;
import eu.dc4eu.gateway.emreg.EmregRepresentation;
import eu.dc4eu.gateway.issuer.Apiv1NotificationReply;
import eu.dc4eu.gateway.service.IssuerService;
import jakarta.inject.Inject;

@Controller
@RequestMapping("/upload")
public class ElmoUploadController {


	@Inject
	private ConverterService converterService;

	@Inject
	private IssuerService issuerService;

	Pattern BASE64_PATTERN = Pattern.compile(
			"^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$"
	);


	Logger logger = LoggerFactory.getLogger(ElmoUploadController.class);

	@PostMapping(value = "/uploadElmo")
	public String submit(Model model, @RequestParam("file") MultipartFile file) {
		try {
			String content = new String(file.getBytes());
			ElmoTojava elmoTojava = new ElmoTojava();
			String elmo = "";
			if (BASE64_PATTERN.matcher(content).matches()) {
				elmo = elmoTojava.fromGz64(content);
			} else {
				elmo = content;
			}

			Elmo elmoparsed = elmoTojava.transformFromXml(elmo);
			byte[] elmoData = Base64.getEncoder().encode(elmo.getBytes(StandardCharsets.UTF_8));
			Response response = converterService.convertElmoToElm(new String(elmoData));

			logger.warn("Response: {}", response);


			String elmAsBase64 = response.getContent();
			byte[] data = Base64.getDecoder().decode(elmAsBase64.getBytes(StandardCharsets.UTF_8));

			logger.warn("ELM: {}", new String(data));

			String person_id = getPersonId(elmoparsed);
			String document_id = UUID.randomUUID().toString();
			String collect_id = UUID.randomUUID().toString();

			String given_name = getGivenName(elmoparsed);
			String family_name = getFamilyName(elmoparsed);
			String birth_date = getBirthDate(elmoparsed);

			String issuerResponse = issuerService.upload(document_id, person_id, collect_id, given_name, family_name, birth_date, elmAsBase64);
			logger.warn("Issuer response:" + issuerResponse);
			Apiv1NotificationReply notification = issuerService.notification(document_id);

			logger.warn("Got deeplink: " + notification.getData().getCredentialOfferUrl());

			model.addAttribute("title", "Emrex Gateway");
			model.addAttribute("emps", new EmregRepresentation());
			model.addAttribute("qr_code", notification.getData().getQrBase64());
			model.addAttribute("qr_url", notification.getData().getCredentialOfferUrl());

			return "success";

		} catch (Exception e) {
			return "error";
		}
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

	private String getPersonId(Elmo elmoparsed) {
		for (Elmo.Learner.Identifier identifier : elmoparsed.getLearner().getIdentifier()) {
			if (identifier.getType().equals("nationalIdentifier")) {
				return identifier.getValue();
			}
		}
		return elmoparsed.getLearner().getBday().toString();
	}
}
