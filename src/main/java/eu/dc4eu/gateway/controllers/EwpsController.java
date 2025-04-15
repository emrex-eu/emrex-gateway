package eu.dc4eu.gateway.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.dc4eu.gateway.EmregReader;

@Controller
@RequestMapping("/emrex")
public class EwpsController {

	private EmregReader emregReader;

	@Autowired
	public EwpsController(EmregReader emregReader) {
		this.emregReader = emregReader;
	}

	@GetMapping("/ewps")
	public String ewps(final Model model) {
		model.addAttribute("title", "Emrex Gateway");
		model.addAttribute("emps", emregReader.getEmregRepresentation());
		model.addAttribute("showupload", true); // Ensure showupload is set
		return "emrexewps";

	}

}