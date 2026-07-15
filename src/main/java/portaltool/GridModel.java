package portaltool;

import java.util.ArrayList;
import java.util.List;

/**
 * 9×9 chunk grid centered on the starter 5-way scan position.
 * Top = South, right = West, bottom = North, left = East.
 * Subchunk (0,0) is the bottom-right of each cell; (16,16) is the top-left.
 */
public final class GridModel {

    public static final int SIZE = 9;
    public static final int CENTER = SIZE / 2;

    public record ScanLine(double originCol, double originRow, double angleDegrees) {}

    private final int[][] hitCounts = new int[SIZE][SIZE];
    private final List<ScanLine> scanLines = new ArrayList<>();
    private Integer anchorChunkX;
    private Integer anchorChunkZ;
    private FrustumPreset frustumPreset = FrustumPreset.QUAKE_1080P;

    public synchronized void addCapture(F3CParser.F3CReading reading) {
        int chunkX = F3CParser.chunkCoord(reading.x());
        int chunkZ = F3CParser.chunkCoord(reading.z());
        double subX = F3CParser.subchunkCoord(reading.x());
        double subZ = F3CParser.subchunkCoord(reading.z());

        if (anchorChunkX == null) {
            anchorChunkX = chunkX;
            anchorChunkZ = chunkZ;
        }

        int gridCol = CENTER - (chunkX - anchorChunkX);
        int gridRow = CENTER - (chunkZ - anchorChunkZ);
        if (gridCol < 0 || gridCol >= SIZE || gridRow < 0 || gridRow >= SIZE) {
            return;
        }

        double originCol = gridCol + (16.0 - subX) / 16.0;
        double originRow = gridRow + (16.0 - subZ) / 16.0;
        double angle = F3CParser.toGridAngle(reading.horizontalAngle());

        scanLines.add(new ScanLine(originCol, originRow, angle));
        markChunksInFrustum(originCol, originRow, angle, frustumPreset.halfAngleDegrees());
    }

    public synchronized void setFrustumPreset(FrustumPreset preset) {
        if (preset == null || preset == frustumPreset) {
            return;
        }
        frustumPreset = preset;
        recalculateHitCounts();
    }

    public synchronized FrustumPreset getFrustumPreset() {
        return frustumPreset;
    }

