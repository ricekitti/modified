package dev.rice.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import net.i2p.crypto.eddsa.*;
import net.i2p.crypto.eddsa.spec.*;

/** Signed local fixtures only. This class and its private test key never enter the release JAR. */
public final class SecurityTest {
    static final byte[] SEED = new byte[32];
    static final EdDSAPrivateKeySpec SPEC = new EdDSAPrivateKeySpec(SEED, EdDSANamedCurveTable.getByName("Ed25519"));
    static final EdDSAPrivateKey PRIVATE = new EdDSAPrivateKey(SPEC);
    static String hex(byte[] bytes) { StringBuilder out = new StringBuilder(); for (byte b : bytes) out.append(String.format("%02x", b & 255)); return out.toString(); }
    static KeyAuthClient.Response signed(String text, long seconds) throws Exception {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        String timestamp = Long.toString(seconds);
        EdDSAEngine signer = new EdDSAEngine(MessageDigest.getInstance("SHA-512")); signer.initSign(PRIVATE);
        signer.update(timestamp.getBytes(StandardCharsets.UTF_8)); signer.update(body);
        return new KeyAuthClient.Response(body, timestamp, hex(signer.sign()));
    }
    static KeyAuthClient fixture(boolean accepted) {
        return new KeyAuthClient(fields -> {
            String type = fields.get("type"), body;
            if (type.equals("init")) body = "\"sessionid\":\"fixture-session\"";
            else if (type.equals("license")) body = "\"info\":{\"subscriptions\":[{\"expiry\":\"" + (System.currentTimeMillis() / 1000 + 3600) + "\"}]}";
            else body = "\"message\":\"checked\"";
            return signed("{\"success\":" + accepted + ",\"ownerid\":\"" + KeyAuthClient.OWNER + "\"," + body + "}", System.currentTimeMillis() / 1000);
        }, hex(SPEC.getA().toByteArray()));
    }
    public static void grantFixture() throws Exception {
        LicenseRuntime.activate(fixture(true).authenticate("TEST-ONLY", String.join("", Collections.nCopies(64, "a"))));
    }
    interface Attempt { void run() throws Exception; }
    static void rejects(Attempt attempt) throws Exception {
        try { attempt.run(); throw new AssertionError("Invalid response accepted"); }
        catch (SecurityException expected) {}
    }
    public static void main(String[] args) throws Exception {
        String key = hex(SPEC.getA().toByteArray()); long now = System.currentTimeMillis() / 1000;
        KeyAuthClient.Response reply = signed("{\"success\":true}", now);
        SignedReply.verify(reply.body, reply.timestamp, reply.signature, key, now);
        rejects(() -> SignedReply.verify("tampered".getBytes(StandardCharsets.UTF_8), reply.timestamp, reply.signature, key, now));
        rejects(() -> SignedReply.verify(reply.body, reply.timestamp, reply.signature, key, now + 26));
        rejects(() -> SignedReply.verify(reply.body, reply.timestamp, reply.signature, key, now - 26));
        rejects(() -> SignedReply.verify(reply.body, reply.timestamp, null, key, now));
        rejects(() -> SignedReply.verify(reply.body, reply.timestamp, reply.signature, SignedReply.KEYAUTH_KEY, now));
        rejects(() -> fixture(false).authenticate("TEST-ONLY", String.join("", Collections.nCopies(64, "a"))));
        KeyAuthClient wrongOwner = new KeyAuthClient(fields -> signed("{\"success\":true,\"ownerid\":\"other\",\"sessionid\":\"x\"}", now), key);
        rejects(() -> wrongOwner.authenticate("TEST-ONLY", String.join("", Collections.nCopies(64, "a"))));
        KeyAuthClient expired = new KeyAuthClient(fields -> signed("{\"success\":true,\"ownerid\":\"" + KeyAuthClient.OWNER +
            "\",\"sessionid\":\"fixture\",\"info\":{\"subscriptions\":[{\"expiry\":\"1\"}]}}", now), key);
        rejects(() -> expired.authenticate("TEST-ONLY", String.join("", Collections.nCopies(64, "a"))));
        LicenseRuntime.revoke(); rejects(LicenseRuntime::requireValid);
        grantFixture(); LicenseRuntime.requireValid();
        KeyAuthClient api = fixture(true); api.check(LicenseRuntime.session());
        LicenseRuntime.revoke(); rejects(LicenseRuntime::requireValid);
        System.out.println("PASS: signed authentication, tamper/wrong-key/unsigned/replay/future/owner/rejected-license denial, session checks, fail-closed activation/revocation.");
    }
}
