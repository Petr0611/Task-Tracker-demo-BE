package de.upteams.tasktracker.taskcomment.utils;

import de.upteams.tasktracker.taskcomment.dto.CommentDto;
import de.upteams.tasktracker.taskcomment.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMappingService {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorDisplayName", source = "author.displayName")
    @Mapping(target = "authorEmail", source = "author.email")
    CommentDto mapEntityToDto(Comment entity);
}