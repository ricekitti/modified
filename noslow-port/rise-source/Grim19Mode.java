package rise.module.impl.movement.noslow;

import com.viaversion.viabackwards.protocol.v1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ServerboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import hackclient.rise.afi;
import net.minecraft.item.ai;
import net.minecraft.item.bg;
import net.minecraft.item.by;
import net.minecraft.item.cp;
import rise.event.EventLink;
import rise.event.Listener;
import rise.event.impl.PreMotionEvent;
import rise.event.impl.SlowDownEvent;
import rise.module.Mode;
import rise.module.impl.movement.NoSlow;

public class Grim19Mode
extends Mode<NoSlow> {
    int MM;
    @EventLink
    final public Listener<PreMotionEvent> MN = preMotionEvent -> {
        if (Grim19Mode.mc.thePlayer.isUsingItem() && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_9)) {
            UserConnection userConnection = Via.getManager().getConnectionManager().getConnections().iterator().next();
            if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19)) {
                PacketWrapper packetWrapper = PacketWrapper.create(ServerboundPackets1_19.USE_ITEM, userConnection);
                packetWrapper.write(Types.VAR_INT, 1);
                packetWrapper.write(Types.VAR_INT, Grim19Mode.mc.bfL.Ha());
                packetWrapper.sendToServer(Protocol1_19To1_18_2.class);
            } else {
                PacketWrapper packetWrapper = PacketWrapper.create(ServerboundPackets1_9.USE_ITEM, userConnection);
                packetWrapper.write(Types.VAR_INT, 1);
                packetWrapper.sendToServer(Protocol1_9To1_8.class);
            }
            ++this.MM;
        } else if (Grim19Mode.mc.thePlayer.isUsingItem() && Grim19Mode.mc.thePlayer.aQx % 5 == 0) {
            afi.b("use 1.9+ for this bypass", new Object[0]);
        } else {
            this.MM = 0;
        }
    };
    @EventLink
    final public Listener<SlowDownEvent> MO = slowDownEvent -> {
        if (((Boolean)((NoSlow)this.getParent()).DO.getValue()).booleanValue() && Grim19Mode.mc.thePlayer.isUsingItem() && Grim19Mode.mc.thePlayer.UW().bP() instanceof bg) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DP.getValue()).booleanValue() && Grim19Mode.mc.thePlayer.isUsingItem() && Grim19Mode.mc.thePlayer.UW().bP() instanceof by) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DQ.getValue()).booleanValue() && Grim19Mode.mc.thePlayer.isUsingItem() && Grim19Mode.mc.thePlayer.UW().bP() instanceof cp) {
            slowDownEvent.cF();
        }
        if (((Boolean)((NoSlow)this.getParent()).DR.getValue()).booleanValue() && Grim19Mode.mc.thePlayer.isUsingItem() && Grim19Mode.mc.thePlayer.UW().bP() instanceof ai) {
            slowDownEvent.cF();
        }
    };

    public Grim19Mode(String string, NoSlow noSlow) {
        super(string, noSlow);
    }
}


