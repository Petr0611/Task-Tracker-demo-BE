package de.upteams.tasktracker.task.entity;


import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;

import java.util.Objects;

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


    @Override
    public String toString() {
        return String.format("Attachment with ID %s: url=%s, task=%s", super.id, url, task);
    }

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
    public boolean equals(Object o) {
        if (!(o instanceof Attachment that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(url, that.url) && Objects.equals(task, that.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, task);
    }
}