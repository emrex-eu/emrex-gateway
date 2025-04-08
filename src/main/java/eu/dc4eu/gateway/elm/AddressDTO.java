package eu.dc4eu.gateway.elm;

import lombok.Getter;

import java.net.URI;


@Getter
public class AddressDTO  {

    private final URI id;
    private final String type;
    private final ConceptDTO countryCode; // ControlledList.COUNTRY - cached


    public AddressDTO(ConceptDTO countryCode) {
        type = this.getClass().getSimpleName().replaceAll("DTO$", "");
        id = URI.create("urn:epass:default:Address:".concat(String.valueOf(System.nanoTime())));
        this.countryCode = countryCode;
    }

}
