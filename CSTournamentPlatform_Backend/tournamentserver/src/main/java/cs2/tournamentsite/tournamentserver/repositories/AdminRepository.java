package cs2.tournamentsite.tournamentserver.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cs2.tournamentsite.tournamentserver.models.Admin;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
