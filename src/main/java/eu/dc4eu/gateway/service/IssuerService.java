package eu.dc4eu.gateway.service;

import org.apache.catalina.realm.AuthenticatedUserRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.dc4eu.gateway.issuer.Apiv1NotificationReply;
import eu.dc4eu.gateway.issuer.Apiv1NotificationRequest;
import eu.dc4eu.gateway.issuer.Apiv1UploadRequest;
import lombok.Getter;


@Component
@Getter
public class IssuerService {

	Logger logger = LoggerFactory.getLogger(IssuerService.class);

	String issuerRequestMock= """
			{

			    "document_data": {
			      "elm": "$elm_base64$"
			    },
			    "document_data_version": "1.0.0",
			    "document_display": {
			      "description_structured": {
			        "en": "issuer",
			        "sv": "utfärdare"
			      },
			      "type": "ELM",
			      "version": "1.0.0"
			    },
			    "identities": [
			      {
			        "authentic_source_person_id": "$person_id$",
			        "birth_date": "2009-04-25",
			        "family_name": "Herzog",
			        "given_name": "Sanford",
			        "schema": {
			          "name": "SE",
			          "version": "1.0.0"
			        }
			      }
			    ],
			    "meta": {
			      "authentic_source": "LADOK",
			      "collect": {
			        "id": "$collect_id$",
			        "valid_until": 1731228637
			      },
			      "credential_valid_from": -1755784082,
			      "credential_valid_to": -176812047,
			      "document_id": "$document_id$",
			      "document_type": "ELM",
			      "document_version": "1.0.0",
			      "real_data": false,
			      "revocation": {
			        "id": "e6b6e99d-921b-4b17-88c5-f1ab5f937dba",
			        "reference": {
			          "authentic_source": "LADOK",
			          "document_id": "$document_id$",
			          "document_type": "ELM"
			        }
			      }
			    }
			}
			""";

	String issuerNotificationMock= """
			{
			  "authentic_source": "LADOK",
			  "document_id": "$document_id$",
			  "document_type": "ELM"
			}
			 
 """;

	@Value("${dc4eu.issuer.url}")
	private String issuerURL;

	public String upload(String document_id, String person_id, String collect_id, String elm) {

		logger.warn("Issuer URL: {}", issuerURL);

		Apiv1UploadRequest request = convertToRequest(issuerRequestMock, document_id, person_id, collect_id, elm);


		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");



		HttpEntity<Apiv1UploadRequest> entity = new HttpEntity<>(request, headers);

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				issuerURL+"/upload", HttpMethod.POST, entity, String.class);

		return responseEntity.getBody();
	}

	public Apiv1NotificationReply notification(String document_id) {
		Apiv1NotificationRequest request = convertToRequestNotification(issuerNotificationMock, document_id);


		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");



		HttpEntity<Apiv1NotificationRequest> entity = new HttpEntity<>(request, headers);

		ResponseEntity<Apiv1NotificationReply> responseEntity = restTemplate.exchange(
				issuerURL+"/notification", HttpMethod.POST, entity, Apiv1NotificationReply.class);

		return responseEntity.getBody();
	}


	public Apiv1UploadRequest convertToRequest(String issuerData, String document_id, String person_id, String collect_id, String elm)  {
		ObjectMapper objectMapper = new ObjectMapper();
		Apiv1UploadRequest request = null;
		try {
			request = objectMapper.readValue(issuerData.replace("$document_id$", document_id)
													   .replace("$person_id$",person_id)
													   .replace("$elm_base64$", elm)
													   .replace("$collect_id$",collect_id), Apiv1UploadRequest.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return request;
	}

	public Apiv1NotificationRequest convertToRequestNotification(String notificationData, String document_id)  {
		ObjectMapper objectMapper = new ObjectMapper();
		Apiv1NotificationRequest request = null;
		try {
			request = objectMapper.readValue(notificationData.replace("$document_id$", document_id), Apiv1NotificationRequest.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return request;
	}
}


