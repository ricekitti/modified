package dev.rice.jade.deps.websocket.enums;

/** Rice host shim for the shaded Java-WebSocket ReadyState used by Jade's stats service. */
public enum ReadyState {
   NOT_YET_CONNECTED,
   OPEN,
   CLOSING,
   CLOSED;
}
