/*
 * Decompiled with CFR 0.152.
 */
package valen.utility;

public final class ResourceCounts {
    public final int diamonds;
    public final int emeralds;

    public ResourceCounts(int n, int n2) {
        if (n < 0 || n2 < 0 || n > 3456 || n2 > 3456) {
            throw new IllegalArgumentException("Invalid resource counts");
        }
        this.diamonds = n;
        this.emeralds = n2;
    }

    public boolean any() {
        return this.diamonds > 0 || this.emeralds > 0;
    }

    public boolean equals(Object object) {
        if (!(object instanceof ResourceCounts)) {
            return false;
        }
        ResourceCounts resourceCounts = (ResourceCounts)object;
        return this.diamonds == resourceCounts.diamonds && this.emeralds == resourceCounts.emeralds;
    }

    public int hashCode() {
        return this.diamonds * 37 + this.emeralds;
    }
}

