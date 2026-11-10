package dev.rice.security;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;

/** Public application IDs only. No seller API keys, private signing keys or saved licenses. */
public final class KeyAuthClient {
    public static final String APPLICATION = "Soju", OWNER = "UVLPXieCUS", VERSION = "1.0";
    private final Transport transport;
    private final String verificationKey;

    public KeyAuthClient() { this(new HttpsTransport(), SignedReply.KEYAUTH_KEY); }
    KeyAuthClient(Transport transport, String verificationKey) {
        this.transport = transport; this.verificationKey = verificationKey;
    }
    public Session authenticate(String license, String device) throws Exception {
        if (license == null || license.trim().isEmpty() || license.length() > 4096)
            throw new SecurityException("Enter a valid license key.");
        if (device == null || !device.matches("[0-9a-f]{64}")) throw new SecurityException("Could not identify this device.");
        Map<String,String> fields = form("init"); fields.put("ver", VERSION);
        JsonObject init = request(fields);
        String id = string(init, "sessionid");
        if (id.isEmpty() || id.length() > 1024) throw new SecurityException("License service returned an invalid session.");
        fields = form("license"); fields.put("sessionid", id); fields.put("key", license.trim()); fields.put("hwid", device); fields.put("code", "");
        JsonObject reply = request(fields);
        JsonObject info = reply.has("info") && reply.get("info").isJsonObject() ? reply.getAsJsonObject("info") : null;
        long expires = 0;
        if (info != null && info.has("subscriptions") && info.get("subscriptions").isJsonArray()) {
            for (JsonElement row : info.getAsJsonArray("subscriptions")) {
                if (!row.isJsonObject()) continue;
                try { expires = Math.max(expires, Long.parseLong(string(row.getAsJsonObject(), "expiry"))); }
                catch (NumberFormatException invalid) { throw new SecurityException("License subscription could not be verified."); }
            }
        }
        if (expires <= System.currentTimeMillis() / 1000) throw new SecurityException("This license has no active subscription.");
        return new Session(id, expires);
    }
    public void check(Session session) throws Exception {
        Map<String,String> fields = form("check"); fields.put("sessionid", session.id);
        request(fields);
        if (session.expiry <= System.currentTimeMillis() / 1000) throw new SecurityException("Your license has expired.");
    }
    private Map<String,String> form(String type) {
        Map<String,String> fields = new LinkedHashMap<>();
        fields.put("type", type); fields.put("name", APPLICATION); fields.put("ownerid", OWNER);
        return fields;
    }
    private JsonObject request(Map<String,String> fields) throws Exception {
        Response response = transport.post(Collections.unmodifiableMap(fields));
        SignedReply.verify(response.body, response.timestamp, response.signature, verificationKey, System.currentTimeMillis() / 1000);
        JsonElement parsed = new JsonParser().parse(new String(response.body, StandardCharsets.UTF_8));
        if (!parsed.isJsonObject()) throw new SecurityException("License service returned invalid data.");
        JsonObject result = parsed.getAsJsonObject();
        if (!OWNER.equals(string(result, "ownerid"))) throw new SecurityException("License application could not be verified.");
        if (!result.has("success") || !result.get("success").isJsonPrimitive() ||
            !result.getAsJsonPrimitive("success").isBoolean() || !result.get("success").getAsBoolean())
            throw new SecurityException("License rejected, expired, or unavailable for this device.");
        return result;
    }
    private static String string(JsonObject value, String key) {
        return value.has(key) && value.get(key).isJsonPrimitive() ? value.get(key).getAsString() : "";
    }
    public static final class Session {
        private final String id;
        final long expiry;
        private Session(String id, long expiry) { this.id = id; this.expiry = expiry; }
    }
    interface Transport { Response post(Map<String,String> fields) throws Exception; }
    static final class Response {
        final byte[] body; final String timestamp, signature;
        Response(byte[] body, String timestamp, String signature) { this.body = body; this.timestamp = timestamp; this.signature = signature; }
    }
    private static final class HttpsTransport implements Transport {
        public Response post(Map<String,String> fields) throws Exception {
            HttpsURLConnection connection = (HttpsURLConnection)new URL("https://keyauth.win/api/1.3/").openConnection();
            connection.setConnectTimeout(12000); connection.setReadTimeout(12000);
            connection.setInstanceFollowRedirects(false); connection.setUseCaches(false);
            connection.setRequestMethod("POST"); connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "RiceModified/Forge1.8.9");
            StringBuilder form = new StringBuilder();
            for (Map.Entry<String,String> field : fields.entrySet()) {
                if (form.length() != 0) form.append('&');
                form.append(URLEncoder.encode(field.getKey(), "UTF-8")).append('=')
                    .append(URLEncoder.encode(field.getValue(), "UTF-8"));
            }
            byte[] request = form.toString().getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(request.length);
            try {
                try (OutputStream output = connection.getOutputStream()) { output.write(request); }
                if (connection.getResponseCode() != 200) throw new IOException("License service is unavailable. Try again later.");
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                try (InputStream input = connection.getInputStream()) {
                    byte[] buffer = new byte[4096]; int size;
                    while ((size = input.read(buffer)) != -1) {
                        if (body.size() + size > 262144) throw new IOException("License response exceeded the allowed size.");
                        body.write(buffer, 0, size);
                    }
                }
                return new Response(body.toByteArray(), connection.getHeaderField("x-signature-timestamp"), connection.getHeaderField("x-signature-ed25519"));
            } finally { Arrays.fill(request, (byte)0); connection.disconnect(); }
        }
    }
}
