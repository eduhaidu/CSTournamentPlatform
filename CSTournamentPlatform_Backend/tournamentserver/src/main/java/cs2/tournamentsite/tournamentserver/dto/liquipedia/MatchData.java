package cs2.tournamentsite.tournamentserver.dto.liquipedia;

import lombok.Data;
import lombok.Builder;
import cs2.tournamentsite.tournamentserver.dto.liquipedia.MapData;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MatchData {
    private Integer roundId;
    private Integer matchId;
    private String teamA;
    private String teamB;
    private Integer scoreA;
    private Integer scoreB;
    private String winner;
    private List<MapData> maps;
    private LocalDateTime matchDate;
    private Integer totalMaps; // Total number of maps (Best of X)
}
