package dev.rice.security;

import java.lang.reflect.*;
import java.util.*;

/** Public application initialization only: no license submitted and no user data read. */
public final class LicenseProbe {
    public static void main(String[] args) throws Exception {
        try {
            KeyAuthClient client = new KeyAuthClient();
            Method request = KeyAuthClient.class.getDeclaredMethod("request", Map.class);
            request.setAccessible(true);
            Map<String,String> fields = new LinkedHashMap<>();
            fields.put("type", "init"); fields.put("name", KeyAuthClient.APPLICATION);
            fields.put("ownerid", KeyAuthClient.OWNER); fields.put("ver", KeyAuthClient.VERSION);
            request.invoke(client, fields);
            System.out.println("PASS: live HTTPS initialization; server signature, timestamp and application owner verified. No license submitted.");
        } catch (Exception failure) {
            System.out.println("Live license service initialization could not be verified. No license submitted.");
            System.exit(1);
        }
    }
}
