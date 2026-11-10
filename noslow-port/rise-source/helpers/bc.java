package hackclient.rise;

import hackclient.rise.afi;
import hackclient.rise.ahj;
import hackclient.rise.am;
import hackclient.rise.fa;
import hackclient.rise.fg;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import net.minecraft.network.aj;
import net.minecraft.network.handshake.client.a;
import net.minecraft.network.play.client.e;
import net.minecraft.network.status.client.b;
import rise.Client;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PacketSendEvent;

public final class bc
extends am {
    private static boolean cP = true;
    final public static ConcurrentLinkedQueue<aj<?>> cQ = new ConcurrentLinkedQueue();
    public static boolean cR;
    public static boolean cS;
    public static ArrayList<Class<?>> cT;
    public static rip.vantage.commons.util.time.a cU;
    @EventLink(priority=0)
    final public Listener<PacketSendEvent> cV = packetSendEvent -> {
        if (bc.mc.thePlayer == null) {
            cQ.clear();
            cT.clear();
            return;
        }
        if (bc.mc.thePlayer.aQx < 10 && cP) {
            Client.INSTANCE.p().to();
            cP = false;
        }
        if (bc.mc.thePlayer.crt || mc.isSingleplayer() || !bc.mc.AG().bEh) {
            cQ.forEach((Consumer<aj<?>>)((Consumer<aj>)ahj::m));
            cQ.clear();
            cR = false;
            cT.clear();
            if (bc.mc.thePlayer.aQx < 10 && cP) {
                Client.INSTANCE.p().to();
                cP = false;
            }
            return;
        }
        aj<?> aj2 = packetSendEvent.dq();
        if (aj2 instanceof a || aj2 instanceof net.minecraft.network.login.client.a || aj2 instanceof net.minecraft.network.status.client.a || aj2 instanceof b || aj2 instanceof net.minecraft.network.login.client.b) {
            return;
        }
        if (cR && !cS) {
            if (cU.T(100L)) {
                cU.aX();
                cT.clear();
            }
            if (!packetSendEvent.isCancelled() && cT.stream().noneMatch(clazz -> clazz == aj2.getClass())) {
                cQ.add(aj2);
                packetSendEvent.cF();
            }
        } else if (aj2 instanceof e) {
            cQ.forEach((Consumer<aj<?>>)((Consumer<aj>)ahj::m));
            cQ.clear();
            cS = false;
        }
    };
    @EventLink(priority=0)
    final public Listener<fg> cW = fg2 -> {
        cQ.clear();
        cR = false;
    };
    @EventLink(priority=0)
    final public Listener<fa> cX = fa2 -> {
        afi.b("s", new Object[0]);
        cQ.clear();
        cR = false;
    };

    public static void a(Class<?> ... classArray) {
        cT = new ArrayList(Arrays.asList(classArray));
        cU.aX();
    }

    public static void dispatch() {
        cS = true;
    }

    static {
        cT = new ArrayList();
        cU = new rip.vantage.commons.util.time.a();
    }
}