    private void recalculateHitCounts() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                hitCounts[row][col] = 0;
            }
        }
        double halfAngle = frustumPreset.halfAngleDegrees();
        for (ScanLine line : scanLines) {
            markChunksInFrustum(line.originCol(), line.originRow(), line.angleDegrees(), halfAngle);
        }
    }

    public synchronized void reset() {
        scanLines.clear();
        anchorChunkX = null;
        anchorChunkZ = null;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                hitCounts[row][col] = 0;
            }
        }
    }

    public synchronized List<ScanLine> getScanLines() {
        return List.copyOf(scanLines);
    }

    public synchronized int getHitCount(int row, int col) {
        return hitCounts[row][col];
    }

    public synchronized boolean hasAnchor() {
        return anchorChunkX != null;
    }

    private void markChunksInFrustum(
            double originCol, double originRow, double centerAngleDeg, double halfAngleDeg) {
        for (int col = 0; col < SIZE; col++) {
            for (int row = 0; row < SIZE; row++) {
                if (wedgeIntersectsCell(originCol, originRow, centerAngleDeg, halfAngleDeg, col, row)) {
                    hitCounts[row][col]++;
                }
            }
        }
    }

    private static boolean wedgeIntersectsCell(
            double originCol,
            double originRow,
            double centerAngleDeg,
            double halfAngleDeg,
            int cellCol,
            int cellRow) {
        if (originCol >= cellCol
                && originCol < cellCol + 1.0
                && originRow >= cellRow
                && originRow < cellRow + 1.0) {
            return true;
        }

        double minCol = cellCol;
        double maxCol = cellCol + 1.0;
        double minRow = cellRow;
        double maxRow = cellRow + 1.0;
        double[][] samplePoints = {
            {minCol, minRow},
            {maxCol, minRow},
            {maxCol, maxRow},
            {minCol, maxRow},
            {(minCol + maxCol) / 2.0, minRow},
            {(minCol + maxCol) / 2.0, maxRow},
            {minCol, (minRow + maxRow) / 2.0},
            {maxCol, (minRow + maxRow) / 2.0},
            {(minCol + maxCol) / 2.0, (minRow + maxRow) / 2.0}
        };

        for (double[] point : samplePoints) {
            if (pointInForwardWedge(
                    originCol, originRow, centerAngleDeg, halfAngleDeg, point[0], point[1])) {
                return true;
            }
        }

        double leftAngle = centerAngleDeg - halfAngleDeg;
        double rightAngle = centerAngleDeg + halfAngleDeg;
        double leftRadians = Math.toRadians(leftAngle);
        double rightRadians = Math.toRadians(rightAngle);
        double leftDirCol = Math.sin(leftRadians);
        double leftDirRow = -Math.cos(leftRadians);
        double rightDirCol = Math.sin(rightRadians);
        double rightDirRow = -Math.cos(rightRadians);

        return lineIntersectsCell(originCol, originRow, leftDirCol, leftDirRow, cellCol, cellRow)
                || lineIntersectsCell(originCol, originRow, rightDirCol, rightDirRow, cellCol, cellRow);
    }

    private static boolean pointInForwardWedge(
            double originCol,
            double originRow,
            double centerAngleDeg,
            double halfAngleDeg,
            double pointCol,
            double pointRow) {
        double deltaCol = pointCol - originCol;
        double deltaRow = pointRow - originRow;
        double centerRadians = Math.toRadians(centerAngleDeg);
        double forwardCol = Math.sin(centerRadians);
        double forwardRow = -Math.cos(centerRadians);
        if (deltaCol * forwardCol + deltaRow * forwardRow <= 1e-12) {
            return false;
        }
        double pointAngle = angleToPoint(originCol, originRow, pointCol, pointRow);
        return isAngleInWedge(pointAngle, centerAngleDeg, halfAngleDeg);
    }

    private static double angleToPoint(
            double originCol, double originRow, double pointCol, double pointRow) {
        double deltaCol = pointCol - originCol;
        double deltaRow = pointRow - originRow;
        return normalizeAngle(Math.toDegrees(Math.atan2(deltaCol, -deltaRow)));
    }

    private static boolean isAngleInWedge(double pointAngle, double centerAngle, double halfAngle) {
        double diff = normalizeAngle(pointAngle - centerAngle);
        if (diff > 180.0) {
            diff -= 360.0;
        }
        return Math.abs(diff) <= halfAngle + 1e-9;
    }

    private static double normalizeAngle(double degrees) {
        double wrapped = degrees % 360.0;
        if (wrapped < 0.0) {
            wrapped += 360.0;
        }
        return wrapped;
    }

    private static boolean lineIntersectsCell(
            double originCol,
            double originRow,
            double dirCol,
            double dirRow,
            int cellCol,
            int cellRow) {
        double minCol = cellCol;
        double maxCol = cellCol + 1.0;
        double minRow = cellRow;
        double maxRow = cellRow + 1.0;

        double tMin = 0.0;
        double tMax = Double.POSITIVE_INFINITY;

        if (Math.abs(dirCol) < 1e-12) {
            if (originCol < minCol || originCol >= maxCol) {
                return false;
            }
        } else {
            double t1 = (minCol - originCol) / dirCol;
            double t2 = (maxCol - originCol) / dirCol;
            double low = Math.min(t1, t2);
            double high = Math.max(t1, t2);
            tMin = Math.max(tMin, low);
            tMax = Math.min(tMax, high);
        }

        if (Math.abs(dirRow) < 1e-12) {
            if (originRow < minRow || originRow >= maxRow) {
                return false;
            }
        } else {
            double t1 = (minRow - originRow) / dirRow;
            double t2 = (maxRow - originRow) / dirRow;
            double low = Math.min(t1, t2);
            double high = Math.max(t1, t2);
            tMin = Math.max(tMin, low);
            tMax = Math.min(tMax, high);
        }

        return tMax >= Math.max(tMin, 0.0);
    }
}
