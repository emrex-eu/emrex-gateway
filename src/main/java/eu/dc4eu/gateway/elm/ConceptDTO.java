package eu.dc4eu.gateway.elm;

import lombok.Getter;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


@Getter
public class ConceptDTO {


    private final URI id;
    private final String type;
    //private ConceptSchemeDTO inScheme;
    private final Map<String, String> prefLabel = new HashMap<>();

    public ConceptDTO() {
        id = URI.create("http://publications.europa.eu/resource/authority/country/NOR");
        type = "Concept";
        prefLabel.put("sv", "Sverige");
    }

}
