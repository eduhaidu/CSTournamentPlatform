package cs2.tournamentsite.tournamentserver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cs2.tournamentsite.tournamentserver.models.Match;

public interface MatchRepository extends JpaRepository<Match, Integer> {
    public List<Match> findByTournamentId(Integer tournamentId);
}
