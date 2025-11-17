package cs2.tournamentsite.tournamentserver.services;

import cs2.tournamentsite.tournamentserver.dto.liquipedia.TeamData;
import cs2.tournamentsite.tournamentserver.dto.liquipedia.TournamentData;
import cs2.tournamentsite.tournamentserver.models.Event;
import cs2.tournamentsite.tournamentserver.models.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiquipediaImportService {

    private final LiquipediaService liquipediaService;
    private final EventService eventService;
    private final TeamService teamService;

    /**
     * Import a single tournament from Liquipedia by page title
     */
    @Transactional
    public Event importTournament(String pageTitle) {
        try {
            log.info("Importing tournament: {}", pageTitle);
            
            // Fetch page content
            String wikiContent = liquipediaService.fetchPageContent(pageTitle);
            if (wikiContent == null) {
                log.error("Failed to fetch page content for: {}", pageTitle);
                return null;
            }

            // Parse tournament data
            TournamentData tournamentData = liquipediaService.parseTournamentData(wikiContent);
            if (tournamentData == null) {
                log.error("Failed to parse tournament data for: {}", pageTitle);
                return null;
            }

            // Convert to Event model
            Event event = new Event();
            event.setName(tournamentData.getName());
            event.setLocation(tournamentData.getLocation());
            event.setStartDate(tournamentData.getStartDate());
            event.setEndDate(tournamentData.getEndDate());
            event.setOrganizer(tournamentData.getOrganizer());
            event.setPrizePool(tournamentData.getPrizePool() != null ? tournamentData.getPrizePool() : 0.0);
            event.setDescription(tournamentData.getDescription());

            // Save to database
            Event savedEvent = eventService.saveEvent(event);
            log.info("Successfully imported tournament: {} (ID: {})", savedEvent.getName(), savedEvent.getId());
            
            return savedEvent;
            
        } catch (Exception e) {
            log.error("Error importing tournament: {}", pageTitle, e);
            return null;
        }
    }

    /**
     * Import a single team from Liquipedia by page title
     */
    @Transactional
    public Team importTeam(String pageTitle) {
        try {
            log.info("Importing team: {}", pageTitle);
            
            // Fetch page content
            String wikiContent = liquipediaService.fetchPageContent(pageTitle);
            if (wikiContent == null) {
                log.error("Failed to fetch page content for: {}", pageTitle);
                return null;
            }

            // Parse team data
            TeamData teamData = liquipediaService.parseTeamData(wikiContent);
            if (teamData == null) {
                log.error("Failed to parse team data for: {}", pageTitle);
                return null;
            }

            // Convert to Team model
            Team team = new Team();
            team.setName(teamData.getName());
            team.setCountry(teamData.getCountry());
            team.setFoundedOn(teamData.getFoundedOn());
            team.setCoachName(teamData.getCoachName());

            // Save to database
            Team savedTeam = teamService.saveTeam(team);
            log.info("Successfully imported team: {} (ID: {})", savedTeam.getName(), savedTeam.getId());
            
            return savedTeam;
            
        } catch (Exception e) {
            log.error("Error importing team: {}", pageTitle, e);
            return null;
        }
    }

    /**
     * Import multiple tournaments
     */
    @Transactional
    public List<Event> importTournaments(List<String> pageTitles) {
        List<Event> importedEvents = new ArrayList<>();
        
        for (String pageTitle : pageTitles) {
            Event event = importTournament(pageTitle);
            if (event != null) {
                importedEvents.add(event);
            }
            
            // Be respectful with API calls - add delay
            try {
                Thread.sleep(1000); // 1 second delay between requests
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Import interrupted");
                break;
            }
        }
        
        log.info("Import completed: {} tournaments imported", importedEvents.size());
        return importedEvents;
    }

    /**
     * Import multiple teams
     */
    @Transactional
    public List<Team> importTeams(List<String> pageTitles) {
        List<Team> importedTeams = new ArrayList<>();
        
        for (String pageTitle : pageTitles) {
            Team team = importTeam(pageTitle);
            if (team != null) {
                importedTeams.add(team);
            }
            
            // Be respectful with API calls - add delay
            try {
                Thread.sleep(1000); // 1 second delay between requests
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Import interrupted");
                break;
            }
        }
        
        log.info("Import completed: {} teams imported", importedTeams.size());
        return importedTeams;
    }
}
