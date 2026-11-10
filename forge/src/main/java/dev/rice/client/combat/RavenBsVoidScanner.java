package dev.rice.client.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import dev.rice.client.ClientContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.block.state.IBlockState;

/** Raven bS beta Void direction scan; debug rendering stays outside this adapter. */
public final class RavenBsVoidScanner {
    private static final int VOID_SCAN_DIRECTIONS = 48;
    private static final double VOID_SCAN_STEP = 0.5D;
    private static final double VOID_COLLISION_STEP = 0.25D;
    private static final double VOID_COLLISION_INSET = 0.03D;
    private static final double VOID_REFINEMENT_STEP = 1.5D;
    private static final double VOID_SCORE_EPSILON = 1.0E-4D;
    private final Minecraft mc;
    private boolean legitVoid;
    private float referenceYaw;
    private final double angle, radius;
    private final boolean ignoreOneBlockWall;
    private RavenBsVoidScanner(ClientContext c, double angle, double radius, boolean ignoreOneBlockWall) {
        this.mc = c.minecraft(); this.angle = angle; this.radius = radius;
        this.ignoreOneBlockWall = ignoreOneBlockWall;
    }
    public static Float findYaw(ClientContext c, Entity target, float playerYaw, double angle,
                                double radius, boolean ignoreOneBlockWall) {
        return findYaw(c, target, playerYaw, angle, radius, ignoreOneBlockWall, false);
    }
    public static Float findYaw(ClientContext c, Entity target, float playerYaw, double angle,
                                double radius, boolean ignoreOneBlockWall, boolean legitVoid) {
        if (!c.ready() || target == null) return null;
        RavenBsVoidScanner scanner = new RavenBsVoidScanner(c, angle, radius, ignoreOneBlockWall);
        scanner.legitVoid = legitVoid;
        scanner.referenceYaw = playerYaw;
        return scanner.findBestVoidYaw(target, playerYaw);
    }
    // 1.8.9 adapter for bS World.getCollisionBoxes: collect block shapes without entity collisions.
    private List<AxisAlignedBB> blockCollisions(AxisAlignedBB box) {
        List<AxisAlignedBB> collisions = new ArrayList<>();
        for (int x = MathHelper.floor_double(box.minX); x < MathHelper.floor_double(box.maxX + 1); x++) {
            for (int z = MathHelper.floor_double(box.minZ); z < MathHelper.floor_double(box.maxZ + 1); z++) {
                for (int y = MathHelper.floor_double(box.minY) - 1; y < MathHelper.floor_double(box.maxY + 1); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    state.getBlock().addCollisionBoxesToList(mc.theWorld, pos, state, box, collisions, null);
                }
            }
        }
        return collisions;
    }
    private Float findBestVoidYaw(Entity target, float playerYaw) {
        if (target == null || mc.thePlayer == null || mc.theWorld == null) {
            return null;
        }

        float offset = (float) angle;
        float preferredYawPositive = MathHelper.wrapAngleTo180_float(playerYaw + offset);
        float preferredYawNegative = MathHelper.wrapAngleTo180_float(playerYaw - offset);
        double scanAngleStep = 360.0D / (double) VOID_SCAN_DIRECTIONS;
        double scanDistance = radius;
        Map<Long, Boolean> voidColumns = new HashMap<>();
        Map<Long, VoidNeighborhood> voidNeighborhoods = new HashMap<>();
        VoidYawCandidate best = null;

        for (int directionIndex = 0; directionIndex < VOID_SCAN_DIRECTIONS; directionIndex++) {
            float candidateYaw = MathHelper.wrapAngleTo180_float(preferredYawPositive
                    + (float) (directionIndex * scanAngleStep));
            best = selectBetterVoidYaw(best, candidateYaw, preferredYawPositive, preferredYawNegative,
                    target, scanDistance,
                    voidColumns, voidNeighborhoods);
        }

        if (best == null) {
            return null;
        }

        float coarseYaw = best.yaw;
        for (double refinementOffset = -scanAngleStep + VOID_REFINEMENT_STEP;
             refinementOffset < scanAngleStep;
             refinementOffset += VOID_REFINEMENT_STEP) {
            if (Math.abs(refinementOffset) < VOID_SCORE_EPSILON) {
                continue;
            }
            float candidateYaw = MathHelper.wrapAngleTo180_float(coarseYaw + (float) refinementOffset);
            best = selectBetterVoidYaw(best, candidateYaw, preferredYawPositive, preferredYawNegative,
                    target, scanDistance,
                    voidColumns, voidNeighborhoods);
        }

        
        return best.yaw;
    }

