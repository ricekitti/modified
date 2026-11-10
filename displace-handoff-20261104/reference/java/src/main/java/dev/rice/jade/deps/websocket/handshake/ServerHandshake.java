package dev.rice.jade.deps.websocket.handshake;

/** Rice host shim (type only) for the shaded Java-WebSocket server handshake. */
public interface ServerHandshake {
   short getHttpStatus();

   String getHttpStatusMessage();
}
