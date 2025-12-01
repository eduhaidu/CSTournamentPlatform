package cs2.tournamentsite.tournamentserver.services;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cs2.tournamentsite.tournamentserver.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiquipediaImageService {

    private static final String API_URL = "https://liquipedia.net/counterstrike/api.php";
    private static final String USER_AGENT = "CSTournamentPlatform/1.0 (Educational Project)";
    
    private final RestTemplate restTemplate;
    private final FileStorageProperties fileStorageProperties;
    private final ObjectMapper objectMapper;

    /**
     * Extracts image filename from wiki content infobox
     */
    public String extractImageFilename(String wikiContent, String imageKey) {
        if (wikiContent == null || wikiContent.isEmpty()) {
            return null;
        }

        // Pattern to match |image=filename or |logo=filename
        Pattern pattern = Pattern.compile("\\|\\s*" + imageKey + "\\s*=\\s*([^\\n\\|]+)");
        Matcher matcher = pattern.matcher(wikiContent);
        
        if (matcher.find()) {
            String filename = matcher.group(1).trim();
            // Remove any wiki markup
            filename = filename.replaceAll("\\[\\[File:", "").replaceAll("\\]\\]", "");
            return filename;
        }
        
        return null;
    }

    /**
     * Gets the actual image URL from Liquipedia using MediaWiki imageinfo API
     */
    public String getImageUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        try {
            String url = API_URL + "?action=query&format=json&formatversion=2"
                    + "&prop=imageinfo&iiprop=url"
                    + "&titles=File:" + URLEncoder.encode(filename, StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            headers.set("Accept-Encoding", "gzip");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            // Parse JSON response
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode pages = root.path("query").path("pages");
            
            if (pages.isArray() && pages.size() > 0) {
                JsonNode page = pages.get(0);
                JsonNode imageinfo = page.path("imageinfo");
                
                if (imageinfo.isArray() && imageinfo.size() > 0) {
                    String imageUrl = imageinfo.get(0).path("url").asText();
                    log.info("Found image URL for {}: {}", filename, imageUrl);
                    return imageUrl;
                }
            }
            
            log.warn("No image URL found for: {}", filename);
            return null;
            
        } catch (JsonProcessingException | RestClientException e) {
            log.error("Error getting image URL for: {}", filename, e);
            return null;
        }
    }

    /**
     * Downloads image from URL and saves it to the uploads directory
     * Returns the relative path to the saved image
     */
    public String downloadAndSaveImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            log.info("Downloading image from: {}", imageUrl);
            
            // Get file extension from URL
            String extension = getFileExtension(imageUrl);
            
            // Generate unique filename
            String filename = UUID.randomUUID().toString() + extension;
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            
            // Download image
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                imageUrl, 
                HttpMethod.GET, 
                entity, 
                byte[].class
            );
            
            if (response.getBody() != null) {
                // Save to file
                Path targetLocation = uploadPath.resolve(filename);
                Files.write(targetLocation, response.getBody());
                
                log.info("Image saved to: {}", filename);
                return "/uploads/" + filename; // Return the web-accessible path
            }
            
            return null;
            
        } catch (IOException | RestClientException e) {
            log.error("Error downloading image from: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * Downloads team logo from Liquipedia
     */
    public String downloadTeamLogo(String wikiContent) {
        // Try different image keys that teams might use
        String[] imageKeys = {"image", "logo", "teamlogo"};
        
        for (String key : imageKeys) {
            String filename = extractImageFilename(wikiContent, key);
            if (filename != null) {
                String imageUrl = getImageUrl(filename);
                if (imageUrl != null) {
                    String savedPath = downloadAndSaveImage(imageUrl);
                    if (savedPath != null) {
                        return savedPath;
                    }
                }
            }
        }
        
        log.warn("No team logo found in wiki content");
        return null;
    }

    /**
     * Downloads event/tournament banner from Liquipedia
     */
    public String downloadEventBanner(String wikiContent) {
        // Try different image keys that events might use
        String[] imageKeys = {"image", "logo", "banner"};
        
        for (String key : imageKeys) {
            String filename = extractImageFilename(wikiContent, key);
            if (filename != null) {
                String imageUrl = getImageUrl(filename);
                if (imageUrl != null) {
                    String savedPath = downloadAndSaveImage(imageUrl);
                    if (savedPath != null) {
                        return savedPath;
                    }
                }
            }
        }
        
        log.warn("No event banner found in wiki content");
        return null;
    }

    /**
     * Downloads player photo from Liquipedia given an image filename
     * @param imageFilename The filename from the player's infobox
     * @return The path to the saved image (e.g., /uploads/uuid.png), or null if failed
     */
    public String downloadPlayerPhoto(String imageFilename) {
        if (imageFilename == null || imageFilename.isEmpty()) {
            return null;
        }
        
        String imageUrl = getImageUrl(imageFilename);
        if (imageUrl != null) {
            String savedPath = downloadAndSaveImage(imageUrl);
            if (savedPath != null) {
                log.info("Downloaded player photo: {}", savedPath);
                return savedPath;
            }
        }
        
        log.warn("Failed to download player photo: {}", imageFilename);
        return null;
    }

    /**
     * Extracts file extension from URL
     */
    private String getFileExtension(String url) {
        String lowerUrl = url.toLowerCase();
        
        if (lowerUrl.contains(".png")) return ".png";
        if (lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg")) return ".jpg";
        if (lowerUrl.contains(".gif")) return ".gif";
        if (lowerUrl.contains(".webp")) return ".webp";
        if (lowerUrl.contains(".svg")) return ".svg";
        
        return ".png"; // Default to PNG
    }
}
