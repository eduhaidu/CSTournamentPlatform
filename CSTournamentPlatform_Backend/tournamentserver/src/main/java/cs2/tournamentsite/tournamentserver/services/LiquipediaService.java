package cs2.tournamentsite.tournamentserver.services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.InvalidUrlException;
import org.springframework.web.util.UriComponentsBuilder;

import cs2.tournamentsite.tournamentserver.dto.liquipedia.MapData;
import cs2.tournamentsite.tournamentserver.dto.liquipedia.MatchData;
import cs2.tournamentsite.tournamentserver.dto.liquipedia.MediaWikiResponse;
import cs2.tournamentsite.tournamentserver.dto.liquipedia.PlayerData;
import cs2.tournamentsite.tournamentserver.dto.liquipedia.TeamData;
import cs2.tournamentsite.tournamentserver.dto.liquipedia.TournamentData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiquipediaService {

    private static final String API_URL = "https://liquipedia.net/counterstrike/api.php";

    private final RestTemplate restTemplate;

    /**
     * Fetches a page from Liquipedia MediaWiki API
     */
    public String fetchPageContent(String pageTitle) {
        try {
            String url = UriComponentsBuilder.fromUriString(API_URL)
                    .queryParam("action", "query")
                    .queryParam("prop", "revisions")
                    .queryParam("rvprop", "content")
                    .queryParam("titles", pageTitle)
                    .queryParam("format", "json")
                    .queryParam("formatversion", "2")
                    .toUriString();

            log.info("Fetching Liquipedia page: {}", pageTitle);
            log.info("API URL: {}", url);

            // Try to get raw response as String first to debug
            // String rawResponse = restTemplate.getForObject(url, String.class);
            // log.info("Raw API Response: {}", rawResponse);

            MediaWikiResponse response = restTemplate.getForObject(url, MediaWikiResponse.class);

            if (response != null && response.getQuery() != null && response.getQuery().getPages() != null) {
                List<MediaWikiResponse.Page> pages = response.getQuery().getPages();
                if (pages.isEmpty()) {
                    log.warn("No pages found for: {}", pageTitle);
                    return null;
                }

                MediaWikiResponse.Page page = pages.get(0);

                if (page.getRevisions() != null && !page.getRevisions().isEmpty()) {
                    return page.getRevisions().get(0).getContent();
                }
            }

            log.warn("No content found for page: {}", pageTitle);
            return null;

        } catch (RestClientException | InvalidUrlException e) {
            log.error("Error fetching Liquipedia page: {}", pageTitle, e);
            return null;
        }
    }

    /**
     * Parses tournament data from MediaWiki content
     */
    public TournamentData parseTournamentData(String wikiContent, String pageTitle) {
        if (wikiContent == null || wikiContent.isEmpty()) {
            return null;
        }

        try {
            TournamentData.TournamentDataBuilder builder = TournamentData.builder();
            List<MatchData> matches = new java.util.ArrayList<>();

            // Parse name from infobox
            String name = extractInfoboxValue(wikiContent, "name");
            builder.name(name != null ? name : "Unknown Tournament");

            // Parse dates
            String startDateStr = extractInfoboxValue(wikiContent, "sdate");
            String endDateStr = extractInfoboxValue(wikiContent, "edate");

            if (startDateStr != null) {
                builder.startDate(parseDate(startDateStr));
            }
            if (endDateStr != null) {
                builder.endDate(parseDate(endDateStr));
            }

            // Parse location
            String location = extractInfoboxValue(wikiContent, "location");
            builder.location(location);

            // Parse organizer
            String organizer = extractInfoboxValue(wikiContent, "organizer");
            builder.organizer(organizer);

            // Parse prize pool
            String prizePoolStr = extractInfoboxValue(wikiContent, "prizepool");
            if (prizePoolStr != null) {
                builder.prizePool(parsePrizePool(prizePoolStr));
            }

            // Extract description (first paragraph after infobox)
            String description = extractDescription(wikiContent);
            builder.description(description);

            // Parse matches
            // Strategy: Try to find bracket in main content first
            // If not found, fetch /Playoffs subpage as fallback

            String contentToSearch = wikiContent;
            boolean fetchedPlayoffs = false;
            
            // First, try to find bracket in main page content
            String bracketContent = extractBracketSection(wikiContent);
            
            // If no bracket found in main page, try /Playoffs subpage
            if (bracketContent == null || bracketContent.isEmpty()) {
                log.info("No bracket found in main page, trying /Playoffs subpage");
                String playoffsContent = fetchPageContent(pageTitle + "/Playoffs");
                if (playoffsContent != null && !playoffsContent.isEmpty()) {
                    contentToSearch = playoffsContent;
                    fetchedPlayoffs = true;
                    log.info("Successfully fetched /Playoffs subpage content");
                    bracketContent = extractBracketSection(playoffsContent);
                } else {
                    log.warn("Failed to fetch /Playoffs subpage for: {}", pageTitle);
                }
            }
            
            // Now look for ===Results=== or ===Playoffs=== or ==Results== section
            int resultsStart = contentToSearch.indexOf("===Results===");
            if (resultsStart == -1) {
                resultsStart = contentToSearch.indexOf("===Playoffs===");
            }
            if (resultsStart == -1) {
                resultsStart = contentToSearch.indexOf("==Results==");
            }
            
            if (resultsStart != -1 || (bracketContent != null && !bracketContent.isEmpty())) {
                String resultsSection = null;
                
                if (resultsStart != -1) {
                    log.info("Found Results/Playoffs section at position {}", resultsStart);
                    int resultsEnd = contentToSearch.indexOf("===", resultsStart + 13);
                    if (resultsEnd == -1) {
                        resultsEnd = contentToSearch.indexOf("==", resultsStart + 11);
                    }
                    resultsSection = resultsEnd != -1 && resultsEnd > resultsStart
                            ? contentToSearch.substring(resultsStart, resultsEnd)
                            : contentToSearch.substring(resultsStart, Math.min(resultsStart + 10000, contentToSearch.length()));
                    
                    log.info("Results section length: {}", resultsSection.length());
                    log.debug("Results section preview: {}", resultsSection.substring(0, Math.min(500, resultsSection.length())));
                    
                    // Try to extract bracket from results section if not already found
                    if (bracketContent == null || bracketContent.isEmpty()) {
                        bracketContent = extractBracketSection(resultsSection);
                    }
                }
                
                // Extract and parse bracket
                if (bracketContent != null && !bracketContent.isEmpty()) {
                    log.info("Found bracket content, length: {}", bracketContent.length());
                    Pattern roundMatchPattern = Pattern.compile("\\|R(\\d+)M(\\d+)=");
                    Matcher matcher = roundMatchPattern.matcher(bracketContent);
                    int matchCount = 0;
                    while (matcher.find()) {
                        // Use matcher.end() to get position right after |R1M1=
                        String substringFromMatch = bracketContent.substring(matcher.end());
                        String matchTemplate = extractTemplateSection(substringFromMatch, "Match");
                        MatchData match = parseMatchData(matchTemplate);
                        if (match != null) {
                            match.setRoundId(Integer.parseInt(matcher.group(1)));
                            match.setMatchId(Integer.parseInt(matcher.group(2)));
                            matches.add(match);
                            matchCount++;
                            log.info("Parsed match {}: {} vs {} (Round {}, Match {})", matchCount, match.getTeamA(), match.getTeamB(),
                                    match.getRoundId(), match.getMatchId());
                        }
                    }
                    log.info("Total matches parsed: {}", matchCount);
                } else {
                    log.warn("No Bracket template found in page content{}", fetchedPlayoffs ? " (including /Playoffs subpage)" : "");
                    // Debug: check what's in the content
                    String searchContent = resultsSection != null ? resultsSection : contentToSearch;
                    if (searchContent.contains("Bracket")) {
                        log.info("Content contains 'Bracket' keyword but extraction failed");
                        int bracketPos = searchContent.indexOf("Bracket");
                        log.debug("Context around Bracket: {}", 
                            searchContent.substring(Math.max(0, bracketPos - 20), 
                                                    Math.min(searchContent.length(), bracketPos + 100)));
                    }
                }
            } else {
                log.warn("No Results/Playoffs section found for tournament: {}", pageTitle);
            }
            builder.matches(matches);
            return builder.build();

        } catch (Exception e) {
            log.error("Error parsing tournament data", e);
            return null;
        }
    }

    /**
     * Parses team data from MediaWiki content
     */
    public TeamData parseTeamData(String wikiContent) {
        if (wikiContent == null || wikiContent.isEmpty()) {
            return null;
        }

        try {
            TeamData.TeamDataBuilder builder = TeamData.builder();

            // Parse team name
            // String name = extractInfoboxValue(wikiContent, "team");
            // if (name == null) {
            // name = extractInfoboxValue(wikiContent, "name");
            // }
            // builder.name(name != null ? name : "Unknown Team");

            String section = extractTemplateSection(wikiContent, "Infobox team");
            String name = extractInfoboxValue(section, "name");
            builder.name(name != null ? name : "Unknown Team");

            // Debug: save wiki content to file
            try {
                String debugPath = "/tmp/liquipedia_"
                        + (name != null ? name.replaceAll("[^a-zA-Z0-9]", "_") : "unknown") + ".txt";
                java.nio.file.Files.writeString(
                        java.nio.file.Paths.get(debugPath),
                        wikiContent);
                log.info("Saved wiki content to: {}", debugPath);
            } catch (IOException e) {
                log.warn("Could not save debug file: {}", e.getMessage());
            }

            // Parse country
            String country = extractInfoboxValue(wikiContent, "country");
            builder.country(country);

            // Parse founded date
            String foundedStr = extractInfoboxValue(wikiContent, "founded");
            if (foundedStr != null) {
                builder.foundedOn(parseLocalDate(foundedStr));
            }

            // Parse coach
            String coach = extractInfoboxValue(wikiContent, "coach");
            builder.coachName(coach);

            // Parse active players from roster section
            List<PlayerData> players = parseActivePlayers(wikiContent);
            builder.players(players);

            return builder.build();

        } catch (Exception e) {
            log.error("Error parsing team data", e);
            return null;
        }
    }

    /**
     * Searches for tournaments by category
     */
    public List<String> searchTournaments(String category) {
        try {
            log.info("Searching tournaments in category: {}", category);

            // This would return a list of page titles
            // For simplicity, returning a hardcoded list of major tournaments
            return Arrays.asList(
                    "Intel_Extreme_Masters",
                    "BLAST_Premier",
                    "ESL_Pro_League",
                    "PGL_Major");

        } catch (Exception e) {
            log.error("Error searching tournaments", e);
            return Collections.emptyList();
        }
    }

    // Helper methods for parsing wiki markup

    private String extractInfoboxValue(String content, String key) {
        Pattern pattern = Pattern.compile("\\|\\s*" + key + "\\s*=\\s*([^\\n\\|]+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String extractDescription(String content) {
        // Extract first paragraph after infobox
        Pattern pattern = Pattern.compile("}}\\s*([^\\{\\n]{50,300})");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "CS2 Tournament";
    }

    private Date parseDate(String dateStr) {
        try {
            // Liquipedia uses formats like "2024-11-17" or "November 17, 2024"
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                return java.sql.Date.valueOf(localDate);
            }
            // Add more date format parsers as needed
            return new Date();
        } catch (Exception e) {
            log.warn("Could not parse date: {}", dateStr);
            return new Date();
        }
    }

    private LocalDate parseLocalDate(String dateStr) {
        try {
            if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            return LocalDate.now();
        } catch (Exception e) {
            log.warn("Could not parse date: {}", dateStr);
            return LocalDate.now();
        }
    }

    private Double parsePrizePool(String prizePoolStr) {
        try {
            // Remove currency symbols and commas
            String cleaned = prizePoolStr.replaceAll("[^0-9.]", "");
            return Double.valueOf(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Could not parse prize pool: {}", prizePoolStr);
            return 0.0;
        }
    }

    /**
     * Parses active players from team roster section
     * Looks for {{Person|...}} or {{Squad|...}} patterns in wiki markup
     */
    private List<PlayerData> parseActivePlayers(String wikiContent) {
        List<PlayerData> players = new java.util.ArrayList<>();

        if (wikiContent == null || wikiContent.isEmpty()) {
            return players;
        }

        try {
            // Find the Active roster section
            // Pattern: {{Person|flag=xx|id=nickname|name=Full Name|joindate=...}}
            Pattern personPattern = Pattern.compile(
                    "\\{\\{Person\\|([^}]+)\\}\\}",
                    Pattern.CASE_INSENSITIVE);

            // Try multiple section naming patterns
            int activeStart = -1;
            int activeEnd = -1;

            // Try "===Active===" first
            activeStart = wikiContent.indexOf("===Active===");
            if (activeStart != -1) {
                log.debug("Found ===Active=== section at position {}", activeStart);
                activeEnd = wikiContent.indexOf("===", activeStart + 13);
            }

            // Try "==Player Roster==" if not found
            if (activeStart == -1) {
                activeStart = wikiContent.indexOf("==Player Roster==");
                if (activeStart != -1) {
                    log.debug("Found ==Player Roster== section at position {}", activeStart);
                    // Look for the next major section (==)
                    activeEnd = wikiContent.indexOf("\n==", activeStart + 17);
                }
            }

            // Try just looking for {{Squad|status=active pattern
            if (activeStart == -1) {
                activeStart = wikiContent.indexOf("{{Squad|status=active");
                if (activeStart != -1) {
                    log.debug("Found {{Squad|status=active at position {}", activeStart);
                    // Find the closing of this Squad section
                    activeEnd = wikiContent.indexOf("}}", activeStart);
                    if (activeEnd != -1) {
                        // Extend to capture all Person entries in this squad
                        int nextSquad = wikiContent.indexOf("{{Squad", activeStart + 20);
                        activeEnd = nextSquad != -1 ? nextSquad : wikiContent.length();
                    }
                }
            }

            if (activeStart != -1) {
                String activeSection = activeEnd != -1 && activeEnd > activeStart
                        ? wikiContent.substring(activeStart, activeEnd)
                        : wikiContent.substring(activeStart, Math.min(activeStart + 5000, wikiContent.length()));

                log.debug("Searching for players in section of length: {}", activeSection.length());

                Matcher matcher = personPattern.matcher(activeSection);

                while (matcher.find()) {
                    String personData = matcher.group(1);
                    log.debug("Found Person template: {}", personData.substring(0, Math.min(100, personData.length())));
                    PlayerData player = parsePersonData(personData);
                    if (player != null) {
                        players.add(player);
                        log.info("Parsed player: {} ({})", player.getNickname(), player.getRealName());
                    }
                }
            } else {
                log.warn("Could not find Active roster section in wiki content");
            }

            log.info("Parsed {} active players from roster", players.size());

        } catch (Exception e) {
            log.error("Error parsing players from roster", e);
        }

        return players;
    }

    /**
     * Parses individual player data from Person template parameters
     * Format: flag=xx|id=nickname|name=Full
     * Name|joindate=2024-01-01|role=Rifler|igl=y
     */
    private PlayerData parsePersonData(String personData) {
        try {
            PlayerData.PlayerDataBuilder builder = PlayerData.builder();

            // Parse nickname (id field)
            String nickname = extractTemplateParam(personData, "id");
            if (nickname == null || nickname.isEmpty()) {
                return null; // Nickname is required
            }
            builder.nickname(nickname);

            // Parse full name
            String fullName = extractTemplateParam(personData, "name");
            if (fullName != null && !fullName.isEmpty()) {
                String[] nameParts = fullName.split("\\s+", 2);
                builder.firstName(nameParts[0]);
                builder.lastName(nameParts.length > 1 ? nameParts[1] : "");
                builder.realName(fullName);
            } else {
                builder.firstName("");
                builder.lastName("");
                builder.realName("");
            }

            // Parse country (flag field)
            String country = extractTemplateParam(personData, "flag");
            builder.country(country != null ? country.toUpperCase() : "");

            // Parse role
            String role = extractTemplateParam(personData, "role");
            builder.role(role != null ? role : "Player");

            // Check if IGL
            String iglStr = extractTemplateParam(personData, "igl");
            builder.isIGL("y".equalsIgnoreCase(iglStr) || "yes".equalsIgnoreCase(iglStr));
            if (builder.build().isIGL() && (role == null || role.isEmpty())) {
                builder.role("IGL");
            }

            // Parse join date
            String joinDateStr = extractTemplateParam(personData, "joindate");
            if (joinDateStr != null && !joinDateStr.isEmpty()) {
                LocalDate joinDate = parseLocalDate(joinDateStr);
                builder.joinDate(joinDate);
            }

            return builder.build();

        } catch (Exception e) {
            log.warn("Error parsing person data: {}", personData, e);
            return null;
        }
    }

    /**
     * Extracts parameter value from wiki template
     * Format: key=value
     */
    private String extractTemplateParam(String templateData, String key) {
        Pattern pattern = Pattern.compile(key + "\\s*=\\s*([^|]+)");
        Matcher matcher = pattern.matcher(templateData);
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            // Remove wiki links [[...]]
            value = value.replaceAll("\\[\\[([^\\]]+)\\]\\]", "$1");
            return value;
        }
        return null;
    }

    /**
     * Fetches a player's individual page and extracts their photo filename
     * 
     * @param playerNickname The player's nickname/ID
     * @return The image filename from the player's infobox, or null if not found
     */
    public String getPlayerPhotoFilename(String playerNickname) {
        try {
            log.info("Fetching player page for: {}", playerNickname);

            String wikiContent = fetchPageContent(playerNickname);
            if (wikiContent == null || wikiContent.isEmpty()) {
                log.warn("No content found for player: {}", playerNickname);
                return null;
            }

            // Extract image from infobox
            // Pattern: |image=filename
            String imageFilename = extractInfoboxValue(wikiContent, "image");
            if (imageFilename != null && !imageFilename.isEmpty()) {
                log.info("Found photo for {}: {}", playerNickname, imageFilename);
                return imageFilename;
            }

            log.warn("No image found in infobox for player: {}", playerNickname);
            return null;

        } catch (Exception e) {
            log.error("Error fetching player photo for: {}", playerNickname, e);
            return null;
        }
    }

    public String extractTemplateSection(String content, String prefix) {
        String section = "";
        int sectionStart = content.indexOf("{{" + prefix);
        int sectionEnd = -1;
        int openBracketCount = 0;
        int closedBracketCount = 0;
        if (sectionStart == -1) {
            log.debug(prefix + " not found");
            return null;
        }
        openBracketCount++;
        log.debug("Found " + prefix + " at position: " + sectionStart);
        int maxIterations = 10000;
        int iterations = 0;
        int currentIndex = sectionStart + 2;
        while (closedBracketCount < openBracketCount && iterations++ < maxIterations && currentIndex < content.length() - 1) {
            if (content.substring(currentIndex, currentIndex + 2).equals("{{")) {
                openBracketCount++;
            }
            if (content.substring(currentIndex, currentIndex + 2).equals("}}")) {
                closedBracketCount++;
            }
            currentIndex++;
        }
        if (iterations >= maxIterations) {
            log.error("Hit max iterations - possible infinite loop!");
        }
        sectionEnd = currentIndex;
        section = content.substring(sectionStart, sectionEnd);
        return section;
    }

    /**
     * Extracts bracket section with more flexible matching
     * Matches {{Bracket|...}} or {{Bracket/8|...}} etc.
     */
    private String extractBracketSection(String content) {
        // Look for {{Bracket with any suffix (|, /, space, etc.)
        Pattern bracketPattern = Pattern.compile("\\{\\{Bracket[\\|/]");
        Matcher matcher = bracketPattern.matcher(content);
        
        if (!matcher.find()) {
            log.warn("No Bracket template found");
            return null;
        }
        
        int sectionStart = matcher.start();
        int openBracketCount = 1;
        int closedBracketCount = 0;
        int currentIndex = sectionStart + 2;
        int maxIterations = 50000; // Brackets can be very long
        int iterations = 0;
        
        while (closedBracketCount < openBracketCount && iterations++ < maxIterations && currentIndex < content.length() - 1) {
            if (currentIndex + 1 < content.length()) {
                String twoChars = content.substring(currentIndex, currentIndex + 2);
                if (twoChars.equals("{{")) {
                    openBracketCount++;
                    currentIndex++; // Skip next char
                } else if (twoChars.equals("}}")) {
                    closedBracketCount++;
                    currentIndex++; // Skip next char
                }
            }
            currentIndex++;
        }
        
        if (iterations >= maxIterations) {
            log.error("Hit max iterations while extracting Bracket - possible infinite loop!");
            return null;
        }
        
        String bracketContent = content.substring(sectionStart, currentIndex);
        log.info("Extracted bracket section: {} chars, open: {}, closed: {}", 
                bracketContent.length(), openBracketCount, closedBracketCount);
        return bracketContent;
    }

    public MatchData parseMatchData(String matchTemplate) {
        try {
            List<MapData> maps = new java.util.ArrayList<>();
            MatchData.MatchDataBuilder builder = MatchData.builder();
            int teamAScore = 0;
            int teamBScore = 0;
            String teamASection = extractTemplateParam(matchTemplate, "opponent1");
            String teamBSection = extractTemplateParam(matchTemplate, "opponent2");
            String teamA = null;
            String teamB = null;
            if (teamASection != null) {
                String TeamOpponentTemplate = extractTemplateSection(teamASection, "TeamOpponent");
                if (TeamOpponentTemplate != null) {
                    Pattern namePattern = Pattern.compile("TeamOpponent\\|(\\w+)");
                    Matcher nameMatcher = namePattern.matcher(TeamOpponentTemplate);
                    if (nameMatcher.find()) {
                        teamA = nameMatcher.group(1).trim();
                    }
                }
            }
            if (teamBSection != null) {
                String TeamOpponentTemplate = extractTemplateSection(teamBSection, "TeamOpponent");
                if (TeamOpponentTemplate != null) {
                    Pattern namePattern = Pattern.compile("TeamOpponent\\|(\\w+)");
                    Matcher nameMatcher = namePattern.matcher(TeamOpponentTemplate);
                    if (nameMatcher.find()) {
                        teamB = nameMatcher.group(1).trim();
                    }
                }
            }
            String matchDateStr = extractTemplateParam(matchTemplate, "date");
            if (matchDateStr != null) {
                builder.matchDate(parseDate(matchDateStr).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            }
            // Parse maps
            Pattern mapPattern = Pattern.compile("\\|map(\\d+)=");
            Matcher mapMatcher = mapPattern.matcher(matchTemplate);
            while (mapMatcher.find()) {
                // Use matcher.end() to handle |map1= through |map99=
                String substringFromMap = matchTemplate.substring(mapMatcher.end());
                String mapTemplate = extractTemplateSection(substringFromMap, "Map");
                MapData mapData = parseMapData(mapTemplate);
                if (mapData != null) {
                    maps.add(mapData);
                    if ("A".equals(mapData.getWinner())) {
                        teamAScore++;
                    } else if ("B".equals(mapData.getWinner())) {
                        teamBScore++;
                    }
                }
            }
            builder.maps(maps);
            String winner = teamAScore > teamBScore ? teamA : teamB;
            builder.teamA(teamA != null ? teamA : "Team A");
            builder.teamB(teamB != null ? teamB : "Team B");
            builder.scoreA(teamAScore);
            builder.scoreB(teamBScore);
            builder.winner(winner);
            return builder.build();

        } catch (Exception e) {
            log.error("Error parsing match data: {}", matchTemplate, e);
        }
        return null;
    }

    private MapData parseMapData(String mapTemplate) {
        try {
            MapData.MapDataBuilder builder = MapData.builder();
            String mapName = extractTemplateParam(mapTemplate, "map");
            String teamATSideScoreStr = extractTemplateParam(mapTemplate, "t1t");
            String teamBTSideScoreStr = extractTemplateParam(mapTemplate, "t2t");
            String teamACTSideScoreStr = extractTemplateParam(mapTemplate, "t1ct");
            String teamBCTSideScoreStr = extractTemplateParam(mapTemplate, "t2ct");
            int teamATSideScore = teamATSideScoreStr != null ? Integer.parseInt(teamATSideScoreStr) : 0;
            int teamBTSideScore = teamBTSideScoreStr != null ? Integer.parseInt(teamBTSideScoreStr) : 0;
            int teamACTSideScore = teamACTSideScoreStr != null ? Integer.parseInt(teamACTSideScoreStr) : 0;
            int teamBCTSideScore = teamBCTSideScoreStr != null ? Integer.parseInt(teamBCTSideScoreStr) : 0;
            int teamAFinalScore = teamATSideScore + teamACTSideScore;
            int teamBFinalScore = teamBTSideScore + teamBCTSideScore;

            builder.mapName(mapName != null ? mapName : "Unknown Map");
            builder.teamATSideScore(teamATSideScore);
            builder.teamBTSideScore(teamBTSideScore);
            builder.teamACTSideScore(teamACTSideScore);
            builder.teamBCTSideScore(teamBCTSideScore);
            builder.teamAFinalScore(teamAFinalScore);
            builder.teamBFinalScore(teamBFinalScore);
            String winner = teamAFinalScore > teamBFinalScore ? "A" : "B";
            builder.winner(winner);
            return builder.build();

        } catch (NumberFormatException e) {
            log.error("Error parsing map data: {}", mapTemplate, e);
            return null;
        }
    }

    /**
     * Determines the tournament stage based on bracket round number and total teams
     * Follows standard single-elimination bracket naming
     */
    public String determineStageFromRound(int roundId, int totalMatchesInBracket) {
        // Calculate total teams from total matches in bracket
        // Single elimination: 8 teams = 7 matches (4 QF + 2 SF + 1 GF)
        // With 3rd place: 8 teams = 8 matches (4 QF + 2 SF + 1 GF + 1 3rd)
        
        if (roundId == 1) {
            // Round 1 could be Ro32, Ro16, or Quarterfinals depending on bracket size
            if (totalMatchesInBracket >= 15) { // 16 teams
                return "Round of 16";
            } else if (totalMatchesInBracket >= 7) { // 8 teams
                return "Quarterfinals";
            } else if (totalMatchesInBracket >= 3) { // 4 teams
                return "Semifinals";
            }
            return "Playoffs";
        } else if (roundId == 2) {
            if (totalMatchesInBracket >= 15) { // 16 teams -> R2 is QF
                return "Quarterfinals";
            } else if (totalMatchesInBracket >= 7) { // 8 teams -> R2 is SF
                return "Semifinals";
            }
            return "Grand Final";
        } else if (roundId == 3) {
            if (totalMatchesInBracket >= 15) { // 16 teams -> R3 is SF
                return "Semifinals";
            }
            return "Grand Final";
        } else if (roundId == 4) {
            return "Grand Final";
        }
        
        // Handle special cases like Third Place Match (RxMTP identifier)
        return "Playoffs";
    }

}
