/*
 * Decompiled with CFR 0.152.
 */
package hackclient.rise;

import hackclient.rise.a;
import hackclient.rise.adx;
import hackclient.rise.ahj;
import hackclient.rise.aif;
import hackclient.rise.dk;
import hackclient.rise.dm;
import hackclient.rise.ed;
import hackclient.rise.ei;
import hackclient.rise.jg;
import hackclient.rise.tn;
import net.minecraft.network.play.client.e;

public final class vo
implements adx {
    private tn ajR;
    static final private double ajS = 0.001;
    private boolean ajT;
    @dm(cH=3)
    public final dk<ei> ajU = ei2 -> {
        if (!this.kE()) {
            this.unregister();
            return;
        }
        if (ei2.isOnGround() || vo.aEi.bgb.aO) {
            if (!((Boolean)this.e(jg.class).wu.wp()).booleanValue() || !this.e(jg.class).isEnabled()) {
                if (vo.aEi.bgb.aQx % 3 == 0 && vo.aEi.bgb.cqP > 2 && !vo.aEi.aIp.cgI.QQ()) {
                    if (this.e(tn.class).isEnabled() && !vo.aEi.aIp.cgM.QQ() && !vo.aEi.aIp.cgJ.QQ()) {
                        ei2.setPosY(ei2.getPosY() + 0.001);
                    }
                } else if (this.e(tn.class).isEnabled()) {
                    // empty if block
                }
            }
            this.ajT = true;
        } else if (!this.ajR.isEnabled()) {
            this.unregister();
        }
        if (this.e(tn.class).isEnabled() && vo.aEi.aIp.cgJ.QS() && vo.aEi.bgb.aQx % 3 == 1) {
            vo.aEi.aIp.cgJ.ay(false);
        }
        if (this.e(tn.class).isEnabled() && vo.aEi.aIp.cgJ.QS()) {
            aif.stop();
            vo.aEi.bfT.dzH = 0.5f;
            ahj.l(new e(vo.aEi.bgb.aO));
        }
    };
    @dm(cH=4)
    public final dk<ed> ajV = ed2 -> {
        if (vo.aEi.bgb.aQx % 3 == 1 && vo.aEi.bgb.cqP > 2 && this.e(tn.class).isEnabled()) {
            ed2.cF();
        }
    };

    public vo(tn tn2) {
        this.ajR = tn2;
    }

    private void unregister() {
        a.a.e().c(this);
        this.ajT = false;
    }

    private boolean kE() {
        return this.ajR.isEnabled() || this.ajT;
    }

    public void onEnable() {
        a.a.e().b(this);
    }
}


