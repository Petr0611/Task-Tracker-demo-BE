package de.upteams.tasktracker.taskcolumn.template.service.interfaces;

import de.upteams.tasktracker.taskcolumn.dto.TaskColumnDto;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateApplyRequestDto;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateCreateRequestDto;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateDto;
import de.upteams.tasktracker.user.entity.AppUser;

import java.util.List;

public interface TaskColumnTemplateService {

    TaskColumnTemplateDto createFromProject(String projectId, TaskColumnTemplateCreateRequestDto requestDto, AppUser creator);

    List<TaskColumnTemplateDto> getAllForUser(AppUser owner);

    List<TaskColumnDto> applyTemplate(String templateId, TaskColumnTemplateApplyRequestDto requestDto, AppUser changer);
}