package cs2.tournamentsite.tournamentserver.dto.liquipedia;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MapData {
    private String mapName;
    private String winner;
    private Integer teamAFinalScore;
    private Integer teamBFinalScore;
    private String teamA;
    private String teamB;
    private Integer mapNumber;
    private Integer teamATSideScore;
    private Integer teamBTSideScore;
    private Integer teamACTSideScore;
    private Integer teamBCTSideScore;
}