    private VoidYawCandidate selectBetterVoidYaw(VoidYawCandidate currentBest, float candidateYaw,
                                                 float preferredYawPositive, float preferredYawNegative,
                                                 Entity target, double scanDistance,
                                                 Map<Long, Boolean> voidColumns,
                                                 Map<Long, VoidNeighborhood> voidNeighborhoods) {
        if (legitVoid && !LegitVoidPolicy.allows(referenceYaw, candidateYaw)) return currentBest;
        double radians = Math.toRadians(candidateYaw);
        double forwardX = -Math.sin(radians);
        double forwardZ = Math.cos(radians);
        double positiveDistance = Math.abs(MathHelper.wrapAngleTo180_float(
                candidateYaw - preferredYawPositive));
        double negativeDistance = Math.abs(MathHelper.wrapAngleTo180_float(
                candidateYaw - preferredYawNegative));
        double priorityDistance = Math.min(positiveDistance, negativeDistance);
        VoidPathScore pathScore = scoreVoidPath(target, forwardX, forwardZ, scanDistance,
                voidColumns, voidNeighborhoods);
        
        if (pathScore == null) {
            return currentBest;
        }

        VoidYawCandidate candidate = new VoidYawCandidate(candidateYaw, pathScore, priorityDistance);
        if (currentBest == null || candidate.compareTo(currentBest) > 0) {
            return candidate;
        }
        return currentBest;
    }

    private VoidPathScore scoreVoidPath(Entity target, double forwardX, double forwardZ,
                                        double scanDistance, Map<Long, Boolean> voidColumns,
                                        Map<Long, VoidNeighborhood> voidNeighborhoods) {
        double sideX = -forwardZ;
        double sideZ = forwardX;
        double sideOffset = Math.max(0.2D, (double) target.width * 0.45D);
        double checkedForward = 0.0D;
        double pathQuality = 0.0D;
        int consecutiveCenterVoid = 0;
        int longestCenterVoidRun = 0;
        int bestNeighborhoodQuality = -1;
        int bestDestinationWidth = 0;
        double bestDestinationDistance = Double.MAX_VALUE;
        double bestDestinationX = 0.0D;
        double bestDestinationZ = 0.0D;
        VoidNeighborhood bestNeighborhood = null;
        AxisAlignedBB collisionBox = target.getEntityBoundingBox().contract(
                VOID_COLLISION_INSET, 0.001D, VOID_COLLISION_INSET);

        for (double forward = VOID_SCAN_STEP;
             forward <= scanDistance + VOID_SCORE_EPSILON;
             forward += VOID_SCAN_STEP) {
            double blockedDistance = getVoidPathBlockedDistance(collisionBox, forwardX, forwardZ,
                    checkedForward, forward);
            if (blockedDistance >= 0.0D) {
                
                break;
            }
            checkedForward = forward;
            

            double centerX = target.posX + forwardX * forward;
            double centerZ = target.posZ + forwardZ * forward;
            boolean centerVoid = isVoidColumn(centerX, target.posY, centerZ, voidColumns);
            boolean leftVoid = isVoidColumn(centerX - sideX * sideOffset, target.posY,
                    centerZ - sideZ * sideOffset, voidColumns);
            boolean rightVoid = isVoidColumn(centerX + sideX * sideOffset, target.posY,
                    centerZ + sideZ * sideOffset, voidColumns);
            double distanceWeight = scanDistance + VOID_SCAN_STEP - forward;

            

            if (leftVoid) {
                pathQuality += distanceWeight;
            }
            if (rightVoid) {
                pathQuality += distanceWeight;
            }
            if (centerVoid) {
                consecutiveCenterVoid++;
                longestCenterVoidRun = Math.max(longestCenterVoidRun, consecutiveCenterVoid);
                pathQuality += distanceWeight * 1.6D + (double) consecutiveCenterVoid * 2.0D;

                VoidNeighborhood neighborhood = getVoidNeighborhood(centerX, target.posY, centerZ,
                        voidColumns, voidNeighborhoods);
                int neighborhoodQuality = neighborhood.quality;
                int destinationWidth = 1 + (leftVoid ? 1 : 0) + (rightVoid ? 1 : 0);
                if (neighborhoodQuality > bestNeighborhoodQuality
                        || (neighborhoodQuality == bestNeighborhoodQuality
                        && destinationWidth > bestDestinationWidth)
                        || (neighborhoodQuality == bestNeighborhoodQuality
                        && destinationWidth == bestDestinationWidth
                        && forward < bestDestinationDistance)) {
                    bestNeighborhoodQuality = neighborhoodQuality;
                    bestDestinationWidth = destinationWidth;
                    bestDestinationDistance = forward;
                    bestDestinationX = centerX;
                    bestDestinationZ = centerZ;
                    bestNeighborhood = neighborhood;
                }
            } else {
                consecutiveCenterVoid = 0;
            }
        }

        if (bestNeighborhoodQuality < 0) {
            return null;
        }

        return new VoidPathScore(bestNeighborhood.immediateVoidNeighbors,
                bestNeighborhood.extendedVoidNeighbors, bestDestinationWidth,
                longestCenterVoidRun, pathQuality, bestDestinationDistance,
                bestDestinationX, bestDestinationZ, bestNeighborhood.blockX, bestNeighborhood.blockZ,
                bestNeighborhood.voidMask);
    }

