package portaltool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.JPanel;

public final class GridPanel extends JPanel {

    private static final Color GRID_LINE = new Color(80, 80, 80);
    private static final Color CENTER_FILL = new Color(55, 55, 60);
    private static final Color EMPTY_FILL = new Color(35, 35, 40);
    private static final Color LABEL_COLOR = new Color(170, 170, 175);
    private static final Color LINE_COLOR = new Color(120, 200, 255, 180);

    private final GridModel model;

    public GridPanel(GridModel model) {
        this.model = model;
        setBackground(new Color(28, 28, 32));
        setPreferredSize(new Dimension(540, 540));
    }

    public void refresh() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int pad = 28;
        int size = Math.min(getWidth(), getHeight()) - pad * 2;
        int cell = size / GridModel.SIZE;
        int originX = (getWidth() - cell * GridModel.SIZE) / 2;
        int originY = (getHeight() - cell * GridModel.SIZE) / 2;

        drawDirectionLabels(g2, originX, originY, cell, size);

        synchronized (model) {
            for (int row = 0; row < GridModel.SIZE; row++) {
                for (int col = 0; col < GridModel.SIZE; col++) {
                    int x = originX + col * cell;
                    int y = originY + row * cell;
                    g2.setColor(heatmapColor(model.getHitCount(row, col)));
                    g2.fillRect(x + 1, y + 1, cell - 1, cell - 1);
                }
            }

            List<GridModel.ScanLine> lines = model.getScanLines();
            if (!lines.isEmpty()) {
                double extent = cell * GridModel.SIZE * 1.5;
                g2.setStroke(new BasicStroke(2f));
                for (GridModel.ScanLine line : lines) {
                    double startX = originX + line.originCol() * cell;
                    double startY = originY + line.originRow() * cell;
                    double radians = Math.toRadians(line.angleDegrees());
                    double dx = Math.sin(radians) * extent;
                    double dy = -Math.cos(radians) * extent;
                    g2.setColor(LINE_COLOR);
                    g2.drawLine(
                            (int) Math.round(startX),
                            (int) Math.round(startY),
                            (int) Math.round(startX + dx),
                            (int) Math.round(startY + dy));
                }
            }
        }

        g2.setColor(GRID_LINE);
        for (int i = 0; i <= GridModel.SIZE; i++) {
            int x = originX + i * cell;
            int y = originY + i * cell;
            g2.drawLine(originX, y, originX + size, y);
            g2.drawLine(x, originY, x, originY + size);
        }

        if (model.hasAnchor()) {
            int centerX = originX + GridModel.CENTER * cell;
            int centerY = originY + GridModel.CENTER * cell;
            g2.setColor(CENTER_FILL);
            g2.fillRect(centerX + 1, centerY + 1, cell - 1, cell - 1);
            g2.setColor(GRID_LINE);
            g2.drawRect(centerX + 1, centerY + 1, cell - 2, cell - 2);
        }

        g2.dispose();
    }

    /** Yellow = 1 line, orange = 2, red = 3+. */
    private static Color heatmapColor(int hits) {
        if (hits <= 0) {
            return EMPTY_FILL;
        }
        if (hits == 1) {
            return new Color(255, 235, 80);
        }
        if (hits == 2) {
            return new Color(255, 150, 40);
        }
        int red = Math.min(255, 200 + (hits - 3) * 18);
        return new Color(red, 55, 45);
    }

    private void drawDirectionLabels(Graphics2D g2, int originX, int originY, int cell, int size) {
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g2.setColor(LABEL_COLOR);
        int midX = originX + size / 2;
        int midY = originY + size / 2;

        drawCenteredLabel(g2, "S", midX, originY - 10);
        drawCenteredLabel(g2, "N", midX, originY + size + 18);
        drawCenteredLabel(g2, "E", originX - 14, midY + 5);
        drawCenteredLabel(g2, "W", originX + size + 14, midY + 5);
    }

    private static void drawCenteredLabel(Graphics2D g2, String text, int centerX, int centerY) {
        int width = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, centerX - width / 2, centerY);
    }
}
