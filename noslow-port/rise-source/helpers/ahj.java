package hackclient.rise;

import com.alan.clients.script.api.NetworkAPI;
import java.util.Arrays;
import lombok.Generated;
import net.minecraft.client.network.h;
import net.minecraft.network.aj;
import rise.event.impl.PacketReceiveEvent;
import rise.util.InstanceAccess;

public final class ahj
implements InstanceAccess {
    private static h uL() {
        return mc != null ? mc.AG() : null;
    }

    public static void l(aj<?> aj2) {
        h h2 = ahj.uL();
        if (h2 != null && aj2 != null) {
            h2.t(aj2);
        }
    }

    public static void m(aj<?> aj2) {
        h h2 = ahj.uL();
        if (h2 != null && aj2 != null) {
            h2.u(aj2);
        }
    }

    public static void n(aj<?> aj2) {
        if (aj2 == null) {
            System.out.println("Packet is null");
            return;
        }
        h h2 = ahj.uL();
        if (h2 == null) {
            return;
        }
        if (ahj.s(aj2)) {
            h2.t(aj2);
        } else {
            h2.v(aj2);
        }
    }

    public static void o(aj<?> aj2) {
        h h2 = ahj.uL();
        if (h2 == null || aj2 == null) {
            return;
        }
        if (ahj.s(aj2)) {
            h2.u(aj2);
        } else {
            h2.w(aj2);
        }
    }

    public static void p(aj<?> aj2) {
        h h2 = ahj.uL();
        if (h2 != null && aj2 != null) {
            h2.v(aj2);
        }
    }

    public static void q(aj<?> aj2) {
        h h2 = ahj.uL();
        if (h2 != null && aj2 != null) {
            h2.w(aj2);
        }
    }

    public static boolean r(aj<?> aj2) {
        return !ahj.s(aj2);
    }

    public static boolean s(aj<?> aj2) {
        return Arrays.stream(NetworkAPI.serverbound).anyMatch(clazz -> clazz == aj2.getClass());
    }

    public static void j(PacketReceiveEvent packetReceiveEvent) {
        if (!ahj.mc.thePlayer.crt) {
            // empty if block
        }
    }

    @Generated
    private ahj() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}


