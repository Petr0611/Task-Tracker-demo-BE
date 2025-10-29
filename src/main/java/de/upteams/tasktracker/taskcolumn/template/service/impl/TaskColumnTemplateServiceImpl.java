package de.upteams.tasktracker.taskcolumn.template.service.impl;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import de.upteams.tasktracker.taskcolumn.dto.TaskColumnDto;
import de.upteams.tasktracker.taskcolumn.entity.TaskColumn;
import de.upteams.tasktracker.taskcolumn.persistence.TaskColumnRepository;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateApplyRequestDto;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateCreateRequestDto;
import de.upteams.tasktracker.taskcolumn.template.dto.TaskColumnTemplateDto;
import de.upteams.tasktracker.taskcolumn.template.entity.TaskColumnTemplate;
import de.upteams.tasktracker.taskcolumn.template.entity.TaskColumnTemplateColumn;
import de.upteams.tasktracker.taskcolumn.template.persistence.TaskColumnTemplateRepository;
import de.upteams.tasktracker.taskcolumn.template.service.interfaces.TaskColumnTemplateService;
import de.upteams.tasktracker.taskcolumn.template.utils.TaskColumnTemplateMappingService;
import de.upteams.tasktracker.taskcolumn.utils.TaskColumnMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskColumnTemplateServiceImpl implements TaskColumnTemplateService {

    private static final String COLUMN_REMOVAL_BLOCKED_MESSAGE = "Cannot remove project column because it still contains tasks";
    private static final String TEMPLATE_NOT_FOUND_MESSAGE = "Task column template not found";

    private final TaskColumnTemplateRepository templateRepository;
    private final TaskColumnRepository columnRepository;
    private final TaskColumnTemplateMappingService mappingService;
    private final TaskColumnMappingService columnMappingService;
    private final ProjectService projectService;
    private final CollaboratorService collaboratorService;

    @Override
    @Transactional
    public TaskColumnTemplateDto createFromProject(String projectId, TaskColumnTemplateCreateRequestDto requestDto, AppUser creator) {
        final Project project = getProjectOrThrow(projectId);
        enforceTaskManagementPermission(project, creator);

        final List<TaskColumn> projectColumns = columnRepository.findAllByProjectOrderByOrderIndexAsc(project);
        final TaskColumnTemplate template = new TaskColumnTemplate();
        template.setName(requestDto.name());
        template.setOwner(creator);

        int orderIndex = 0;
        for (TaskColumn column : projectColumns.stream().sorted(Comparator.comparing(TaskColumn::getOrderIndex)).toList()) {
            final TaskColumnTemplateColumn templateColumn = new TaskColumnTemplateColumn();
            templateColumn.setTitle(column.getTitle());
            templateColumn.setOrderIndex(orderIndex++);
            template.addColumn(templateColumn);
        }

        final TaskColumnTemplate saved = templateRepository.save(template);
        return mappingService.mapEntityToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskColumnTemplateDto> getAllForUser(AppUser owner) {
        return mappingService.mapEntitiesToDtos(templateRepository.findAllByOwnerOrderByNameAsc(owner));
    }

    @Override
    @Transactional
    public List<TaskColumnDto> applyTemplate(String templateId, TaskColumnTemplateApplyRequestDto requestDto, AppUser changer) {
        final TaskColumnTemplate template = getTemplateOrThrow(templateId, changer);
        final Project project = getProjectOrThrow(requestDto.projectId());
        enforceTaskManagementPermission(project, changer);

        final List<TaskColumnTemplateColumn> templateColumns = template.getColumns().stream()
                .sorted(Comparator.comparing(TaskColumnTemplateColumn::getOrderIndex))
                .toList();

        final List<TaskColumn> projectColumns = new ArrayList<>(columnRepository.findAllByProjectOrderByOrderIndexAsc(project));

        final List<TaskColumn> columnsToPersist = new ArrayList<>();

        for (int i = 0; i < templateColumns.size(); i++) {
            final TaskColumnTemplateColumn templateColumn = templateColumns.get(i);
            final TaskColumn column = i < projectColumns.size() ? projectColumns.get(i) : new TaskColumn();
            column.setProject(project);
            column.setTitle(templateColumn.getTitle());
            column.setOrderIndex(i);
            columnsToPersist.add(column);
        }

        if (projectColumns.size() > templateColumns.size()) {
            for (int i = templateColumns.size(); i < projectColumns.size(); i++) {
                final TaskColumn column = projectColumns.get(i);
                if (!column.getTasks().isEmpty()) {
                    throw new RestApiException(HttpStatus.BAD_REQUEST, COLUMN_REMOVAL_BLOCKED_MESSAGE);
                }
                columnRepository.delete(column);
            }
        }

        columnRepository.saveAll(columnsToPersist);

        return columnMappingService.mapEntitiesToDtos(columnRepository.findAllByProjectOrderByOrderIndexAsc(project));
    }

    private TaskColumnTemplate getTemplateOrThrow(String templateId, AppUser owner) {
        final UUID templateUuid = parseUuid(templateId, "Template ID must be a valid UUID");
        return templateRepository.findByIdAndOwner(templateUuid, owner)
                .orElseThrow(() -> new RestApiException(HttpStatus.NOT_FOUND, TEMPLATE_NOT_FOUND_MESSAGE));
    }

    private Project getProjectOrThrow(String projectId) {
        try {
            return projectService.getOrTrow(projectId);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, TaskValidationConstats.PROJECT_ID_INVALID_MESSAGE);
        }
    }

    private void enforceTaskManagementPermission(Project project, AppUser user) {
        if (isProjectOwner(project, user)) {
            return;
        }

        final boolean hasPermission = collaboratorService.hasUserPermission(
                user,
                project,
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }
    }

    private boolean isProjectOwner(Project project, AppUser user) {
        return project.getOwner() != null && project.getOwner().equals(user);
    }

    private UUID parseUuid(String rawId, String errorMessage) {
        try {
            return UUID.fromString(rawId);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }
}