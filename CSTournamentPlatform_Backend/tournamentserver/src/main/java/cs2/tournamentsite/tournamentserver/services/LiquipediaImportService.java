package cs2.tournamentsite.tournamentserver.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cs2.tournamentsite.tournamentserver.dto.liquipedia.MapData;
import cs2.tournamentsite.tournamentserver.dto.liquipedia.MatchData;
import cs2.tournamentsite.tournamentserver.dto.liquipedia.PlayerData;
import cs2.tournamentsite.tournamentserver.dto.liquipedia.TeamData;
import cs2.tournamentsite.tournamentserver.dto.liquipedia.TournamentData;
import cs2.tournamentsite.tournamentserver.models.Event;
import cs2.tournamentsite.tournamentserver.models.Map;
import cs2.tournamentsite.tournamentserver.models.Match;
import cs2.tournamentsite.tournamentserver.models.Player;
import cs2.tournamentsite.tournamentserver.models.Team;
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiquipediaImportService {

    private final LiquipediaService liquipediaService;
    private final LiquipediaImageService liquipediaImageService;
    private final EventService eventService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final MatchService matchService;
    private final MapService mapService;

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
            TournamentData tournamentData = liquipediaService.parseTournamentData(wikiContent, pageTitle);
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

            // Download and save event banner
            try {
                String bannerPath = liquipediaImageService.downloadEventBanner(wikiContent);
                if (bannerPath != null) {
                    event.setBannerPath(bannerPath);
                    log.info("Downloaded event banner: {}", bannerPath);
                }
            } catch (Exception e) {
                log.warn("Failed to download event banner: {}", e.getMessage());
            }

            // Save to database
            Event savedEvent = eventService.saveEvent(event);
            log.info("Successfully imported tournament: {} (ID: {})", savedEvent.getName(), savedEvent.getId());

            if(tournamentData.getMatches() != null && !tournamentData.getMatches().isEmpty()){
                int importedMatches = 0;
                int importedMaps = 0;
                int totalMatches = tournamentData.getMatches().size();
                
                for(MatchData matchData : tournamentData.getMatches()){
                    try {
                        Match match = new Match();
                        match.setTournamentId(savedEvent.getId());
                        match.setTeamAId(getTeamIdFromBracket(matchData.getTeamA()));
                        match.setTeamBId(getTeamIdFromBracket(matchData.getTeamB()));
                        match.setTeamAScore(matchData.getScoreA());
                        match.setTeamBScore(matchData.getScoreB());
                        match.setMatchDate(matchData.getMatchDate());
                        
                        if(matchData.getWinner() != null){
                            match.setWinnerTeamId(getTeamIdFromBracket(matchData.getWinner()));
                        }
                        
                        // Determine stage from round info
                        if(matchData.getRoundId() != null){
                            String stage = liquipediaService.determineStageFromRound(
                                matchData.getRoundId(), 
                                totalMatches
                            );
                            match.setStage(stage);
                        } else {
                            match.setStage("Playoffs");
                        }
                        
                        if(matchData.getMaps() != null && !matchData.getMaps().isEmpty()){
                            
                            for(MapData mapData : matchData.getMaps()){
                                try {
                                    Map map = new Map();
                                    map.setMapName(mapData.getMapName());
                                    map.setMatchId(match.getId());
                                    map.setTeamACTRounds(mapData.getTeamBCTSideScore());
                                    map.setTeamBTRounds(mapData.getTeamBTSideScore());
                                    map.setTeamATRounds(mapData.getTeamATSideScore());
                                    map.setTeamBCTRounds(mapData.getTeamBCTSideScore());
                                    map.setTeamAFinalScore(mapData.getTeamAFinalScore());
                                    map.setTeamBFinalScore(mapData.getTeamBFinalScore());
                                    mapService.saveMap(map);
                                    importedMaps++;
                                } catch (Exception e) {

                                }
                            }
                        }
                        if(importedMaps == 1){
                            match.setMatchType("Best of 1");
                        } else if(importedMaps > 1){
                            match.setMatchType("Best of " + importedMaps);
                        } else {
                            match.setMatchType("Unknown");
                        }
                        matchService.saveMatch(match);
                        importedMatches++;
                    } catch (Exception e) {
                    }
                }
                log.info("Imported {} matches with a total of {} maps for tournament {}", importedMatches, importedMaps, savedEvent.getName());
            }

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
            team.setPageTitle(pageTitle);
            
            // Extract and set alias from page title (e.g., "Team_Spirit" -> "spirit")
            String alias = extractTeamAlias(pageTitle);
            team.setAlias(alias);
            log.info("Team alias set to: {}", alias);

            // Download and save team logo
            try {
                String logoPath = liquipediaImageService.downloadTeamLogo(wikiContent);
                if (logoPath != null) {
                    team.setLogoPath(logoPath);
                    log.info("Downloaded team logo: {}", logoPath);
                }
            } catch (Exception e) {
                log.warn("Failed to download team logo: {}", e.getMessage());
            }

            // Save to database
            Team savedTeam = teamService.saveTeam(team);
            log.info("Successfully imported team: {} (ID: {})", savedTeam.getName(), savedTeam.getId());

            // Import players for this team
            if (teamData.getPlayers() != null && !teamData.getPlayers().isEmpty()) {
                int importedPlayers = 0;
                for (PlayerData playerData : teamData.getPlayers()) {
                    try {
                        Player player = new Player();
                        player.setNickname(playerData.getNickname());
                        player.setFirstName(playerData.getFirstName() != null ? playerData.getFirstName() : "");
                        player.setLastName(playerData.getLastName() != null ? playerData.getLastName() : "");
                        player.setCountry(playerData.getCountry() != null ? playerData.getCountry() : "");
                        player.setTeamId(savedTeam.getId());
                        player.setRole(playerData.getRole() != null ? playerData.getRole() : "Player");
                        player.setJoinedOn(playerData.getJoinDate() != null ? playerData.getJoinDate()
                                : java.time.LocalDate.now());
                        player.setDateOfBirth(java.time.LocalDate.now().minusYears(20)); // Default age ~20, not
                                                                                         // available in Liquipedia

                        // Fetch and download player photo from their individual page
                        try {
                            String photoFilename = liquipediaService.getPlayerPhotoFilename(playerData.getNickname());
                            if (photoFilename != null) {
                                String photoPath = liquipediaImageService.downloadPlayerPhoto(photoFilename);
                                player.setPhotoPath(photoPath != null ? photoPath : "");
                            } else {
                                player.setPhotoPath("");
                            }
                        } catch (Exception e) {
                            log.warn("Failed to download photo for player {}: {}", playerData.getNickname(),
                                    e.getMessage());
                            player.setPhotoPath("");
                        }

                        playerService.savePlayer(player);
                        importedPlayers++;
                        log.debug("Imported player: {} for team {}", player.getNickname(), savedTeam.getName());
                    } catch (Exception e) {
                        log.warn("Failed to import player {}: {}", playerData.getNickname(), e.getMessage());
                    }
                }
                log.info("Imported {} players for team {}", importedPlayers, savedTeam.getName());
            }

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

    @Transactional
    public String testFetchPage(String pageTitle) throws java.io.IOException {
        String wikiContent = liquipediaService.fetchPageContent(pageTitle);
        if (wikiContent != null) {
            log.info(wikiContent);
            try {
                String debugPath = "/tmp/liquipedia_"
                        + (pageTitle != null ? pageTitle.replaceAll("[^a-zA-Z0-9]", "_") : "unknown") + ".txt";
                java.nio.file.Files.writeString(
                        java.nio.file.Paths.get(debugPath),
                        wikiContent);
                log.info("Saved wiki content to: {}", debugPath);
            } catch (IOException e) {
                log.warn("Could not save debug file: {}", e.getMessage());
            }
        }
        return "Failed to fetch content for page: " + pageTitle;
    }

    public Integer getTeamIdFromBracket(String bracketName){
        if(bracketName == null || bracketName.isEmpty()){
            return null;
        }
        
        log.debug("Looking up team from bracket name: {}", bracketName);
        
        // Strategy 1: Try exact alias match (most reliable for bracket identifiers)
        Team team = teamService.findByAlias(bracketName);
        if(team != null){
            log.debug("Found team by alias: {} -> {} (ID: {})", bracketName, team.getName(), team.getId());
            return team.getId();
        }
        
        // Strategy 2: Try normalized alias match (handle case variations)
        String normalized = normalizeTeamIdentifier(bracketName);
        team = teamService.findByAlias(normalized);
        if(team != null){
            log.debug("Found team by normalized alias: {} -> {} (ID: {})", bracketName, team.getName(), team.getId());
            return team.getId();
        }
        
        // Strategy 3: Try name search (partial match)
        List<Team> matchingTeams = teamService.searchTeamsByName(bracketName);
        if(matchingTeams != null && !matchingTeams.isEmpty()){
            // Filter out academy/youth teams
            String bracketLower = bracketName.toLowerCase();
            for(Team t : matchingTeams){
                String teamName = t.getName().toLowerCase();
                if(!bracketLower.contains("academy")
                    && !bracketLower.contains("youth")
                    && !bracketLower.contains("junior")
                    && !bracketLower.contains("women")
                    && !bracketLower.contains("reserve")){
                    log.debug("Found team by name search: {} -> {} (ID: {})", bracketName, t.getName(), t.getId());
                    return t.getId();
                }
            }
        }
        
        // Strategy 4: Try page title search as last resort
        matchingTeams = teamService.searchTeamsByPageTitle(bracketName);
        if(matchingTeams != null && !matchingTeams.isEmpty()){
            log.debug("Found team by page title: {} -> {} (ID: {})", bracketName, matchingTeams.get(0).getName(), matchingTeams.get(0).getId());
            return matchingTeams.get(0).getId();
        }
        
        log.warn("No team found for bracket identifier: {}", bracketName);
        return null;
    }
    
    /**
     * Extracts a team alias from Liquipedia page title
     * Examples: "Team_Spirit" -> "spirit", "MOUZ" -> "mouz", "Natus_Vincere" -> "navi"
     */
    private String extractTeamAlias(String pageTitle) {
        if(pageTitle == null || pageTitle.isEmpty()){
            return null;
        }
        
        // Common team name mappings for well-known teams
        String lower = pageTitle.toLowerCase().replace("_", "");
        
        // Check for common abbreviations in the page title itself
        if(pageTitle.contains("_")){
            String[] parts = pageTitle.split("_");
            if(parts.length > 1){
                // For "Team_Spirit" -> use "Spirit"
                // For "Natus_Vincere" -> use "Vincere" (but we'll handle special cases below)
                return parts[parts.length - 1].toLowerCase();
            }
        }
        
        // Return the normalized page title as alias
        return normalizeTeamIdentifier(pageTitle);
    }
    
    /**
     * Normalizes team identifier for matching
     * Removes common prefixes, underscores, and converts to lowercase
     */
    private String normalizeTeamIdentifier(String identifier) {
        if(identifier == null){
            return null;
        }
        
        String normalized = identifier.toLowerCase()
            .replace("_", "")
            .replace(" ", "")
            .replace("-", "")
            .replace("team", "")
            .replace("esports", "")
            .replace("gaming", "")
            .trim();
        
        return normalized;
    }
}
