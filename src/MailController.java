import javax.swing.*;
import java.util.List;

public class MailController {
    private MailView view;
    private MailModel model;

    public MailController(MailView view, MailModel model) {
        this.view = view;
        this.model = model;

        this.view.getBtnSend().addActionListener(e -> handleSendEmail());
        this.view.getBtnFetch().addActionListener(e -> handleFetchEmail());
    }

    private void handleSendEmail() {
        // 1. Lấy Tài khoản & Mật khẩu từ thanh cấu hình phía trên
        String myEmail = view.getMyEmail();
        String appPassword = view.getAppPassword();

        if (myEmail.isEmpty() || appPassword.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập Email và Mật khẩu ứng dụng của bạn ở thanh trên cùng!", "Lỗi thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Lấy thông tin người nhận
        String to = view.getTo();
        String subject = view.getSubject();
        String content = view.getContent();

        if (to.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập Email người nhận!", "Lỗi thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        view.getBtnSend().setEnabled(false);
        view.setSmtpLog("Đang kết nối đến smtp.gmail.com...\n");

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                try {
                    // 3. Truyền đủ tham số (Tài khoản, Mật khẩu) xuống Model
                    model.sendEmail(myEmail, appPassword, to, subject, content, this::publish);
                    publish("\n=> THÀNH CÔNG! Đã gửi mail tới " + to);
                } catch (Exception ex) {
                    publish("\n[LỖI]: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String text : chunks) {
                    view.appendSmtpLog(text);
                }
            }

            @Override
            protected void done() {
                view.getBtnSend().setEnabled(true);
            }
        };
        worker.execute();
    }

    private void handleFetchEmail() {
        // 1. Lấy Tài khoản & Mật khẩu từ thanh cấu hình phía trên
        String myEmail = view.getMyEmail();
        String appPassword = view.getAppPassword();

        if (myEmail.isEmpty() || appPassword.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập Email và Mật khẩu ứng dụng của bạn ở thanh trên cùng!", "Lỗi thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        view.getBtnFetch().setEnabled(false);
        view.setPop3Log("Đang kết nối đến pop.gmail.com...\n");

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                try {
                    // 2. Truyền đủ tham số (Tài khoản, Mật khẩu) xuống Model
                    model.fetchLatestEmail(myEmail, appPassword, this::publish);
                    publish("\n=> THÀNH CÔNG! Đã đọc xong thư.");
                } catch (Exception ex) {
                    publish("\n[LỖI]: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String text : chunks) {
                    view.appendPop3Log(text);
                }
            }

            @Override
            protected void done() {
                view.getBtnFetch().setEnabled(true);
            }
        };
        worker.execute();
    }
}