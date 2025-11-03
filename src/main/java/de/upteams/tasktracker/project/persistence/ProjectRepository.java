package de.upteams.tasktracker.project.persistence;

import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @EntityGraph(attributePaths = {"projectTeam", "projectTeam.appUser", "projectTeam.projectRolesSet"})
    List<Project> findAllByOwner(AppUser owner);

    @Query("SELECT DISTINCT p FROM Project p " +
           "LEFT JOIN FETCH p.projectTeam t " +
           "WHERE p.owner.id = :ownerId")
    List<Project> findAllByOwnerWithTeam(@Param("ownerId") UUID ownerId);


    @Query("""
SELECT DISTINCT p
FROM Project p
LEFT JOIN FETCH p.projectTeam t
LEFT JOIN FETCH t.appUser
LEFT JOIN FETCH t.projectRolesSet
LEFT JOIN FETCH p.invitations i
WHERE p.owner = :user
   OR t.appUser = :user
   OR (LOWER(i.email) = LOWER(:#{#user.email}) AND i.status = 'USED')
""")
    List<Project> findAllVisibleForUser(@Param("user") AppUser user);



}