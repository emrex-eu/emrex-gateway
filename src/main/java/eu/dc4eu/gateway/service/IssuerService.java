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
import eu.dc4eu.gateway.model.CredentialOfferReply;
import lombok.Getter;


@Component
@Getter
public class IssuerService {

	Logger logger = LoggerFactory.getLogger(IssuerService.class);

	public static final String API_PART="/api/v1";

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
			        "birth_date": "$birth_date$",
			        "family_name": "$family_name$",
			        "given_name": "$given_name$",
			        "schema": {
			          "name": "DefaultSchema"
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
			      "document_type": "urn:edui:elm:1",
			      "document_version": "1.0.0",
			      "real_data": false,
			      "revocation": {
			      			"reference": {}
			      }			    
			    }
			}
			""";

	String issuerNotificationMock= """
			{
			  "authentic_source": "LADOK",
			  "document_id": "$document_id$",
			  "document_type": "urn:edui:elm:1"
			}
			 
 """;

	@Value("${dc4eu.issuer.url}")
	private String issuerURL;

	public String upload(String document_id, String person_id, String collect_id, String given_name, String family_name, String birth_date, String elm) {

		logger.warn("Issuer URL: {}", issuerURL);

		Apiv1UploadRequest request = convertToRequest(issuerRequestMock, document_id, person_id, collect_id, given_name, family_name, birth_date, elm);


		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<Apiv1UploadRequest> entity = new HttpEntity<>(request, headers);

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				issuerURL+API_PART+"/upload", HttpMethod.POST, entity, String.class);

		return responseEntity.getBody();
	}

	public Apiv1NotificationReply notification(String document_id) {
		Apiv1NotificationRequest request = convertToRequestNotification(issuerNotificationMock, document_id);


		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");



		HttpEntity<Apiv1NotificationRequest> entity = new HttpEntity<>(request, headers);

		ResponseEntity<Apiv1NotificationReply> responseEntity = restTemplate.exchange(
				issuerURL+API_PART+"/notification", HttpMethod.POST, entity, Apiv1NotificationReply.class);

		return responseEntity.getBody();
	}

	/**
	 * {"credential_issuer":"https://vc-interop-3.sunet.se",
	 * "credential_configuration_ids":[""],
	 * "grants":{"authorization_code":{"issuer_state":"collect_id=586a42ad-4e19-4148-ada3-1ccb0d1820f9\u0026document_type=ELM\u0026authentic_source=LADOK"}}}
	 * @param credentialOfferId
	 * @return
	 */
	public String getCredentialOffer(String credentialOfferId) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<CredentialOfferReply> responseEntity = restTemplate.exchange(
				issuerURL+"/credential-offer/"+credentialOfferId, HttpMethod.GET, entity, CredentialOfferReply.class);


		String jsonString = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			jsonString = objectMapper.writeValueAsString(responseEntity.getBody());
		} catch (Exception e) {
			logger.error("Error converting response body to JSON string", e);
		}



		return jsonString;
	}


	public Apiv1UploadRequest convertToRequest(String issuerData, String document_id, String person_id, String collect_id, String given_name, String family_name, String birth_date, String elm)  {
		ObjectMapper objectMapper = new ObjectMapper();
		Apiv1UploadRequest request = null;
		try {
			request = objectMapper.readValue(issuerData.replace("$document_id$", document_id)
													   .replace("$person_id$",person_id)
													   .replace("$given_name$", given_name)
													   .replace("$family_name$", family_name)
													   .replace("$birth_date$", birth_date)
													   .replace("$elm_base64$", elm)
													   .replace("$collect_id$",collect_id), Apiv1UploadRequest.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		try {
			logger.warn("Request payload: {}", objectMapper.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			logger.error("Error converting request to JSON string", e);
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


