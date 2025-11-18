package cs2.tournamentsite.tournamentserver.services;

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
            String rawResponse = restTemplate.getForObject(url, String.class);
            log.info("Raw API Response: {}", rawResponse);
            
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
    public TournamentData parseTournamentData(String wikiContent) {
        if (wikiContent == null || wikiContent.isEmpty()) {
            return null;
        }

        try {
            TournamentData.TournamentDataBuilder builder = TournamentData.builder();

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
            String name = extractInfoboxValue(wikiContent, "team");
            if (name == null) {
                name = extractInfoboxValue(wikiContent, "name");
            }
            builder.name(name != null ? name : "Unknown Team");

            // Debug: save wiki content to file
            try {
                String debugPath = "/tmp/liquipedia_" + (name != null ? name.replaceAll("[^a-zA-Z0-9]", "_") : "unknown") + ".txt";
                java.nio.file.Files.writeString(
                    java.nio.file.Paths.get(debugPath),
                    wikiContent
                );
                log.info("Saved wiki content to: {}", debugPath);
            } catch (Exception e) {
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
                "PGL_Major"
            );
            
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
        } catch (Exception e) {
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
                Pattern.CASE_INSENSITIVE
            );
            
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
     * Format: flag=xx|id=nickname|name=Full Name|joindate=2024-01-01|role=Rifler|igl=y
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
}
