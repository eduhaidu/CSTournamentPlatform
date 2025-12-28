package cs2.tournamentsite.tournamentserver.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cs2.tournamentsite.tournamentserver.models.Map;

public interface MapRepository extends JpaRepository<Map, Integer> {
    public List<Map> findByMatchId(Integer matchId);
}
