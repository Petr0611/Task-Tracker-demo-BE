package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.mail.EmailService;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskDueDateReminderScheduler {

    private static final Duration ONE_HOUR = Duration.ofHours(1);
    private static final Duration TWENTY_FOUR_HOURS = Duration.ofHours(24);
    private static final Duration REMINDER_TOLERANCE = Duration.ofMinutes(15);

    private final TaskRepository taskRepository;
    private final EmailService emailService;

    @Value("${app.task-reminder.enabled:true}")
    private boolean remindersEnabled;

    @Scheduled(fixedDelayString = "${app.task-reminder.check-interval:900000}")
    @Transactional
    public void processDueDateReminders() {
        if (!remindersEnabled) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Task> for24Hours = taskRepository.findTasksFor24HourReminder(
                now,
                now.plusHours(24).plus(REMINDER_TOLERANCE)
        );
        processTasks(for24Hours, ReminderType.HOURS_24, now);

        List<Task> forOneHour = taskRepository.findTasksForOneHourReminder(
                now,
                now.plusHours(1).plus(REMINDER_TOLERANCE)
        );
        processTasks(forOneHour, ReminderType.HOUR_1, now);
    }

    private void processTasks(List<Task> tasks, ReminderType type, LocalDateTime now) {
        for (Task task : tasks) {
            LocalDateTime dueDate = task.getDueDate();
            if (dueDate == null || !dueDate.isAfter(now)) {
                continue;
            }

            Set<String> recipients = collectRecipients(task);
            if (recipients.isEmpty()) {
                log.debug("No recipients for task {} reminder {}", task.getId(), type);
            }

            Duration timeLeft = Duration.between(now, dueDate);
            if (!shouldSendReminder(type, timeLeft)) {
                continue;
            }
            String timeLeftDescription = formatDuration(timeLeft);
            String projectTitle = resolveProjectTitle(task);

            for (String recipient : recipients) {
                emailService.sendTaskDueReminder(recipient, projectTitle, task.getTitle(), dueDate, timeLeftDescription);
            }

            markReminderSent(task, type);
        }
    }

    private Set<String> collectRecipients(Task task) {
        Set<String> recipients = new HashSet<>();
        for (Collaborator collaborator : task.getExecutors()) {
            AppUser user = collaborator.getAppUser();
            if (user != null && StringUtils.isNotBlank(user.getEmail())) {
                recipients.add(user.getEmail());
            }
        }

        Project project = task.getProject();
        if (project != null) {
            AppUser owner = project.getOwner();
            if (owner != null && StringUtils.isNotBlank(owner.getEmail())) {
                recipients.add(owner.getEmail());
            }
        }
        return recipients;
    }

    private void markReminderSent(Task task, ReminderType type) {
        switch (type) {
            case HOURS_24 -> task.setDueDateReminder24Sent(true);
            case HOUR_1 -> task.setDueDateReminder1Sent(true);
        }
    }

    private String resolveProjectTitle(Task task) {
        Project project = task.getProject();
        return project != null && StringUtils.isNotBlank(project.getTitle()) ? project.getTitle() : "Project";
    }

    private String formatDuration(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            return "менее минуты";
        }

        long totalMinutes = duration.toMinutes();
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(days).append(" д ");
        }
        if (hours > 0) {
            builder.append(hours).append(" ч ");
        }
        if (minutes > 0) {
            builder.append(minutes).append(" мин");
        }

        String result = builder.toString().trim();
        return result.isEmpty() ? "менее минуты" : result;
    }

    private boolean shouldSendReminder(ReminderType type, Duration timeLeft) {
        if (timeLeft.isNegative() || timeLeft.isZero()) {
            return false;
        }

        return switch (type) {
            case HOURS_24 -> isWithinTolerance(timeLeft, TWENTY_FOUR_HOURS);
            case HOUR_1 -> isWithinTolerance(timeLeft, ONE_HOUR)
                    || (timeLeft.compareTo(ONE_HOUR) < 0);
        };
    }

    private boolean isWithinTolerance(Duration actual, Duration target) {
        Duration lowerBound = target.minus(REMINDER_TOLERANCE);
        if (lowerBound.isNegative()) {
            lowerBound = Duration.ZERO;
        }
        Duration upperBound = target.plus(REMINDER_TOLERANCE);
        return actual.compareTo(lowerBound) >= 0 && actual.compareTo(upperBound) <= 0;
    }

    private enum ReminderType {
        HOURS_24,
        HOUR_1
    }
}