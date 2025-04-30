package eu.dc4eu.gateway;

import java.util.List;

import eu.dc4eu.gateway.converter.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;


@Component
@Getter
public class ConverterService {

	@Value("${dc4eu.converter.url}")
	private String converterURL;

	public Response convertElmoToElm(String elmo) {
		Protocol fromProtocol = new Protocol();
		fromProtocol.setName("elmo");
		fromProtocol.setVersion("1.7");
		Protocol toProtocol = new Protocol();
		toProtocol.setName("elm");
		toProtocol.setVersion("3.2");
		Parameters parameters = new Parameters();
		parameters.setPreferredLanguages(List.of("en", "sv", "no"));
		parameters.setDefaultLanguage("en");
		Request request = new Request();
		request.setFrom(fromProtocol);
		request.setTo(toProtocol);
		request.setParameters(parameters);
		request.setContent(elmo);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<Request> entity = new HttpEntity<>(request, headers);

		ResponseEntity<Response> responseEntity = restTemplate.exchange(
				converterURL, HttpMethod.POST, entity, Response.class);

		return responseEntity.getBody();
	}

	public Response convertElmToElmo(String elm) {
		Protocol fromProtocol = new Protocol();
		fromProtocol.setName("elm");
		fromProtocol.setVersion("3.2");
		Protocol toProtocol = new Protocol();
		toProtocol.setName("elmo");
		toProtocol.setVersion("1.7");
		Parameters parameters = new Parameters();
		parameters.setPreferredLanguages(List.of("en", "sv", "no"));
		parameters.setDefaultLanguage("en");
		Request request = new Request();
		request.setFrom(fromProtocol);
		request.setTo(toProtocol);
		request.setParameters(parameters);
		request.setContent(elm);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<Request> entity = new HttpEntity<>(request, headers);

		ResponseEntity<Response> responseEntity = restTemplate.exchange(
				converterURL, HttpMethod.POST, entity, Response.class);

		return responseEntity.getBody();
	}
}


