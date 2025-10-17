package de.upteams.tasktracker.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for email sending
 */
@Service
@RequiredArgsConstructor
public class EmailService {

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

    private String buildFrontendLink(String path, String inviteToken) {
        String sanitizedBase = frontendBaseUrl.replaceAll("/+$", "");
        String sanitizedPath = path.startsWith("/") ? path : "/" + path;
        return "%s%s?inviteToken=%s".formatted(sanitizedBase, sanitizedPath, inviteToken);
    }
}
