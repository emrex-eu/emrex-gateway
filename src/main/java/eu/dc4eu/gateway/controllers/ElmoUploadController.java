package eu.dc4eu.gateway.controllers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

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
				elmo = elmoTojava.frånGz64(content);
			} else {
				elmo = content;
			}

			Elmo elmoparsed = elmoTojava.transformeraFrånXml(elmo);
			byte[] elmoData = Base64.getEncoder().encode(elmo.getBytes(StandardCharsets.UTF_8));
			Response response = converterService.convertElmoToElm(new String(elmoData));

			logger.warn("Response: {}", response);

			byte[] data = Base64.getDecoder().decode(response.getContent().getBytes(StandardCharsets.UTF_8));

			logger.warn("ELM: {}", new String(data));

			String person_id = getPersonId(elmoparsed);
			String document_id = UUID.randomUUID().toString();
			String collect_id = UUID.randomUUID().toString();

			String issuerResponse = issuerService.upload(document_id, person_id, collect_id);
			logger.warn("Issuer response:" + issuerResponse);
			Apiv1NotificationReply notification = issuerService.notification(document_id);

			logger.warn("Got deeplink: " + notification.getData().getCredentialOffer());

			model.addAttribute("title", "Emrex Gateway");
			model.addAttribute("emps", new EmregRepresentation());
			model.addAttribute("qr_code", notification.getData().getBase64Image());
			model.addAttribute("qr_url", notification.getData().getCredentialOffer());

			return "success";

		} catch (Exception e) {
			return "error";
		}
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
