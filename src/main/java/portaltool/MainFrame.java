package portaltool;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public final class MainFrame extends JFrame {

    private final GridModel model = new GridModel();
    private final GridPanel gridPanel = new GridPanel(model);
    private Thread clipboardThread;
    private ClipboardMonitor clipboardMonitor;

    public MainFrame() {
        super("Portal Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(620, 680));
        getContentPane().setBackground(new Color(28, 28, 32));
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        stopClipboardMonitor();
                    }
                });

        pack();
        setLocationRelativeTo(null);
        startClipboardMonitor();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(22, 22, 26));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 65)));
        header.setPreferredSize(new Dimension(0, 44));

        JLabel title = new JLabel("Portal Tool");
        title.setForeground(new Color(230, 230, 235));
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        title.setBorder(new EmptyBorder(0, 16, 0, 0));
        header.add(title, BorderLayout.WEST);

        JComboBox<FrustumPreset> frustumSelector = new JComboBox<>(FrustumPreset.values());
        frustumSelector.setSelectedItem(model.getFrustumPreset());
        frustumSelector.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        frustumSelector.addActionListener(
                e -> {
                    FrustumPreset selected = (FrustumPreset) frustumSelector.getSelectedItem();
                    if (selected != null) {
                        model.setFrustumPreset(selected);
                        gridPanel.refresh();
                    }
                });
        JPanel settingsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        settingsRow.setBackground(new Color(22, 22, 26));
        settingsRow.add(frustumSelector);
        header.add(settingsRow, BorderLayout.CENTER);

        JButton closeButton = new JButton("X");
        closeButton.setForeground(new Color(200, 200, 205));
        closeButton.setBackground(new Color(22, 22, 26));
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setPreferredSize(new Dimension(44, 44));
        closeButton.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        header.add(closeButton, BorderLayout.EAST);

        return header;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(28, 28, 32));
        footer.setBorder(new EmptyBorder(8, 0, 12, 0));

        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        resetButton.setPreferredSize(new Dimension(120, 36));
        resetButton.addActionListener(
                e -> {
                    model.reset();
                    gridPanel.refresh();
                });

        JPanel resetRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        resetRow.setBackground(new Color(28, 28, 32));
        resetRow.add(resetButton);
        footer.add(resetRow, BorderLayout.CENTER);

        JLabel hint = new JLabel("Press F3+C in-game to add a line", SwingConstants.CENTER);
        hint.setForeground(new Color(140, 140, 148));
        hint.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        JPanel hintRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        hintRow.setBackground(new Color(28, 28, 32));
        hintRow.add(hint);
        footer.add(hintRow, BorderLayout.SOUTH);

        return footer;
    }

    private void startClipboardMonitor() {
        clipboardMonitor =
                new ClipboardMonitor(
                        reading -> {
                            model.addCapture(reading);
                            SwingUtilities.invokeLater(gridPanel::refresh);
                        });
        clipboardThread = new Thread(clipboardMonitor, "clipboard-monitor");
        clipboardThread.setDaemon(true);
        clipboardThread.start();
    }

    private void stopClipboardMonitor() {
        if (clipboardMonitor != null) {
            clipboardMonitor.stop();
        }
        if (clipboardThread != null) {
            clipboardThread.interrupt();
        }
    }
}
