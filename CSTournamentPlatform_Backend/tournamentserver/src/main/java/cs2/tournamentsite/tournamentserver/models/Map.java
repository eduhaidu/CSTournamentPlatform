package cs2.tournamentsite.tournamentserver.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="maps")
public class Map{
    @Id
    @GeneratedValue(strategy=jakarta.persistence.GenerationType.IDENTITY)
    private Integer id;
    private String mapName;
    private Integer matchId;
    private Integer teamAFinalScore;
    private Integer teamBFinalScore;
    private Integer teamATRounds;
    private Integer teamACTRounds;
    private Integer teamBTRounds;
    private Integer teamBCTRounds;
}