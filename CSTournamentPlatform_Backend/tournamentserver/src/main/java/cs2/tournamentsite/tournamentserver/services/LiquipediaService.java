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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import cs2.tournamentsite.tournamentserver.dto.liquipedia.MediaWikiResponse;
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
            
        } catch (Exception e) {
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
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            log.warn("Could not parse prize pool: {}", prizePoolStr);
            return 0.0;
        }
    }
}
