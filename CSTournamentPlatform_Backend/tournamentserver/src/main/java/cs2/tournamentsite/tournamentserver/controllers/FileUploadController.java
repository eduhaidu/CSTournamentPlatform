package cs2.tournamentsite.tournamentserver.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cs2.tournamentsite.tournamentserver.models.Event;
import cs2.tournamentsite.tournamentserver.models.Player;
import cs2.tournamentsite.tournamentserver.models.Team;
import cs2.tournamentsite.tournamentserver.services.EventService;
import cs2.tournamentsite.tournamentserver.services.FileStorageService;
import cs2.tournamentsite.tournamentserver.services.PlayerService;
import cs2.tournamentsite.tournamentserver.services.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final EventService eventService;

    @PostMapping("/teams/{teamId}/logo")
    public ResponseEntity<?> uploadTeamLogo(
            @PathVariable Integer teamId,
            @RequestParam("file") MultipartFile file) {
        try {
            Team team = teamService.findTeamById(teamId);
            if (team == null) {
                return ResponseEntity.notFound().build();
            }

            // Delete old logo if exists
            if (team.getLogoPath() != null) {
                fileStorageService.deleteFile(team.getLogoPath());
            }

            // Store new logo
            String filePath = fileStorageService.storeFile(file, "teams");
            team.setLogoPath(filePath);
            teamService.saveTeam(team);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logo uploaded successfully");
            response.put("filePath", filePath);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error uploading team logo", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/players/{playerId}/photo")
    public ResponseEntity<?> uploadPlayerPhoto(
            @PathVariable Integer playerId,
            @RequestParam("file") MultipartFile file) {
        try {
            Player player = playerService.findPlayerById(playerId);
            if (player == null) {
                return ResponseEntity.notFound().build();
            }

            // Delete old photo if exists
            if (player.getPhotoPath() != null) {
                fileStorageService.deleteFile(player.getPhotoPath());
            }

            // Store new photo
            String filePath = fileStorageService.storeFile(file, "players");
            player.setPhotoPath(filePath);
            playerService.savePlayer(player);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Photo uploaded successfully");
            response.put("filePath", filePath);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error uploading player photo", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/events/{eventId}/banner")
    public ResponseEntity<?> uploadEventBanner(
            @PathVariable Integer eventId,
            @RequestParam("file") MultipartFile file) {
        try {
            Event event = eventService.findEventById(eventId);
            if (event == null) {
                return ResponseEntity.notFound().build();
            }

            // Delete old banner if exists
            if (event.getBannerPath() != null) {
                fileStorageService.deleteFile(event.getBannerPath());
            }

            // Store new banner
            String filePath = fileStorageService.storeFile(file, "events");
            event.setBannerPath(filePath);
            eventService.saveEvent(event);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Banner uploaded successfully");
            response.put("filePath", filePath);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error uploading event banner", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/teams/{teamId}/logo")
    public ResponseEntity<?> deleteTeamLogo(@PathVariable Integer teamId) {
        try {
            Team team = teamService.findTeamById(teamId);
            if (team == null) {
                return ResponseEntity.notFound().build();
            }

            if (team.getLogoPath() != null) {
                fileStorageService.deleteFile(team.getLogoPath());
                team.setLogoPath(null);
                teamService.saveTeam(team);
            }

            return ResponseEntity.ok(Map.of("message", "Logo deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting team logo", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/players/{playerId}/photo")
    public ResponseEntity<?> deletePlayerPhoto(@PathVariable Integer playerId) {
        try {
            Player player = playerService.findPlayerById(playerId);
            if (player == null) {
                return ResponseEntity.notFound().build();
            }

            if (player.getPhotoPath() != null) {
                fileStorageService.deleteFile(player.getPhotoPath());
                player.setPhotoPath(null);
                playerService.savePlayer(player);
            }

            return ResponseEntity.ok(Map.of("message", "Photo deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting player photo", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/events/{eventId}/banner")
    public ResponseEntity<?> deleteEventBanner(@PathVariable Integer eventId) {
        try {
            Event event = eventService.findEventById(eventId);
            if (event == null) {
                return ResponseEntity.notFound().build();
            }

            if (event.getBannerPath() != null) {
                fileStorageService.deleteFile(event.getBannerPath());
                event.setBannerPath(null);
                eventService.saveEvent(event);
            }

            return ResponseEntity.ok(Map.of("message", "Banner deleted successfully"));
            
        } catch (Exception e) {
            log.error("Error deleting event banner", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
