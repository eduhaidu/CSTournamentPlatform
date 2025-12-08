package cs2.tournamentsite.tournamentserver.dto.liquipedia;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamData {
    private String name;
    private String country;
    private LocalDate foundedOn;
    private String coachName;
    private List<PlayerData> players;
    private String pageTitle;
}
