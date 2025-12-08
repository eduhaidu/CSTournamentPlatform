package cs2.tournamentsite.tournamentserver.models;

import java.sql.Time;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer tournamentId;
    private Integer teamAId;
    private Integer teamBId;
    private Time scheduledTime;
    private Date playedOn;
    private String result;
    private String status;
    private Integer teamAScore;
    private Integer teamBScore;
    private Integer winnerTeamId;
    private String matchType; // "Single Elimination", "Best of 3", "Best of 5", etc.
}
