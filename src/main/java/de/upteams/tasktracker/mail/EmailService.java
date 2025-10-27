package de.upteams.tasktracker.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Service for email sending
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final DateTimeFormatter DUE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.frontend-url:${app.base-url}}")
    private String frontendBaseUrl;

    private final EmailSender emailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendConfirmationEmail(String sentTo, String confirmationCode) {
        String confirmationLink = "%s/api/v1/users/confirm/%s".formatted(baseUrl.replaceAll("/+$", ""), confirmationCode);

        Map<String, Object> model = Map.of(
                "link", confirmationLink
        );

        String htmlContent = templateEngine.generateHtml("confirm_registration_mail.ftlh", model);
        emailSender.sendEmail(sentTo, "Confirm your registration", htmlContent);
    }

    public void sendPasswordResetEmail(String email, String token) {
        String resetLink = "%s/reset-password?token=%s"
                .formatted(frontendBaseUrl.replaceAll("/+$", ""), token);
        String subject = "Password Reset Request";
        String body = "Click the link to reset your password: " + resetLink;
        emailSender.sendEmail(email, subject, body);
    }

    @Async
    public void sendProjectInvitationForNewUser(String sentTo, String projectName, String inviteToken) {
        String registerLink = buildFrontendLink("/confirm-invite", inviteToken);

        String body = """
                <p>Приглашаем вас зарегистрироваться и присоединиться к проекту %s.</p>
                <p>Ссылка: <a href="%s">%s</a></p>
                """.formatted(projectName, registerLink, registerLink);
        emailSender.sendEmail(sentTo, "Invitation to join project " + projectName, body);
    }

    @Async
    public void sendProjectInvitationForExistingUser(String sentTo, String projectName, String inviteToken) {
        String confirmLink = buildFrontendLink("/confirm-invite", inviteToken);
        String body = """
                <p>Вас пригласили в проект %s.</p>
                <p>Подтвердите участие по ссылке: <a href="%s">%s</a></p>
                """.formatted(projectName, confirmLink, confirmLink);
        emailSender.sendEmail(sentTo, "Confirm participation in project " + projectName, body);
    }

    @Async
    public void sendTaskDueReminder(
            String sentTo,
            String projectTitle,
            String taskTitle,
            LocalDateTime dueDate,
            String timeLeftDescription
    ) {
        String formattedDueDate = dueDate.format(DUE_DATE_FORMATTER);
        String subject = "Напоминание о сроке задачи '" + taskTitle + "'";
        String body = """
                <p>Задача <strong>%s</strong> в проекте <strong>%s</strong> должна быть выполнена до %s.</p>
                <p>Осталось: %s.</p>
                """.formatted(taskTitle, projectTitle, formattedDueDate, timeLeftDescription);
        emailSender.sendEmail(sentTo, subject, body);
    }

    @Async
    public void sendCommentMentionNotification(
            String sentTo,
            String projectTitle,
            String taskTitle,
            String authorName,
            String commentText,
            String projectId,
            String taskId
    ) {
        String sanitizedBase = frontendBaseUrl.replaceAll("/+$", "");
        String taskLink = "%s/projects/%s/tasks/%s".formatted(sanitizedBase, projectId, taskId);
        String subject = "Вас упомянули в задаче '" + taskTitle + "'";
        String body = """
                <p><strong>%s</strong> упомянул(а) вас в комментарии к задаче <strong>%s</strong> проекта <strong>%s</strong>.</p>
                <p>Комментарий:</p>
                <blockquote>%s</blockquote>
                <p><a href="%s">Открыть задачу</a></p>
                """.formatted(authorName, taskTitle, projectTitle, commentText, taskLink);
        emailSender.sendEmail(sentTo, subject, body);
    }

    private String buildFrontendLink(String path, String inviteToken) {
        String sanitizedBase = frontendBaseUrl.replaceAll("/+$", "");
        String sanitizedPath = path.startsWith("/") ? path : "/" + path;
        return "%s%s?inviteToken=%s".formatted(sanitizedBase, sanitizedPath, inviteToken);
    }
}
