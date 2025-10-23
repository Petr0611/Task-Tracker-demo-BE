package de.upteams.tasktracker.taskcolumn.utils;

import de.upteams.tasktracker.taskcolumn.dto.TaskColumnDto;
import de.upteams.tasktracker.taskcolumn.entity.TaskColumn;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for converting TaskColumn entities to DTOs.
 */
@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = TaskMappingService.class
)
public interface TaskColumnMappingService {

    @Mapping(target = "projectId", source = "project.id")
    TaskColumnDto mapEntityToDto(TaskColumn column);

    List<TaskColumnDto> mapEntitiesToDtos(List<TaskColumn> columns);
}