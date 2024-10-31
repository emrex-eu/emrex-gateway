package eu.dc4eu.gateway.service;

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

	String issuerRequestMock= """
			{

			    "document_data": {
			      "cardHolder": {
			        "birthDate": "1966-06-19 05:37:25.429824508 +0000 UTC",
			        "cardholderStatus": "inactive",
			        "familyName": "Herzog",
			        "givenName": "Sanford",
			        "id": "da9a2731-5c56-4b22-8345-da6791221e13"
			      },
			      "cardInformation": {
			        "expiryDate": "1962-11-17 05:00:01.116373494 +0000 UTC",
			        "id": "d0584540-ef52-4be7-8220-98e722e6d238",
			        "invalidSince": "1908-09-13 14:37:16.135653581 +0000 UTC",
			        "issuanceDate": "1928-12-11 20:03:56.642216763 +0000 UTC",
			        "signature": {
			          "issuer": "Chubb",
			          "seal": "ca6ed58c-0fcf-4937-a4d0-5b6a24cf1a3b"
			        },
			        "validSince": "1929-11-23 10:56:43.877367366 +0000 UTC"
			      },
			      "competentInstitution": {
			        "id": "34c04521-5013-4c76-b27a-dd63e665b4bf",
			        "institutionName": "Scale Unlimited"
			      },
			      "pid": {
			        "exhibitorID": "3043447571",
			        "firstName": "Sanford",
			        "gender": "female",
			        "lastName": "Herzog",
			        "pins": []
			      },
			      "signature": {
			        "issuer": "Energy Solutions Forum",
			        "seal": "20ab322b-cd01-4aec-a935-e2bffb21df29"
			      }
			    },
			    "document_data_version": "1.0.0",
			    "document_display": {
			      "description_structured": {
			        "en": "issuer",
			        "sv": "utfärdare"
			      },
			      "type": "EHIC",
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
			      "authentic_source": "SUNET",
			      "collect": {
			        "id": "7ac7b4a1-cf86-4551-b0f5-de37b1cdf0dc",
			        "valid_until": 1731228637
			      },
			      "credential_valid_from": -1755784082,
			      "credential_valid_to": -176812047,
			      "document_id": "$document_id$",
			      "document_type": "EHIC",
			      "document_version": "1.0.0",
			      "real_data": false,
			      "revocation": {
			        "id": "e6b6e99d-921b-4b17-88c5-f1ab5f937dba",
			        "reference": {
			          "authentic_source": "SUNET",
			          "document_id": "$document_id$",
			          "document_type": "EHIC"
			        }
			      }
			    }
			}
			""";

	String issuerNotificationMock= """
			{
			  "authentic_source": "SUNET",
			  "document_id": "$document_id$",
			  "document_type": "EHIC"
			}
			 
 """;

	@Value("${dc4eu.issuer.url}")
	private String issuerURL;

	public String upload(String document_id, String person_id) {
		Apiv1UploadRequest request = convertToRequest(issuerRequestMock, document_id, person_id);


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


	public Apiv1UploadRequest convertToRequest(String issuerData, String document_id, String person_id)  {
		ObjectMapper objectMapper = new ObjectMapper();
		Apiv1UploadRequest request = null;
		try {
			request = objectMapper.readValue(issuerData.replace("$document_id$", document_id).replace("$person_id$",person_id), Apiv1UploadRequest.class);
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


