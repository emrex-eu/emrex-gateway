package eu.dc4eu.gateway.emreg;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class AcronymRepresentation {

	private String name;
	private String url;
	private List<String> institutions = new ArrayList<String>();
	private String pubKey;
}
