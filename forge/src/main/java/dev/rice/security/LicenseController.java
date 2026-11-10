package dev.rice.security;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LicenseController {
    private final KeyAuthClient api = new KeyAuthClient();
    private final AtomicBoolean busy = new AtomicBoolean();
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "Rice-license"); thread.setDaemon(true); return thread;
    });
    private volatile String status = "Enter your license key to unlock Rice.";
    public LicenseController() {
        worker.scheduleWithFixedDelay(() -> {
            KeyAuthClient.Session session = LicenseRuntime.session();
            if (session == null) return;
            try { api.check(session); LicenseRuntime.refresh(session); }
            catch (Exception failure) {
                LicenseRuntime.revoke(); status = "Session expired or unavailable. Please sign in again.";
            }
        }, 60, 60, TimeUnit.SECONDS);
    }
    public boolean busy() { return busy.get(); }
    public String status() { return status; }
    public void authenticate(final String license) {
        if (!busy.compareAndSet(false, true)) return;
        status = "Checking license...";
        worker.execute(() -> {
            try {
                KeyAuthClient.Session session = api.authenticate(license, DeviceIdentity.current());
                LicenseRuntime.activate(session); status = "License accepted.";
            } catch (Exception failure) {
                LicenseRuntime.revoke();
                status = failure instanceof SecurityException ? failure.getMessage() : "Could not verify your license. Check your connection and try again.";
            } finally { busy.set(false); }
        });
    }
}
