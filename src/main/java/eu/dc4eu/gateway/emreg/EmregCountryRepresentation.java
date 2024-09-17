package eu.dc4eu.gateway.emreg;

import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class EmregCountryRepresentation {

	private String countryCode;
	private String countryName;
	private boolean singleFetch;
	private List<AcronymRepresentation> emps;
}
