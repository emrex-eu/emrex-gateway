package eu.dc4eu.gateway.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vc")
public class WalletController {

	@GetMapping("/wallet")
	public String ewps(final Model model) {
		model.addAttribute("title", "Emrex Gateway");
		model.addAttribute("showupload",false);
		return "wallet";
	}

	@GetMapping("/qrcode")
	public String getQrcode(final Model model) {
		model.addAttribute("title", "Emrex Gateway");
		model.addAttribute("showupload",false);
		model.addAttribute("qr_code", null);
		return "wallet";
	}
}
