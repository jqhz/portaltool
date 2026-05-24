package portaltool;

/**
 * Parses Minecraft F3+C clipboard text (tp command with position and look angles).
 */
public final class F3CParser {

    public record F3CReading(double x, double z, double horizontalAngle) {}

    private F3CParser() {}

    public static F3CReading tryParse(String text) {
        if (text == null || !text.startsWith("/execute in ")) {
            return null;
        }
        String[] parts = text.trim().split("\\s+");
        if (parts.length != 11) {
            return null;
        }
        try {
            double x = Double.parseDouble(parts[6]);
            double z = Double.parseDouble(parts[8]);
            double horizontalAngle = Double.parseDouble(parts[9]);
            return new F3CReading(x, z, horizontalAngle);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Subchunk position within a chunk; bottom-right of the cell is (0, 0), top-left is (16, 16). */
    public static double subchunkCoord(double worldCoord) {
        return ((worldCoord % 16.0) + 16.0) % 16.0;
    }

    public static int chunkCoord(double worldCoord) {
        return (int) Math.floor(worldCoord / 16.0);
    }

    /** Converts F3+C unwrapped yaw to 0–360° for South-based grid drawing. */
    public static double toGridAngle(double f3cHorizontalAngle) {
        double wrapped = f3cHorizontalAngle % 360.0;
        if (wrapped < 0) {
            wrapped += 360.0;
        }
        return wrapped;
    }
}
