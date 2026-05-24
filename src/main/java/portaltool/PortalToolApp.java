package portaltool;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class PortalToolApp {

    private PortalToolApp() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(
                () -> {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception ignored) {
                    }
                    new MainFrame().setVisible(true);
                });
    }
}
