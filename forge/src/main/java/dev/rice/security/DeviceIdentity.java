package dev.rice.security;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;

public final class DeviceIdentity {
    private DeviceIdentity() {}
    public static String current() throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT), material;
        if (os.contains("win")) {
            String output = read("reg.exe", "query", "HKLM\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid", "/reg:64");
            Matcher match = Pattern.compile("MachineGuid\\s+REG_SZ\\s+([a-fA-F0-9-]+)").matcher(output);
            String name = System.getenv("COMPUTERNAME");
            if (!match.find() || name == null || name.isEmpty()) throw new IOException("Device identity unavailable.");
            // Matches the existing Windows launcher's SHA256(MachineGuid|ComputerName).
            material = match.group(1) + "|" + name;
        } else if (os.contains("mac")) {
            String output = read("/usr/sbin/ioreg", "-rd1", "-c", "IOPlatformExpertDevice");
            Matcher match = Pattern.compile("\"IOPlatformUUID\"\\s*=\\s*\"([a-fA-F0-9-]+)\"").matcher(output);
            if (!match.find()) throw new IOException("Device identity unavailable.");
            material = "rice-mac|" + match.group(1);
        } else throw new IOException("This release supports Windows and macOS.");
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        for (byte value : digest) text.append(String.format(Locale.ROOT, "%02x", value & 255));
        return text.toString();
    }
    private static String read(String... command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) throw new IOException("Device identity timed out.");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (InputStream input = process.getInputStream()) {
                byte[] buffer = new byte[1024]; int size;
                while ((size = input.read(buffer)) != -1) {
                    if (output.size() + size > 32768) throw new IOException("Device identity output was too large.");
                    output.write(buffer, 0, size);
                }
            }
            if (process.exitValue() != 0) throw new IOException("Device identity unavailable.");
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        } finally { process.destroy(); }
    }
}
