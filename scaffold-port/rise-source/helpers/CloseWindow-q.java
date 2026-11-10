package net.minecraft.network.play.client;

import net.minecraft.network.aj;
import net.minecraft.network.ak;
import net.minecraft.network.g;
import net.minecraft.network.play.b;

public class q
implements aj<b> {
    private int cSd;

    public q() {
    }

    public q(int n2) {
        this.cSd = n2;
    }

    @Override
    public void a(b b2) {
        b2.a(this);
    }

    @Override
    public void a(ak ak2) {
        this.cSd = ak2.readByte();
    }

    @Override
    public void b(ak ak2) {
        ak2.writeByte(this.cSd);
    }

    @Override
    public void a(g g2) {
        this.a((b)g2);
    }
}


