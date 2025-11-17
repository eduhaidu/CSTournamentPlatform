package cs2.tournamentsite.tournamentserver.dto.liquipedia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamData {
    private String name;
    private String country;
    private LocalDate foundedOn;
    private String coachName;
}
