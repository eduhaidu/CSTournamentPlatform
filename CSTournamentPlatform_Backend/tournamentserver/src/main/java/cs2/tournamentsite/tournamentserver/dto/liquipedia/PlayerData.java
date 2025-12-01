package cs2.tournamentsite.tournamentserver.dto.liquipedia;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerData {
    private String nickname;      // In-game name (id field in wiki)
    private String realName;      // Full name (name field in wiki)
    private String firstName;
    private String lastName;
    private String country;       // Flag field in wiki
    private String role;          // e.g., "Rifler", "AWPer", "IGL"
    private LocalDate joinDate;   // When they joined the team
    private boolean isIGL;     // In-game leader flag
    private String photoUrl;      // URL to player's photo
}
