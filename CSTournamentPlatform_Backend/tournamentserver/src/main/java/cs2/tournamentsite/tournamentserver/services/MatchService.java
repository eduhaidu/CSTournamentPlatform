package cs2.tournamentsite.tournamentserver.services;

import java.util.List;

import org.springframework.stereotype.Service;

import cs2.tournamentsite.tournamentserver.models.Match;
import cs2.tournamentsite.tournamentserver.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    private final MatchRepository matchRepository;

    public Match findMatchById(Integer id) {
        return matchRepository.findById(id).orElse(null);
    }

    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }

    public Match updateMatch(Integer id, Match updatedMatch) {
        return matchRepository.findById(id).map(existingMatch -> {
            existingMatch.setTournamentId(updatedMatch.getTournamentId());
            existingMatch.setTeamAId(updatedMatch.getTeamAId());
            existingMatch.setTeamBId(updatedMatch.getTeamBId());
            existingMatch.setMatchDate(updatedMatch.getMatchDate());
            existingMatch.setResult(updatedMatch.getResult());
            existingMatch.setStatus(updatedMatch.getStatus());
            existingMatch.setTeamAScore(updatedMatch.getTeamAScore());
            existingMatch.setTeamBScore(updatedMatch.getTeamBScore());
            existingMatch.setWinnerTeamId(updatedMatch.getWinnerTeamId());
            existingMatch.setMatchType(updatedMatch.getMatchType());
            return matchRepository.save(existingMatch);
        }).orElse(null);
    }

    public void deleteMatch(Integer id) {
        if (matchRepository.existsById(id)) {
            matchRepository.deleteById(id);
        }
    }

    public List<Match> getMatchesByTournamentId(Integer tournamentId) {
        return matchRepository.findByTournamentId(tournamentId);
    }
}
