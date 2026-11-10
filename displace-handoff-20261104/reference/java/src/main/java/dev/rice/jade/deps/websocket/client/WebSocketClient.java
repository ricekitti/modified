package dev.rice.jade.deps.websocket.client;

import dev.rice.jade.deps.websocket.enums.ReadyState;
import dev.rice.jade.deps.websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;

/**
 * Rice host shim for the shaded Java-WebSocket client used only by Jade's stats service. OpenJade's
 * endpoint is literally {@code wss://redacted/overlay}, which can never resolve, so upstream always ends in
 * an error + close. This shim reports that same outcome synchronously and never touches the network
 * (no DNS lookup, no socket), so stats lookups fail exactly as they do upstream.
 */
public abstract class WebSocketClient {
   private final URI uri;
   private volatile ReadyState readyState = ReadyState.NOT_YET_CONNECTED;

   protected WebSocketClient(URI uri) {
      this.uri = uri;
   }

   public abstract void onOpen(ServerHandshake handshake);

   public abstract void onMessage(String message);

   public abstract void onClose(int code, String reason, boolean remote);

   public abstract void onError(Exception error);

   public void setSocketFactory(SSLSocketFactory factory) {
   }

   public void connect() {
      this.readyState = ReadyState.CLOSED;
      this.onError(new UnknownHostException(this.uri == null ? "redacted" : String.valueOf(this.uri.getHost())));
      this.onClose(-1, "unresolvable host", false);
   }

   public boolean isOpen() {
      return this.readyState == ReadyState.OPEN;
   }

   public ReadyState getReadyState() {
      return this.readyState;
   }

   public void send(String text) {
      throw new IllegalStateException("websocket is not open");
   }

   public void close() {
      this.readyState = ReadyState.CLOSED;
   }
}
