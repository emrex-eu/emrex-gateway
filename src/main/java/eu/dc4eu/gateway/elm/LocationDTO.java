package eu.dc4eu.gateway.elm;

import lombok.Getter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Getter
public class LocationDTO {

    private final URI id;
    private final String type;
    private final List<AddressDTO> address = new ArrayList<>();


    public LocationDTO(AddressDTO a) {
        this.id = URI.create("urn:epass:default:Location:".concat(String.valueOf(System.nanoTime())));
        type = this.getClass().getSimpleName().replaceAll("DTO$", "");
        address.add(a);
    }
}
