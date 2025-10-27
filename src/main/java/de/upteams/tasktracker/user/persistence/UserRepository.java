package de.upteams.tasktracker.user.persistence;

import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<AppUser, UUID> {
        @Query("select a from AppUser a where upper(a.email) = upper(?1)")
        Optional<AppUser> findByEmailIgnoreCase(String email);

        @Query("select a from AppUser a where lower(a.displayName) = lower(?1)")
        Optional<AppUser> findByDisplayNameIgnoreCase(String displayName);

        @Query("select a from AppUser a where lower(a.email) like lower(concat(?1, '@%'))")
        Optional<AppUser> findByEmailLocalPartIgnoreCase(String emailLocalPart);
}
