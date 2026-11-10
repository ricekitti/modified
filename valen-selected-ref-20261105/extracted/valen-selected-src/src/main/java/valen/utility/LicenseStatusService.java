/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public final class LicenseStatusService {
    public static final int URGENCY_CALM = 0;
    public static final int URGENCY_SOON = 1;
    public static final int URGENCY_CRITICAL = 2;
    private static final Result NOT_SHOWABLE = new Result(false, null, null, 0);
    private static final String STATUS_URL = "https://dynamic-elegance-production-4ef7.up.railway.app/api/v1/license/status";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final long MS_PER_DAY = 86400000L;
    private static volatile boolean started;
    private static volatile Result result;

    private LicenseStatusService() {
    }

    public static Result get() {
        return result;
    }

    public static void beginFetch(ExecutorService executorService) {
        if (started) {
            return;
        }
        started = true;
        executorService.submit(new Runnable(){

            @Override
            public void run() {
                LicenseStatusService.result = LicenseStatusService.fetch();
            }
        });
    }

    private static Result fetch() {
        try {
            String string = LicenseStatusService.readCachedKey();
            if (string == null || string.isEmpty()) {
                return NOT_SHOWABLE;
            }
            String string2 = LicenseStatusService.request(string);
            if (string2 == null) {
                return NOT_SHOWABLE;
            }
            Result result = LicenseStatusService.parse(string2);
            return result != null ? result : NOT_SHOWABLE;
        }
        catch (Exception exception) {
            return NOT_SHOWABLE;
        }
    }

    private static String readCachedKey() {
        try {
            File file = new File(System.getProperty("user.home"), ".valen_key.txt");
            if (!file.isFile()) {
                return null;
            }
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
        }
        catch (Exception exception) {
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String request(String string) {
        Object object;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection)new URL(STATUS_URL).openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(8000);
            httpURLConnection.setReadTimeout(10000);
            byte[] byArray = ("{\"key\":\"" + LicenseStatusService.escape(string) + "\"}").getBytes(StandardCharsets.UTF_8);
            object = httpURLConnection.getOutputStream();
            Object object2 = null;
            try {
                ((OutputStream)object).write(byArray);
            }
            catch (Throwable throwable) {
                object2 = throwable;
                throw throwable;
            }
            finally {
                if (object != null) {
                    if (object2 != null) {
                        try {
                            ((OutputStream)object).close();
                        }
                        catch (Throwable throwable) {
                            ((Throwable)object2).addSuppressed(throwable);
                        }
                    } else {
                        ((OutputStream)object).close();
                    }
                }
            }
            if (httpURLConnection.getResponseCode() != 200) {
                object = null;
                return object;
            }
            object = new StringBuilder();
            object2 = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8));
            Throwable throwable = null;
            try {
                int n;
                char[] cArray = new char[1024];
                while ((n = ((Reader)object2).read(cArray)) != -1) {
                    ((StringBuilder)object).append(cArray, 0, n);
                }
            }
            catch (Throwable throwable2) {
                throwable = throwable2;
                throw throwable2;
            }
            finally {
                if (object2 != null) {
                    if (throwable != null) {
                        try {
                            ((BufferedReader)object2).close();
                        }
                        catch (Throwable throwable3) {
                            throwable.addSuppressed(throwable3);
                        }
                    } else {
                        ((BufferedReader)object2).close();
                    }
                }
            }
            object2 = ((StringBuilder)object).toString();
            return object2;
        }
        catch (Exception exception) {
            object = null;
            return object;
        }
        finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    private static Result parse(String string) {
        if (!LicenseStatusService.extractBool(string, "valid")) {
            return null;
        }
        String string2 = LicenseStatusService.extractString(string, "expiresAt");
        if (string2 == null) {
            return null;
        }
        long l = LicenseStatusService.parseInstant(string2);
        if (l <= 0L) {
            return null;
        }
        long l2 = l - System.currentTimeMillis();
        if (l2 <= 0L) {
            return null;
        }
        long l3 = l2 / 86400000L;
        String string3 = l3 >= 1L ? l3 + (l3 == 1L ? " day left" : " days left") : "Expires today";
        String string4 = DATE_FORMAT.format(Instant.ofEpochMilli(l).atZone(ZoneId.systemDefault()));
        int n = l3 <= 3L ? 2 : (l3 <= 10L ? 1 : 0);
        return new Result(true, string3, "Expires " + string4, n);
    }

    private static long parseInstant(String string) {
        try {
            return Instant.parse(string).toEpochMilli();
        }
        catch (Exception exception) {
            try {
                String string2 = string.trim().replace(' ', 'T');
                if (!string2.endsWith("Z") && string2.indexOf(43) < 0) {
                    string2 = string2 + "Z";
                }
                return Instant.parse(string2).toEpochMilli();
            }
            catch (Exception exception2) {
                return 0L;
            }
        }
    }

    private static boolean extractBool(String string, String string2) {
        int n;
        int n2 = string.indexOf("\"" + string2 + "\"");
        if (n2 < 0) {
            return false;
        }
        int n3 = string.indexOf(58, n2 + string2.length() + 2);
        if (n3 < 0) {
            return false;
        }
        for (n = n3 + 1; n < string.length() && Character.isWhitespace(string.charAt(n)); ++n) {
        }
        return string.startsWith("true", n);
    }

    private static String extractString(String string, String string2) {
        int n;
        int n2 = string.indexOf("\"" + string2 + "\"");
        if (n2 < 0) {
            return null;
        }
        int n3 = string.indexOf(58, n2 + string2.length() + 2);
        if (n3 < 0) {
            return null;
        }
        for (n = n3 + 1; n < string.length() && Character.isWhitespace(string.charAt(n)); ++n) {
        }
        if (n >= string.length() || string.charAt(n) != '\"') {
            return null;
        }
        int n4 = n + 1;
        int n5 = string.indexOf(34, n4);
        if (n5 < 0) {
            return null;
        }
        String string3 = string.substring(n4, n5).trim();
        return string3.isEmpty() ? null : string3;
    }

    private static String escape(String string) {
        StringBuilder stringBuilder = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c == '\"' || c == '\\') {
                stringBuilder.append('\\').append(c);
                continue;
            }
            if (c < ' ') continue;
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static final class Result {
        public final boolean showable;
        public final String headline;
        public final String sub;
        public final int urgency;

        Result(boolean bl, String string, String string2, int n) {
            this.showable = bl;
            this.headline = string;
            this.sub = string2;
            this.urgency = n;
        }
    }
}

