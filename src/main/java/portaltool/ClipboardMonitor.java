package portaltool;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Polls the system clipboard for new F3+C text, similar to Ninjabrain Bot's clipboard reader.
 */
public final class ClipboardMonitor implements Runnable {

    private static final int POLL_MS = 100;
    private static final int MAX_CLIPBOARD_LENGTH = 1000;

    private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    private final Consumer<F3CParser.F3CReading> onReading;
    private volatile String lastClipboard = "";
    private volatile boolean running = true;

    public ClipboardMonitor(Consumer<F3CParser.F3CReading> onReading) {
        this.onReading = onReading;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            String text = readClipboard();
            if (text != null && !text.equals(lastClipboard)) {
                F3CParser.F3CReading reading = F3CParser.tryParse(text);
                if (reading != null) {
                    lastClipboard = text;
                    onReading.accept(reading);
                }
            }
            try {
                Thread.sleep(POLL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private String readClipboard() {
        try {
            String text = (String) clipboard.getData(DataFlavor.stringFlavor);
            if (text.length() > MAX_CLIPBOARD_LENGTH) {
                return text.substring(0, MAX_CLIPBOARD_LENGTH);
            }
            return text;
        } catch (UnsupportedFlavorException | IllegalStateException | IOException ignored) {
            return null;
        }
    }
}
