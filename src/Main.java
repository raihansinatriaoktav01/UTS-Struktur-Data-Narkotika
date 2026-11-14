import controller.PutusanController;
import view.MainFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        PutusanController controller = new PutusanController();

        // Aplikasi dimulai dengan struktur kosong.
        // Data dimuat lewat tombol "Impor CSV" di GUI.

        SwingUtilities.invokeLater(() -> {
            MainFrame gui = new MainFrame(controller);
            gui.setVisible(true);
        });
    }
}