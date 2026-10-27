import javax.swing.*;
import java.awt.*;

public class MailView extends JFrame {
    private JTextField txtMyEmail, txtTo, txtSubject;
    private JPasswordField txtAppPassword;
    private JTextArea txtContent, txtLogSMTP, txtLogPOP3;
    private JButton btnSend, btnFetch;

    public MailView() {
        setTitle("Email Client (MVC Architecture) - Nhóm 2");
        setSize(850, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. TẠO THANH CẤU HÌNH TÀI KHOẢN (NẰM TRÊN CÙNG)
        JPanel accountPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        accountPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Cấu hình Tài khoản của bạn (Người gửi/nhận)"));

        accountPanel.add(new JLabel("   Email của bạn (VD: vitrieumot.pham@gmail.com):"));
        txtMyEmail = new JTextField();
        txtMyEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        accountPanel.add(txtMyEmail);

        accountPanel.add(new JLabel("   Mật khẩu Ứng dụng (16 ký tự):"));
        txtAppPassword = new JPasswordField();
        txtAppPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        accountPanel.add(txtAppPassword);

        // Bọc thêm 1 panel trống để tạo padding cho accountPanel
        JPanel topMarginPanel = new JPanel(new BorderLayout());
        topMarginPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        topMarginPanel.add(accountPanel, BorderLayout.CENTER);

        add(topMarginPanel, BorderLayout.NORTH);

        // 2. KHU VỰC TAB CHÍNH (Đã xóa các icon)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.addTab("Gửi Thư (SMTP)", createSmtpPanel());
        tabbedPane.addTab("Hộp Thư (POP3)", createPop3Panel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createSmtpPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));

        JPanel formPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel pnlTo = new JPanel(new BorderLayout());
        pnlTo.add(new JLabel("Người nhận (To): "), BorderLayout.WEST);
        txtTo = new JTextField();
        txtTo.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlTo.add(txtTo, BorderLayout.CENTER);

        JPanel pnlSubject = new JPanel(new BorderLayout());
        pnlSubject.add(new JLabel("Tiêu đề (Subject): "), BorderLayout.WEST);
        txtSubject = new JTextField();
        txtSubject.setFont(new Font("Arial", Font.PLAIN, 14));
        pnlSubject.add(txtSubject, BorderLayout.CENTER);

        formPanel.add(pnlTo);
        formPanel.add(pnlSubject);

        txtContent = new JTextArea("Nhập nội dung thư vào đây...");
        txtContent.setFont(new Font("Arial", Font.PLAIN, 14));
        txtContent.setLineWrap(true);
        JScrollPane scrollContent = new JScrollPane(txtContent);
        scrollContent.setPreferredSize(new Dimension(800, 120));

        // Nút gửi không còn icon
        btnSend = new JButton("GỬI EMAIL (SMTP)");
        btnSend.setPreferredSize(new Dimension(200, 40));
        btnSend.setBackground(new Color(40, 167, 69));
        btnSend.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(btnSend);

        inputPanel.add(formPanel, BorderLayout.NORTH);
        inputPanel.add(scrollContent, BorderLayout.CENTER);
        inputPanel.add(btnPanel, BorderLayout.SOUTH);

        panel.add(inputPanel, BorderLayout.NORTH);

        txtLogSMTP = new JTextArea();
        txtLogSMTP.setEditable(false);
        txtLogSMTP.setBackground(Color.BLACK);
        txtLogSMTP.setForeground(Color.GREEN);
        txtLogSMTP.setFont(new Font("Monospaced", Font.PLAIN, 14));

        panel.add(new JScrollPane(txtLogSMTP), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPop3Panel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Nút tải thư không còn icon
        btnFetch = new JButton("TẢI THƯ MỚI NHẤT VỀ (POP3)");
        btnFetch.setPreferredSize(new Dimension(300, 40));
        btnFetch.setBackground(new Color(0, 123, 255));
        btnFetch.setForeground(Color.WHITE);
        btnFetch.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(btnFetch);

        txtLogPOP3 = new JTextArea();
        txtLogPOP3.setEditable(false);
        txtLogPOP3.setBackground(Color.BLACK);
        txtLogPOP3.setForeground(Color.YELLOW);
        txtLogPOP3.setFont(new Font("Monospaced", Font.PLAIN, 14));

        panel.add(btnPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtLogPOP3), BorderLayout.CENTER);
        return panel;
    }

    // --- CÁC HÀM GETTER ĐỂ CONTROLLER LẤY DỮ LIỆU ---
    public String getMyEmail() { return txtMyEmail.getText().trim(); }
    public String getAppPassword() { return new String(txtAppPassword.getPassword()).trim(); }
    public String getTo() { return txtTo.getText().trim(); }
    public String getSubject() { return txtSubject.getText().trim(); }
    public String getContent() { return txtContent.getText().trim(); }

    public void appendSmtpLog(String text) { txtLogSMTP.append(text + "\n"); }
    public void setSmtpLog(String text) { txtLogSMTP.setText(text); }
    public JButton getBtnSend() { return btnSend; }

    public void appendPop3Log(String text) { txtLogPOP3.append(text + "\n"); }
    public void setPop3Log(String text) { txtLogPOP3.setText(text); }
    public JButton getBtnFetch() { return btnFetch; }
}