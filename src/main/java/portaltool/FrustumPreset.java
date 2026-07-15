package portaltool;

public enum FrustumPreset {
    QUAKE_1080P("Quake 1080p", 68.5),
    QUAKE_THIN("Quake thin", 23.5),
    FOV30_1080P("30fov 1080p", 25.5),
    FOV30_THIN("30fov thin", 4.7),
    EYEZOOM("eyezoom", 1.9);

    private final String label;
    private final double halfAngleDegrees;

    FrustumPreset(String label, double halfAngleDegrees) {
        this.label = label;
        this.halfAngleDegrees = halfAngleDegrees;
    }

    public String label() {
        return label;
    }

    public double halfAngleDegrees() {
        return halfAngleDegrees;
    }

    @Override
    public String toString() {
        return label;
    }
}
