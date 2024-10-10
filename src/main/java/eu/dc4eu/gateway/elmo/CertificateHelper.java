package eu.dc4eu.gateway.elmo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bouncycastle.asn1.ASN1Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import com.fasterxml.jackson.databind.ser.Serializers;

public class CertificateHelper {
	private static final Logger logger = LoggerFactory.getLogger(CertificateHelper.class);

	private static X509Certificate getCertificate(String certString) throws IOException, GeneralSecurityException {
		InputStream is = new ByteArrayInputStream(certString.getBytes());
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
		is.close();
		return cert;
	}

	public static String sign(String certificate, String encKey, String data) {

		logger.warn("Signerar med certifikatet {}...", certificate.substring(28, 38));

		try {

			// Create a DOM XMLSignatureFactory that will be used to generate the enveloped signature.
			XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");


			// Create a Reference to the enveloped document (in this case, you are signing the whole
			// document, so a URI of "" signifies that, and also specify the SHA256 digest algorithm
			// and the ENVELOPED Transform.
			Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA256, null),
					Collections.singletonList(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);

			// Create the SignedInfo.
			SignedInfo si = fac.newSignedInfo(
					fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
					fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null), Collections.singletonList(ref));

			// Instantiate the document to be signed.
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); //Förhindra xxe
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false); //Förhindra xxe
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false); //Förhindra xxe
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //Förhindra xxe
			dbf.setXIncludeAware(false);
			dbf.setExpandEntityReferences(false);

			InputStream is = new ByteArrayInputStream(data
					.getBytes(StandardCharsets.UTF_8.displayName())); // StandardCharsets.ISO_8859_1
			Document doc = dbf.newDocumentBuilder().parse(is);

			// Extract the private key from string
			String strippedEncKey = encKey.replaceAll("(-----.*?-----)", "");

			RSAPrivateKey pk = null;
			try {

				byte[] asn1PrivateKeyBytes =	Base64.getDecoder().decode(strippedEncKey
						.getBytes(StandardCharsets.UTF_8));
//				byte[] asn1PrivateKeyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(strippedEncKey
//						.getBytes(StandardCharsets.UTF_8));
				var asn1sequence = (ASN1Sequence) ASN1Sequence.fromByteArray(asn1PrivateKeyBytes);
				var asn1PrivKey = org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(asn1sequence);
				RSAPrivateKeySpec rsaPrivKeySpec = new RSAPrivateKeySpec(asn1PrivKey.getModulus(), asn1PrivKey.getPrivateExponent());
				KeyFactory kf = KeyFactory.getInstance("RSA");
				pk = (RSAPrivateKey) kf.generatePrivate(rsaPrivKeySpec);
			} catch (ClassCastException e) {
				logger.info("Can't read Private Key in format PKCS1 (will try with PKCS8)");
				byte[] encoded = Base64Coder.decodeLines(strippedEncKey);
				PKCS8EncodedKeySpec rsaPrivKeySpec = new PKCS8EncodedKeySpec(encoded);
				KeyFactory kf = KeyFactory.getInstance("RSA");
				pk = (RSAPrivateKey) kf.generatePrivate(rsaPrivKeySpec);
			}

			// Create a DOMSignContext and specify the RSA PrivateKey and
			// location of the resulting XMLSignature's parent element.
			DOMSignContext dsc = new DOMSignContext(pk, doc.getDocumentElement());

			// Create the XMLSignature, but don't sign it yet.
			KeyInfoFactory kif = fac.getKeyInfoFactory();
			X509Certificate cert = getCertificate(certificate);
			List<Object> x509Content = new ArrayList<Object>();
			x509Content.add(cert.getSubjectX500Principal().getName());
			x509Content.add(cert);
			X509Data xd = kif.newX509Data(x509Content);
			KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));
			XMLSignature signature = fac.newXMLSignature(si, ki);

			// Marshal, generate, and sign the enveloped signature.
			signature.sign(dsc);

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer trans = tf.newTransformer();

			trans.transform(new DOMSource(doc), new StreamResult(os));

			return os.toString(StandardCharsets.UTF_8);
		} catch (Exception e) {
			logger.warn("Got exception ", e);
			return null;
		}
	}

	public static boolean verifySignature(String certificate, String data) {
		// Create a DOM XMLSignatureFactory that will be used to generate the enveloped signature.
		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");


		logger.warn("Verifierar signatur med certifikatet {}...", certificate.substring(28, 38));

		try {
			// Instantiate the document to be signed.
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			dbf.setNamespaceAware(true);
			InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)); // StandardCharsets.ISO_8859_1
			Document doc = dbf.newDocumentBuilder().parse(is, StandardCharsets.UTF_8.displayName());

			// Find Signature element.
			NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
			if (nl.getLength() == 0) {
				logger.warn("Cannot find Signature element");
				return false;
			}

			X509Certificate cert = getCertificate(certificate);
			PublicKey pubKey = cert.getPublicKey();
			DOMValidateContext valContext = new DOMValidateContext(pubKey, nl.item(0));

			// TODO LTRE-59918 Tag bort detta när alla länder bytt från sha1 till sha256
			valContext.setProperty("org.jcp.xml.dsig.secureValidation", false);

			// Unmarshal the XMLSignature.
			XMLSignature signature = fac.unmarshalXMLSignature(valContext);

			// Validate the XMLSignature.
			boolean coreValidity = signature.validate(valContext);

			// Check core validation status.
			if (!coreValidity) {
				logger.error("Signature failed core validation");
				boolean sv = signature.getSignatureValue().validate(valContext);
				logger.error("signature validation status: " + sv);
				if (!sv) {
					// Check the validation status of each Reference.
					Iterator<?> i = signature.getSignedInfo().getReferences().iterator();
					for (int j = 0; i.hasNext(); j++) {
						boolean refValid = ((Reference) i.next()).validate(valContext);
						logger.info("ref[{}] validity status: {}", j, refValid);
					}
				}
			}

			return coreValidity;
		} catch (Exception e) {
			logger.warn("Exception", e);
		}
		return false;
	}
}