    private VoidNeighborhood getVoidNeighborhood(double x, double y, double z,
                                                 Map<Long, Boolean> voidColumns,
                                                 Map<Long, VoidNeighborhood> voidNeighborhoods) {
        int blockX = MathHelper.floor_double(x);
        int blockZ = MathHelper.floor_double(z);
        long columnKey = getColumnKey(blockX, blockZ);
        VoidNeighborhood cached = voidNeighborhoods.get(columnKey);
        if (cached != null) {
            return cached;
        }

        int immediateVoidNeighbors = 0;
        int extendedVoidNeighbors = 0;
        long voidMask = 0L;
        for (int offsetX = -2; offsetX <= 2; offsetX++) {
            for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
                int radius = Math.max(Math.abs(offsetX), Math.abs(offsetZ));
                if (radius == 0) {
                    continue;
                }

                if (isVoidColumn(blockX + offsetX + 0.5D, y, blockZ + offsetZ + 0.5D, voidColumns)) {
                    int bitIndex = (offsetX + 2) * 5 + offsetZ + 2;
                    voidMask |= 1L << bitIndex;
                    if (radius == 1) {
                        immediateVoidNeighbors++;
                    } else {
                        extendedVoidNeighbors++;
                    }
                }
            }
        }

        int quality = immediateVoidNeighbors * 100 + extendedVoidNeighbors;
        VoidNeighborhood neighborhood = new VoidNeighborhood(blockX, blockZ, immediateVoidNeighbors,
                extendedVoidNeighbors, quality, voidMask);
        voidNeighborhoods.put(columnKey, neighborhood);
        return neighborhood;
    }

    private double getVoidPathBlockedDistance(AxisAlignedBB collisionBox, double forwardX, double forwardZ,
                                               double fromForward, double toForward) {
        for (double forward = fromForward + VOID_COLLISION_STEP;
             forward <= toForward + VOID_SCORE_EPSILON;
             forward += VOID_COLLISION_STEP) {
            AxisAlignedBB checkBox = collisionBox.offset(forwardX * forward, 0.0D, forwardZ * forward);
            BlockPos minCorner = new BlockPos(checkBox.minX, checkBox.minY, checkBox.minZ);
            BlockPos maxCorner = new BlockPos(checkBox.maxX, checkBox.maxY, checkBox.maxZ);
            if (!mc.theWorld.isBlockLoaded(minCorner) || !mc.theWorld.isBlockLoaded(maxCorner)) {
                return forward;
            }
            List<AxisAlignedBB> blockCollisions = blockCollisions(checkBox);
            if (!blockCollisions.isEmpty() && (!ignoreOneBlockWall
                    || !isOneBlockWall(checkBox, blockCollisions))) {
                return forward;
            }
        }
        return -1.0D;
    }

    private boolean isOneBlockWall(AxisAlignedBB checkBox, List<AxisAlignedBB> blockCollisions) {
        double maximumWallY = checkBox.minY + 1.0D + VOID_SCORE_EPSILON;
        for (AxisAlignedBB collision : blockCollisions) {
            if (collision.maxY > maximumWallY) {
                return false;
            }
        }
        return true;
    }

    private boolean isVoidColumn(double x, double y, double z, Map<Long, Boolean> voidColumns) {
        int blockX = MathHelper.floor_double(x);
        int blockZ = MathHelper.floor_double(z);
        long columnKey = getColumnKey(blockX, blockZ);
        Boolean cached = voidColumns.get(columnKey);
        if (cached != null) {
            return cached;
        }

        int startY = Math.min(255, MathHelper.floor_double(y - 0.01D));
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(blockX, Math.max(0, startY), blockZ);
        if (!mc.theWorld.isBlockLoaded(blockPos)) {
            voidColumns.put(columnKey, false);
            return false;
        }

        for (int blockY = startY; blockY >= 0; blockY--) {
            blockPos.set(blockX, blockY, blockZ);
            if (!mc.theWorld.isAirBlock(blockPos)) {
                voidColumns.put(columnKey, false);
                return false;
            }
        }

        voidColumns.put(columnKey, true);
        return true;
    }

    private long getColumnKey(int blockX, int blockZ) {
        return ((long) blockX << 32) ^ ((long) blockZ & 0xffffffffL);
    }


    private static final class VoidYawCandidate {
        private final float yaw;
        private final VoidPathScore pathScore;
        private final double priorityDistance;

        VoidYawCandidate(float yaw, VoidPathScore pathScore, double priorityDistance) {
            this.yaw = yaw;
            this.pathScore = pathScore;
            this.priorityDistance = priorityDistance;
        }

        private int compareTo(VoidYawCandidate other) {
            int pathComparison = pathScore.compareTo(other.pathScore);
            if (pathComparison != 0) {
                return pathComparison;
            }
            if (Math.abs(priorityDistance - other.priorityDistance) > VOID_SCORE_EPSILON) {
                return priorityDistance < other.priorityDistance ? 1 : -1;
            }
            return 0;
        }
    }

    private static final class VoidPathScore {
        private final int immediateVoidNeighbors;
        private final int extendedVoidNeighbors;
        private final int destinationWidth;
        private final int longestCenterVoidRun;
        private final double pathQuality;
        private final double destinationDistance;
        private final double destinationX;
        private final double destinationZ;
        private final int destinationBlockX;
        private final int destinationBlockZ;
        private final long destinationVoidMask;

        VoidPathScore(int immediateVoidNeighbors, int extendedVoidNeighbors, int destinationWidth,
                              int longestCenterVoidRun, double pathQuality, double destinationDistance,
                              double destinationX, double destinationZ, int destinationBlockX,
                              int destinationBlockZ, long destinationVoidMask) {
            this.immediateVoidNeighbors = immediateVoidNeighbors;
            this.extendedVoidNeighbors = extendedVoidNeighbors;
            this.destinationWidth = destinationWidth;
            this.longestCenterVoidRun = longestCenterVoidRun;
            this.pathQuality = pathQuality;
            this.destinationDistance = destinationDistance;
            this.destinationX = destinationX;
            this.destinationZ = destinationZ;
            this.destinationBlockX = destinationBlockX;
            this.destinationBlockZ = destinationBlockZ;
            this.destinationVoidMask = destinationVoidMask;
        }

        private int compareTo(VoidPathScore other) {
            if (immediateVoidNeighbors != other.immediateVoidNeighbors) {
                return Integer.compare(immediateVoidNeighbors, other.immediateVoidNeighbors);
            }
            if (extendedVoidNeighbors != other.extendedVoidNeighbors) {
                return Integer.compare(extendedVoidNeighbors, other.extendedVoidNeighbors);
            }
            if (destinationWidth != other.destinationWidth) {
                return Integer.compare(destinationWidth, other.destinationWidth);
            }
            if (longestCenterVoidRun != other.longestCenterVoidRun) {
                return Integer.compare(longestCenterVoidRun, other.longestCenterVoidRun);
            }
            if (Math.abs(pathQuality - other.pathQuality) > VOID_SCORE_EPSILON) {
                return pathQuality > other.pathQuality ? 1 : -1;
            }
            if (Math.abs(destinationDistance - other.destinationDistance) > VOID_SCORE_EPSILON) {
                return destinationDistance < other.destinationDistance ? 1 : -1;
            }
            return 0;
        }
    }

    private static final class VoidNeighborhood {
        private final int blockX;
        private final int blockZ;
        private final int immediateVoidNeighbors;
        private final int extendedVoidNeighbors;
        private final int quality;
        private final long voidMask;

        VoidNeighborhood(int blockX, int blockZ, int immediateVoidNeighbors,
                                 int extendedVoidNeighbors, int quality, long voidMask) {
            this.blockX = blockX;
            this.blockZ = blockZ;
            this.immediateVoidNeighbors = immediateVoidNeighbors;
            this.extendedVoidNeighbors = extendedVoidNeighbors;
            this.quality = quality;
            this.voidMask = voidMask;
        }
    }
}
