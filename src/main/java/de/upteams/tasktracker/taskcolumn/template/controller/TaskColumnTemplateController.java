package de.upteams.tasktracker.taskcolumn.template.controller;

import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnDto;
import de.upteams.tasktracker.taskcolumn.template.controller.api.TaskColumnTemplateApi;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateApplyRequestDto;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateCreateRequestDto;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateDto;
import de.upteams.tasktracker.taskcolumn.template.service.interfaces.TaskColumnTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class TaskColumnTemplateController implements TaskColumnTemplateApi {

    private final TaskColumnTemplateService service;

    @Override
    public TaskColumnTemplateDto create(String projectId, @Valid TaskColumnTemplateCreateRequestDto requestDto, AuthUserDetails principal) {
        return service.createFromProject(projectId, requestDto, principal.user());
    }

    @Override
    public List<TaskColumnTemplateDto> getAll(AuthUserDetails principal) {
        return service.getAllForUser(principal.user());
    }

    @Override
    public List<TaskColumnDto> apply(String templateId, @Valid TaskColumnTemplateApplyRequestDto requestDto, AuthUserDetails principal) {
        return service.applyTemplate(templateId, requestDto, principal.user());
    }
}