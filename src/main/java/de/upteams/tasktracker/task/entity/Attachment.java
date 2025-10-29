package de.upteams.tasktracker.task.entity;

import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;

/**
 * @author Oleg Mordkovich
 * {@code @date} 28.10.2025
 */
@Entity
@Table(name = "attachments")
public class Attachment extends BaseEntity {

    public Attachment() {
    }

    @Column(name = "url", nullable = false)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public String toString() {
        return String.format("Attachment{id=%s, url=%s}", getId(), url);
    }
}