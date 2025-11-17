package cs2.tournamentsite.tournamentserver.config;

import java.util.Arrays;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Create HttpClient with automatic Gzip decompression
        CloseableHttpClient httpClient = HttpClients.custom()
                .build();
        
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(10000); // 10 seconds
        
        // Add User-Agent and Accept-Encoding headers for Liquipedia API
        ClientHttpRequestInterceptor headerInterceptor = (request, body, execution) -> {
            request.getHeaders().add("User-Agent", "CS2TournamentPlatform/1.0 (Educational Project; edu.haidu@gmail.com)");
            request.getHeaders().add("Accept-Encoding", "gzip");
            return execution.execute(request, body);
        };
        
        RestTemplate restTemplate = builder
                .requestFactory(() -> factory)
                .build();
        
        restTemplate.setInterceptors(Arrays.asList(headerInterceptor));
        
        return restTemplate;
    }
}
