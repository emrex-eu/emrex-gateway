package eu.dc4eu.gateway.elmo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class GzipUtil {

	private GzipUtil() {
	}

	public static byte[] compress(String string) throws IOException {
		byte[] compressed;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());) {
			try (GZIPOutputStream gos = new GZIPOutputStream(os);) {
				gos.write(string.getBytes());
				//gos.close();
			}
			compressed = os.toByteArray();
			//os.close();
		}
		return compressed;
	}

	public static String gzipDecompress(byte[] compressed) throws IOException {
		byte[] bytes = gzipDecompressBytes(compressed);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public static byte[] gzipDecompressBytes(byte[] compressed) throws IOException {
		byte[] bytes;
		final int BUFFER_SIZE = 32;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream();
			 ByteArrayInputStream is = new ByteArrayInputStream(compressed);
			 GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);) {
			byte[] data = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = gis.read(data)) != -1) {
				os.write(data, 0, bytesRead);
			}
			//gis.close();
			bytes = os.toByteArray();
		}
		return bytes;
	}

}
