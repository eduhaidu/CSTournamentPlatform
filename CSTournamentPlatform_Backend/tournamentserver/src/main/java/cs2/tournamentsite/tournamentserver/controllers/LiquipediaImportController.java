package cs2.tournamentsite.tournamentserver.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cs2.tournamentsite.tournamentserver.models.Event;
import cs2.tournamentsite.tournamentserver.models.Team;
import cs2.tournamentsite.tournamentserver.services.LiquipediaImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin/liquipedia")
@RequiredArgsConstructor
@Slf4j
public class LiquipediaImportController {

    private final LiquipediaImportService liquipediaImportService;

    /**
     * Import a single tournament by Liquipedia page title
     * Example: POST /api/admin/liquipedia/tournament?pageTitle=IEM_Katowice_2024
     */
    @PostMapping("/tournament")
    public ResponseEntity<?> importTournament(@RequestParam String pageTitle) {
        try {
            log.info("Admin requested import of tournament: {}", pageTitle);
            
            Event event = liquipediaImportService.importTournament(pageTitle);
            
            if (event == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to import tournament: " + pageTitle)
                );
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Tournament imported successfully",
                "event", event
            ));
            
        } catch (Exception e) {
            log.error("Error in import endpoint", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Import a single team by Liquipedia page title
     * Example: POST /api/admin/liquipedia/team?pageTitle=FaZe_Clan
     */
    @PostMapping("/team")
    public ResponseEntity<?> importTeam(@RequestParam String pageTitle) {
        try {
            log.info("Admin requested import of team: {}", pageTitle);
            
            Team team = liquipediaImportService.importTeam(pageTitle);
            
            if (team == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Failed to import team: " + pageTitle)
                );
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Team imported successfully",
                "team", team
            ));
            
        } catch (Exception e) {
            log.error("Error in import endpoint", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Import multiple tournaments at once
     * Example: POST /api/admin/liquipedia/tournaments/bulk
     * Body: ["IEM_Katowice_2024", "BLAST_Premier_Spring_2024", "ESL_Pro_League_Season_18"]
     */
    @PostMapping("/tournaments/bulk")
    public ResponseEntity<?> importTournamentsBulk(@RequestBody List<String> pageTitles) {
        try {
            log.info("Admin requested bulk import of {} tournaments", pageTitles.size());
            
            List<Event> events = liquipediaImportService.importTournaments(pageTitles);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk import completed");
            response.put("requested", pageTitles.size());
            response.put("imported", events.size());
            response.put("events", events);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in bulk import endpoint", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Import multiple teams at once
     * Example: POST /api/admin/liquipedia/teams/bulk
     * Body: ["FaZe_Clan", "Natus_Vincere", "Vitality"]
     */
    @PostMapping("/teams/bulk")
    public ResponseEntity<?> importTeamsBulk(@RequestBody List<String> pageTitles) {
        try {
            log.info("Admin requested bulk import of {} teams", pageTitles.size());
            
            List<Team> teams = liquipediaImportService.importTeams(pageTitles);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk import completed");
            response.put("requested", pageTitles.size());
            response.put("imported", teams.size());
            response.put("teams", teams);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error in bulk import endpoint", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * Get sample tournament page titles for import
     */
    @GetMapping("/sample-tournaments")
    public ResponseEntity<?> getSampleTournaments() {
        List<String> samples = List.of(
            "Intel_Extreme_Masters/2024/Katowice",
            "BLAST_Premier/2024/Spring_Final",
            "ESL_Pro_League/Season_18",
            "PGL/2024/Copenhagen"
        );
        
        return ResponseEntity.ok(Map.of(
            "message", "Sample tournament page titles from Liquipedia",
            "pageTitles", samples
        ));
    }

    /**
     * Get sample team page titles for import
     */
    @GetMapping("/sample-teams")
    public ResponseEntity<?> getSampleTeams() {
        List<String> samples = List.of(
            "FaZe_Clan",
            "Natus_Vincere",
            "Vitality",
            "G2_Esports",
            "MOUZ"
        );
        
        return ResponseEntity.ok(Map.of(
            "message", "Sample team page titles from Liquipedia",
            "pageTitles", samples
        ));
    }
}
