package de.upteams.tasktracker.taskcolumn.template.utils;

import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateDto;
import de.upteams.tasktracker.taskcolumn.template.entity.TaskColumnTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskColumnTemplateMappingService {

    @Mapping(target = "ownerId", source = "owner.id")
    TaskColumnTemplateDto mapEntityToDto(TaskColumnTemplate template);

    List<TaskColumnTemplateDto> mapEntitiesToDtos(List<TaskColumnTemplate> templates);
}