import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.function.Consumer;

public class MailModel {
    // Đã xóa MY_EMAIL và APP_PASSWORD gán cứng ở đây

    // Bổ sung myEmail và appPassword vào tham số hàm
    public void sendEmail(String myEmail, String appPassword, String to, String subject, String content, Consumer<String> logger) throws Exception {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket("smtp.gmail.com", 465);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            logger.accept("Server: " + reader.readLine());
            sendCommand(writer, reader, "EHLO localhost", logger);
            sendCommand(writer, reader, "AUTH LOGIN", logger);
            // Dùng tham số myEmail và appPassword để đăng nhập
            sendCommand(writer, reader, Base64.getEncoder().encodeToString(myEmail.getBytes()), logger);
            sendCommand(writer, reader, Base64.getEncoder().encodeToString(appPassword.getBytes()), logger);
            sendCommand(writer, reader, "MAIL FROM:<" + myEmail + ">", logger);
            sendCommand(writer, reader, "RCPT TO:<" + to + ">", logger);
            sendCommand(writer, reader, "DATA", logger);

            // Bắt đầu gửi nội dung thư
            writer.print("Subject: " + subject + "\r\n");
            writer.print("From: " + myEmail + "\r\n");
            writer.print("To: " + to + "\r\n");
            writer.print("\r\n");
            writer.print(content + "\r\n");
            writer.flush();

            sendCommand(writer, reader, ".", logger);
            sendCommand(writer, reader, "QUIT", logger);
        }
    }

    // Bổ sung myEmail và appPassword vào tham số hàm
    public void fetchLatestEmail(String myEmail, String appPassword, Consumer<String> logger) throws Exception {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket("pop.gmail.com", 995);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            // 1. ĐỌC LỜI CHÀO & ĐĂNG NHẬP (Dùng recent mode với tham số)
            logger.accept("Server: " + reader.readLine());
            sendCommand(writer, reader, "USER recent:" + myEmail, logger);
            sendCommand(writer, reader, "PASS " + appPassword, logger);

            // 2. DÙNG LỆNH LIST ĐỂ LẤY CHÍNH XÁC ID THƯ MỚI NHẤT
            logger.accept("Client: LIST");
            writer.print("LIST\r\n");
            writer.flush();
            String listResponse = reader.readLine();
            logger.accept("Server: " + listResponse);

            if (listResponse == null || !listResponse.startsWith("+OK")) {
                logger.accept("\n[LỖI]: Không thể đọc hộp thư.");
                return;
            }

            int latestEmailId = 0;
            String line;
            // Đọc danh sách trả về từ lệnh LIST để tìm số ID lớn nhất
            while ((line = reader.readLine()) != null) {
                if (line.equals(".")) break;
                try {
                    String[] parts = line.trim().split(" ");
                    int currentId = Integer.parseInt(parts[0]);
                    if (currentId > latestEmailId) {
                        latestEmailId = currentId;
                    }
                } catch (Exception ignored) {}
            }

            if (latestEmailId == 0) {
                logger.accept("\n=> HỘP THƯ TRỐNG: Không có thư nào để tải về.");
                sendCommand(writer, reader, "QUIT", logger);
                return;
            }

            // 3. KÉO THƯ MỚI NHẤT DỰA TRÊN ID ĐÃ TÌM THẤY
            logger.accept("\n>>> ĐANG KÉO THƯ MỚI NHẤT VỀ (RETR " + latestEmailId + ")...");
            writer.print("RETR " + latestEmailId + "\r\n");
            writer.flush();

            String retrResponse = reader.readLine();
            logger.accept("Server: " + retrResponse);

            // 4. PHÂN TÍCH VÀ BÓC TÁCH NỘI DUNG THƯ
            boolean isHeader = true;
            StringBuilder bodyBuilder = new StringBuilder();
            String subject = "(Không có tiêu đề)";
            String from = "(Không rõ người gửi)";
            String date = "(Không rõ ngày gửi)";

            while ((line = reader.readLine()) != null) {
                if (line.equals(".")) break;

                if (isHeader) {
                    if (line.isEmpty()) {
                        isHeader = false;
                        continue;
                    }
                    String lowerLine = line.toLowerCase();
                    if (lowerLine.startsWith("from:")) {
                        from = line.substring(5).trim();
                    } else if (lowerLine.startsWith("subject:")) {
                        subject = line.substring(8).trim();
                    } else if (lowerLine.startsWith("date:")) {
                        date = line.substring(5).trim();
                    }
                } else {
                    bodyBuilder.append(line).append("\n");
                }
            }

            String cleanText = cleanHtmlAndMime(bodyBuilder.toString());

            logger.accept("\n================ THÔNG TIN BỨC THƯ MỚI NHẤT ================\n");
            logger.accept("🕒 NGÀY GỬI: " + date);
            logger.accept("👤 NGƯỜI GỬI: " + from);
            logger.accept("📌 TIÊU ĐỀ: " + subject);
            logger.accept("------------------------------------------------------------");

            if (cleanText.isEmpty()) {
                logger.accept("(Bức thư này trống hoặc chứa định dạng đa phương tiện/hình ảnh)");
            } else {
                logger.accept(cleanText);
            }
            logger.accept("\n============================================================\n");

            sendCommand(writer, reader, "QUIT", logger);
        }
    }

    private String cleanHtmlAndMime(String rawBody) {
        String text = rawBody;
        text = text.replaceAll("(?m)^Content-.*$", "");
        text = text.replaceAll("(?m)^--.*$", "");
        text = text.replaceAll("<[^>]*>", "");
        try {
            text = text.replaceAll("=\\r?\\n", "");
            text = text.replaceAll("=([0-9A-Fa-f]{2})", "%$1");
            text = java.net.URLDecoder.decode(text, "UTF-8");
        } catch (Exception e) {}
        text = text.replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").replace("&quot;", "\"");
        return text.trim();
    }

    private void sendCommand(PrintWriter writer, BufferedReader reader, String command, Consumer<String> logger) throws Exception {
        logger.accept("Client: " + command);
        writer.print(command + "\r\n");
        writer.flush();
        if (!command.equals("QUIT")) {
            String response;
            do {
                response = reader.readLine();
                if (response != null) {
                    logger.accept("Server: " + response);
                }
            } while (response != null && response.length() >= 4 && response.charAt(3) == '-');
        }
    }
}