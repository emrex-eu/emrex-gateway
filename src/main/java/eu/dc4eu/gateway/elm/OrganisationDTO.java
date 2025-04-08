package eu.dc4eu.gateway.elm;

import lombok.Getter;

import java.net.URI;

import java.util.*;

@Getter
public class OrganisationDTO {

    /*
      "issuer": {
    "legalName": {
      "no": "Handelshøyskolen BI",
      "en": "BI Norwegian Business School"
    },
    "identifier": {
      "notation": "bi.no",
      "schemeName": "schac",
      "id": "urn:epass:identifier:1094118b-e2a6-4253-bad5-8cc13c65e647",
      "type": "Identifier"
    },
    "location": {
      "address": {
        "countryCode": {
          "inScheme": {
            "id": "http://publications.europa.eu/resource/authority/country",
            "type": "ConceptScheme"
          },
          "notation": "country",
          "prefLabel": {
            "no": "Norge",
            "en": "Norway"
          },
          "id": "http://publications.europa.eu/resource/authority/country/NOR",
          "type": "Concept"
        },
        "id": "urn:epass:address:e33343aa-1b80-44d6-a686-26691d298ac8",
        "type": "Address"
      },
      "id": "urn:epass:location:bf6fa336-6157-4c30-b86f-1b104bd8647a",
      "type": "Location"
    },
    "id": "urn:epass:default:Organisation:325155729271200",
    "type": "Organisation",
    "homepage": {
      "contentURL": "http://www.bi.no",
      "id": "urn:epass:webResource:f030d07a-84b3-40f5-b0f0-f7e5819599d3",
      "type": "WebResource"
    }
  },
     */
    private final URI id;
    private final String type;
    private final Map<String, String> legalName = new HashMap<>();
    private final LocationDTO location;

    /*
    private List<LegalIdentifier> vatIdentifier = new ArrayList<>();
    private List<AccreditationDTO> accreditation = new ArrayList<>();
    private List<OrganisationDTO> hasSubOrganization = new ArrayList<>();
    private LegalIdentifier eIDASIdentifier;
    private List<WebResourceDTO> homepage = new ArrayList<>();

    private MediaObjectDTO logo;
    private OrganisationDTO subOrganizationOf;
    private LegalIdentifier registration;
    private List<LegalIdentifier> taxIdentifier = new ArrayList<>();
    private LiteralMap altLabel;
    private List<ContactPointDTO> contactPoint = new ArrayList<>();
    private List<GroupDTO> groupMemberOf = new ArrayList<>();
    private List<Identifier> identifier = new ArrayList<>();
    private ZonedDateTime dateModified;
    private List<LocationDTO> location = new ArrayList<>();
    private List<NoteDTO> additionalNote = new ArrayList<>();
    private LiteralMap prefLabel;
*/


    private static final String IDENTIFIER_PREFIX = "urn:epass:default:Organisation:";

    public OrganisationDTO(LocationDTO loc ) {
        id = URI.create(IDENTIFIER_PREFIX.concat(String.valueOf(System.nanoTime())));
        type = "Organisation";
        this.location = loc;
    }
}
