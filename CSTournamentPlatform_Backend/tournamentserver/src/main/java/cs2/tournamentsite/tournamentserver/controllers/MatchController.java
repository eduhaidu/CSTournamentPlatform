package cs2.tournamentsite.tournamentserver.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cs2.tournamentsite.tournamentserver.models.Match;
import cs2.tournamentsite.tournamentserver.services.MatchService;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;

    @GetMapping("/{id}")
    public Match getMatchById(@PathVariable Integer id) {
        return matchService.findMatchById(id);
    }

    @GetMapping("/tournament/{tournamentid}")
    public List<Match> getMatchesForTournament(@PathVariable Integer tournamentid) {
        return matchService.getMatchesByTournamentId(tournamentid);
    }
    
    @PostMapping("/add")
    public Match addMatch(@RequestBody Match match) {
        return matchService.saveMatch(match);
    }
    
    @PutMapping("/update/{id}")
    public Match updateMatch(@PathVariable Integer id, @RequestBody Match updatedMatch) {
        return matchService.updateMatch(id, updatedMatch);
    }
    
    @DeleteMapping("/{id}")
    public void deleteMatch(@PathVariable Integer id) {
        matchService.deleteMatch(id);
    }
    
}
