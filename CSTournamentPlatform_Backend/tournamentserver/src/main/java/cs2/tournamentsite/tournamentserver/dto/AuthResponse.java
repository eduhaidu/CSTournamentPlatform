package cs2.tournamentsite.tournamentserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
    
    private Integer id;
    private String username;
    private String email;
    
    // Convenience constructor that sets type automatically
    public AuthResponse(String token, Integer id, String username, String email) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
    }
}
