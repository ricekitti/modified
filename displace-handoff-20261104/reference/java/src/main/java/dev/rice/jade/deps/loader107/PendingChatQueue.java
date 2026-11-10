package dev.rice.jade.deps.loader107;

public final class PendingChatQueue {
   private static final Object QUEUE_LOCK = new Object();

   public static void enqueueMessage(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         String var1 = var0.indexOf(1) >= 0 ? var0.replace('\u0001', ' ') : var0;
         synchronized (QUEUE_LOCK) {
            String var3 = System.getProperty("jade.chat.queue", "");
            String var4 = var3.isEmpty() ? var1 : var3 + '\u0001' + var1;
            if (var4.length() > 16000) {
               while (var4.length() > 16000) {
                  int var5 = var4.indexOf(1);
                  if (var5 < 0) {
                     var4 = var4.substring(var4.length() - 16000);
                     break;
                  }

                  var4 = var4.substring(var5 + 1);
               }
            }

            System.setProperty("jade.chat.queue", var4);
         }
      }
   }
}
