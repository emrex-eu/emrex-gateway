//package eu.dc4eu.gateway.elmo;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import se.ladok.common.properties.LadokPropertyManager;
//import se.ladok.emrex.common.CertificateHelper;
//import se.ladok.emrex.domain.model.emreg.CountryCode;
//import se.ladok.emrex.domain.model.emreg.NCP;
//import se.ladok.emrex.domain.model.emreg.NCPRepository;
//
//@Component
//public class SignaturVerifierare {
//
//	private final NCPRepository ncpRepository;
//	private final LadokPropertyManager ladokPropertyManager;
//	private static final String mockAcronym = "Nya Ladok";
//
//	@Autowired
//	public SignaturVerifierare(NCPRepository ncpRepository, LadokPropertyManager ladokPropertyManager) {
//		this.ncpRepository = ncpRepository;
//		this.ladokPropertyManager = ladokPropertyManager;
//	}
//
//	public boolean ärSignaturVerifieradFör(String elmoXml, EmrexImportSession importSession) {
//
//		String mockEmregString = ladokPropertyManager.get("ladok.emreg.mock");
//		boolean mockEmreg = mockEmregString.equals("true") || mockEmregString.equals("True") || mockEmregString.equals("TRUE");
//		if (mockEmreg && importSession.acronym().equals(mockAcronym)) {
//			return true;
//		} else {
//			NCP ncp = ncpRepository.hämtaFrån(importSession.acronym(), CountryCode.valueOf(importSession.countryCode()
//																										.getCountryCode()));
//
//			return CertificateHelper.verifySignature(ncp.pubKey(), elmoXml);
//		}
//	}
//}
