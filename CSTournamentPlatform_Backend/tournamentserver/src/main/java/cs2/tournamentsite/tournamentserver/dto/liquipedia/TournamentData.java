package cs2.tournamentsite.tournamentserver.dto.liquipedia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentData {
    private String name;
    private String location;
    private Date startDate;
    private Date endDate;
    private String organizer;
    private Double prizePool;
    private String description;
    private List<MatchData> matches;
}
