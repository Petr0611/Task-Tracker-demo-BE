package de.upteams.tasktracker.project.service.impl;

import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.request.ProjectUpdateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.project.utils.ProjectMapper;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for various operations with Projects
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repository;
    private final ProjectMapper mappingService;

    @Override
    public ProjectResponseDto save(ProjectCreateDto newProjectDto, AppUser projectOwner) {
        Project project = mappingService.mapDtoToEntity(newProjectDto);
        project.setOwner(projectOwner);
        return mappingService.mapEntityToDto(repository.save(project));
    }

    @Override
    public ProjectResponseDto getById(String id) {
        return mappingService.mapEntityToDto(getOrTrow(id));
    }

    @Override
    public Project getOrTrow(String id) {
        return repository
                .findById(UUID.fromString(id))
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public List<ProjectResponseDto> getAll() {
        return repository
                .findAll()
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public void delete(String id) {
        repository.deleteById(UUID.fromString(id));
    }

    @Override
    public ProjectResponseDto updateProject(String id, ProjectUpdateDto updateDTO) {
        Project project = getOrTrow(id);

        if (updateDTO.title() != null && !updateDTO.title().isBlank()) {
            project.setTitle(updateDTO.title());
        }

        if (updateDTO.description() != null && !updateDTO.description().isBlank()) {
            project.setDescription(updateDTO.description());
        }

        Project updatedProject = repository.save(project);
        return mappingService.mapEntityToDto(updatedProject);
    }
}
