package eu.dc4eu.gateway.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import eu.dc4eu.gateway.EmregReader;

@Controller
public class IndexController {

	private EmregReader emregReader;

	@Autowired
	public IndexController(EmregReader emregReader) {
		this.emregReader = emregReader;
	}


	@GetMapping("/")
	public String index(final Model model) {
		model.addAttribute("title", "Emrex Gateway");
		model.addAttribute("emps",emregReader.getEmregRepresentation());


		return "index";
	}
}



