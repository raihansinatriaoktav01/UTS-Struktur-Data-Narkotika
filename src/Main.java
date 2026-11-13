import controller.PutusanController;
import view.MainFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        PutusanController controller = new PutusanController();

        SwingUtilities.invokeLater(() -> {
            MainFrame gui = new MainFrame(controller);
            gui.setVisible(true);
        });
    }
}public class Main {
}
