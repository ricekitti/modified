package dev.rice.jade.deps.loader107;

import javax.net.ssl.SSLSocketFactory;

/**
 * Rice host shim: upstream resolves a platform SSL socket factory through Jade's recovered-bytecode VM
 * for the stats websocket. That socket's endpoint is redacted in OpenJade (it can never connect), and the
 * caller already handles null by keeping the default factory.
 */
public final class TlsSockets {
   private TlsSockets() {
   }

   public static SSLSocketFactory getPlatformSslSocketFactory() {
      return null;
   }
}
