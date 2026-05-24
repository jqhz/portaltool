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
        markChunksAlongLine(originCol, originRow, angle);
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

    private void markChunksAlongLine(double originCol, double originRow, double angleDeg) {
        double radians = Math.toRadians(angleDeg);
        double dirCol = Math.sin(radians);
        double dirRow = -Math.cos(radians);

        for (int col = 0; col < SIZE; col++) {
            for (int row = 0; row < SIZE; row++) {
                if (lineIntersectsCell(originCol, originRow, dirCol, dirRow, col, row)) {
                    hitCounts[row][col]++;
                }
            }
        }
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
