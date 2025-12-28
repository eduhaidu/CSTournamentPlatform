package cs2.tournamentsite.tournamentserver.services;

import java.util.List;

import org.springframework.stereotype.Service;

import cs2.tournamentsite.tournamentserver.models.Map;
import cs2.tournamentsite.tournamentserver.repositories.MapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapService {
    private final MapRepository mapRepository;

    public Map findMapById(Integer id) {
        return mapRepository.findById(id).orElse(null);
    }

    public Map saveMap(Map map) {
        return mapRepository.save(map);
    }

    public Map updateMap(Integer id, Map updatedMap) {
        return mapRepository.findById(id).map(existingMap -> {
            existingMap.setMapName(updatedMap.getMapName());
            existingMap.setMatchId(updatedMap.getMatchId());
            existingMap.setTeamACTRounds(updatedMap.getTeamACTRounds());
            existingMap.setTeamBTRounds(updatedMap.getTeamBTRounds());
            existingMap.setTeamATRounds(updatedMap.getTeamATRounds());
            existingMap.setTeamBCTRounds(updatedMap.getTeamBCTRounds());
            existingMap.setTeamAFinalScore(updatedMap.getTeamAFinalScore());
            existingMap.setTeamBFinalScore(updatedMap.getTeamBFinalScore());
            return mapRepository.save(existingMap);
        }).orElse(null);
    }

    public void deleteMap(Integer id) {
        if (mapRepository.existsById(id)) {
            mapRepository.deleteById(id);
        }
    }

    public List<Map> findMapsByMatchId(Integer matchId) {
        return mapRepository.findByMatchId(matchId);
    }
}
