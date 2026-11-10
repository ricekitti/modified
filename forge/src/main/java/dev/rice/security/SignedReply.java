package dev.rice.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

/** Verifies KeyAuth's timestamp || raw response body before JSON is trusted. */
public final class SignedReply {
    public static final String KEYAUTH_KEY = "5586b4bc69c7a4b487e4563a4cd96afd39140f919bd31cea7d1c6a1e8439422b";
    private SignedReply() {}
    public static void verify(byte[] body, String timestamp, String signature, String publicKey, long nowSeconds) throws Exception {
        if (timestamp == null || !timestamp.matches("[0-9]{1,12}") || body == null)
            throw new SecurityException("License response could not be verified.");
        long issued = Long.parseLong(timestamp);
        if (issued < nowSeconds - 25 || issued > nowSeconds + 25)
            throw new SecurityException("Check your system clock and try again.");
        EdDSAEngine verifier = new EdDSAEngine(MessageDigest.getInstance("SHA-512"));
        verifier.initVerify(new EdDSAPublicKey(new EdDSAPublicKeySpec(hex(publicKey, 32), EdDSANamedCurveTable.getByName("Ed25519"))));
        verifier.update(timestamp.getBytes(StandardCharsets.UTF_8));
        verifier.update(body);
        if (!verifier.verify(hex(signature, 64))) throw new SecurityException("License response could not be verified.");
    }
    public static byte[] hex(String input, int size) {
        if (input == null || input.length() != size * 2) throw new SecurityException("Invalid license signature.");
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            int a = Character.digit(input.charAt(i * 2), 16), b = Character.digit(input.charAt(i * 2 + 1), 16);
            if (a < 0 || b < 0) throw new SecurityException("Invalid license signature.");
            bytes[i] = (byte)(a * 16 + b);
        }
        return bytes;
    }
}
