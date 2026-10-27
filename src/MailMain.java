import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MailMain {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MailModel model = new MailModel();
            MailView view = new MailView();
            new MailController(view, model);
            view.setVisible(true);
        });
    }
}