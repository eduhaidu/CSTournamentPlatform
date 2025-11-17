package cs2.tournamentsite.tournamentserver.dto.liquipedia;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MediaWikiResponse {
    
    @JsonProperty("query")
    private Query query;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Query {
        @JsonProperty("pages")
        private List<Page> pages;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Page {
        @JsonProperty("pageid")
        private Integer pageId;
        
        @JsonProperty("title")
        private String title;
        
        @JsonProperty("revisions")
        private List<Revision> revisions;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Revision {
        @JsonProperty("content")
        private String content;
        
        @JsonProperty("contentformat")
        private String contentFormat;
        
        @JsonProperty("contentmodel")
        private String contentModel;
    }
}
