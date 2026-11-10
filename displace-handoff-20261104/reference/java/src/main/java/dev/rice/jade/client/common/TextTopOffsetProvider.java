// Jade recovery: original class: jade.deps.eLz.KrlqpJ
package dev.rice.jade.client.common;

public interface TextTopOffsetProvider {
   default int getTextTopOffset() {
      return ComponentTextMetrics.top();
   }
}
