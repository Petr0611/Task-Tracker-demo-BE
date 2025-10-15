package de.upteams.tasktracker.task.utils;

import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.task.dto.TaskDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.dto.EmployeeDto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-11T20:43:53+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.13.jar, environment: Java 17.0.16 (Amazon.com Inc.)"
)
@Component
public class TaskMappingServiceImpl implements TaskMappingService {

    @Override
    public TaskDto mapEntityToDto(Task entity) {
        if ( entity == null ) {
            return null;
        }

        String title = null;
        String description = null;
        ProjectResponseDto project = null;

        title = entity.getTitle();
        description = entity.getDescription();
        project = projectToProjectResponseDto( entity.getProject() );

        String id = null;

        TaskDto taskDto = new TaskDto( id, title, description, project );

        return taskDto;
    }

    @Override
    public Task mapDtoToEntity(TaskDto dto) {
        if ( dto == null ) {
            return null;
        }

        Task task = new Task();

        task.setTitle( dto.getTitle() );
        task.setDescription( dto.getDescription() );

        return task;
    }

    protected ProjectResponseDto projectToProjectResponseDto(Project project) {
        if ( project == null ) {
            return null;
        }

        String id = null;
        String title = null;
        String description = null;

        if ( project.getId() != null ) {
            id = project.getId().toString();
        }
        title = project.getTitle();
        description = project.getDescription();

        EmployeeDto owner = null;

        ProjectResponseDto projectResponseDto = new ProjectResponseDto( id, title, description, owner );

        return projectResponseDto;
    }
}
