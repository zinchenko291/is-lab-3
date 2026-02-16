package me.zinch.is.islab3.services;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import me.zinch.is.islab3.Config;
import me.zinch.is.islab3.models.entities.ImportConflict;
import me.zinch.is.islab3.models.entities.User;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class MailService {
    private static final Logger LOGGER = Logger.getLogger(MailService.class.getName());

    @Resource
    private ManagedExecutorService executor;

    public void sendConflictEmail(User user, ImportConflict conflict) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            LOGGER.fine("Email skipped: user or email is missing");
            return;
        }
        try {
            executor.submit(() -> sendConflictEmailSync(user, conflict));
        } catch (RuntimeException ex) {
            LOGGER.log(Level.WARNING, "ManagedExecutor unavailable, sending email synchronously", ex);
            sendConflictEmailSync(user, conflict);
        }
    }

    private void sendConflictEmailSync(User user, ImportConflict conflict) {
        String host = Config.MAIL_HOST;
        String port = Config.MAIL_PORT;
        String username = Config.MAIL_USER;
        String password = Config.MAIL_PASS;
        String from = Config.MAIL_FROM;
        if (host == null || host.isBlank() || port == null || port.isBlank() || from == null || from.isBlank()) {
            LOGGER.warning("Email skipped: missing MAIL_HOST/MAIL_PORT/MAIL_FROM in Config/email.properties");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        boolean hasAuth = username != null && !username.isBlank() && password != null && !password.isBlank();
        props.put("mail.smtp.auth", hasAuth ? "true" : "false");
        boolean useSsl = "465".equals(port);
        if (useSsl) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.connectiontimeout", "3000");
        props.put("mail.smtp.timeout", "3000");
        props.put("mail.smtp.writetimeout", "3000");
        Session session = Session.getInstance(props);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail(), false));
            message.setSubject("Конфликт импорта требует решения");
            message.setText(buildConflictBody(conflict));
            if (username != null && password != null) {
                Transport.send(message, username, password);
            } else {
                Transport.send(message);
            }
            LOGGER.info("Email sent for import conflict id=" + conflict.getId());
        } catch (Exception ignored) {
            LOGGER.log(Level.WARNING, "Failed to send email for import conflict id=" + conflict.getId(), ignored);
        }
    }

    private String buildConflictBody(ImportConflict conflict) {
        return String.format(
                "Обнаружен конфликт импорта. Операция=%s, конфликт=%s, координаты=(%s,%s). Решите конфликт в интерфейсе.",
                conflict.getOperation().getId(),
                conflict.getId(),
                conflict.getCoordinateX(),
                conflict.getCoordinateY()
        );
    }
}
