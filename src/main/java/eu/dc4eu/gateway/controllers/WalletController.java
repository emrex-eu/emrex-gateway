package eu.dc4eu.gateway.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import eu.dc4eu.gateway.model.QRReply;
import eu.dc4eu.gateway.model.QRRequest;

@Controller
@RequestMapping("/vc")
public class WalletController {

	@Value("${dc4eu.issuer.url}")
	private String issuerURL;

	Logger logger = LoggerFactory.getLogger(WalletController.class);


	private final StringHttpMessageConverter stringHttpMessageConverter;

	public WalletController(StringHttpMessageConverter stringHttpMessageConverter) {
		this.stringHttpMessageConverter = stringHttpMessageConverter;
	}

	@GetMapping("/wallet")
	public String ewps(final Model model) {
		model.addAttribute("title", "Emrex Gateway");
		model.addAttribute("showupload", false);
		getQrCode(model);
		return "wallet";
	}

	@GetMapping("/qrcode")
	public String getQrcode(final Model model) {
		getQrCode(model);
		model.addAttribute("title", "Emrex Gateway");
		model.addAttribute("showupload", false);
		model.addAttribute("qr_code", null);
		return "wallet";
	}


	private void getQrCode(final Model model) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		// Create the QRRequest object
		QRRequest qrRequest = new QRRequest();
		qrRequest.setDocumentType("EHIC");
		HttpEntity<QRRequest> entity = new HttpEntity<>(qrRequest, headers);

		String url = issuerURL+":444/qr-code";
		ResponseEntity<QRReply> responseEntity = restTemplate.exchange(
				url, HttpMethod.POST, entity, QRReply.class);

		logger.warn("responseEntity={}", responseEntity.toString());
		QRReply qrReply = responseEntity.getBody();

		if (qrReply != null) {
			// Dynamically construct the URL
			String dc4euWWWalletURL = String.format(
				"https://dc4eu.wwwallet.org/cb?client_id=%s&request_uri=%s",
				qrReply.getClientId(),
				qrReply.getRequestUri()
			);

			String openInDemoWWWalletButton =  String.format(
					"https://demo.wwwallet.org/cb?client_id=%s&request_uri=%s",
					qrReply.getClientId(),
					qrReply.getRequestUri()
			);

			model.addAttribute("qr_code", qrReply.getBase64Image());
			model.addAttribute("qr_uri", qrReply.getUri());
			model.addAttribute("dc4euWWWalletURL", dc4euWWWalletURL);
			model.addAttribute("openInDemoWWWalletButton", openInDemoWWWalletButton);
		} else {
			model.addAttribute("qr_code", null);
			model.addAttribute("qr_uri", null);
			model.addAttribute("dc4euWWWalletURL", null);
			model.addAttribute("openInDemoWWWalletButton", null);
		}
	}
}
