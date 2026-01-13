import { useState, useEffect } from "react";
import axios from "../config/axios";
import "../styles/MatchDetails.css";

interface MapData {
    id: number;
    mapName: string;
    matchId: number;
    teamAFinalScore: number;
    teamBFinalScore: number;
    teamATRounds: number;
    teamACTRounds: number;
    teamBTRounds: number;
    teamBCTRounds: number;
}

interface MatchDetailsProps {
    matchId: number;
    teamA: string;
    teamB: string;
    scoreA: number;
    scoreB: number;
    matchDate: string;
    matchType?: string;
    onClose: () => void;
}

export default function MatchDetails({
    matchId,
    teamA,
    teamB,
    scoreA,
    scoreB,
    matchDate,
    matchType,
    onClose
}: MatchDetailsProps) {
    const [maps, setMaps] = useState<MapData[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchMaps = async () => {
            try {
                setLoading(true);
                const response = await axios.get(`/maps/match/${matchId}`);
                setMaps(response.data);
            } catch (error) {
                console.error("Error fetching maps:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchMaps();
    }, [matchId]);

    const getWinnerClass = (teamAScore: number, teamBScore: number, isTeamA: boolean) => {
        if (teamAScore > teamBScore && isTeamA) return "winner";
        if (teamBScore > teamAScore && !isTeamA) return "winner";
        return "";
    };

    return (
        <div className="matchDetailsOverlay" onClick={onClose}>
            <div className="matchDetailsModal" onClick={(e) => e.stopPropagation()}>
                <button className="closeButton" onClick={onClose}>
                    ×
                </button>
                
                <div className="matchHeader">
                    <h2>Match Details</h2>
                    <div className="matchInfo">
                        <div className="matchDate">
                            {new Date(matchDate).toLocaleDateString('en-US', {
                                weekday: 'long',
                                year: 'numeric',
                                month: 'long',
                                day: 'numeric'
                            })}
                        </div>
                        {matchType && <div className="matchType">{matchType}</div>}
                    </div>
                </div>

                <div className="matchScore">
                    <div className={`teamScore ${scoreA > scoreB ? 'winner' : ''}`}>
                        <span className="teamName">{teamA}</span>
                        <span className="score">{scoreA}</span>
                    </div>
                    <div className="scoreSeparator">-</div>
                    <div className={`teamScore ${scoreB > scoreA ? 'winner' : ''}`}>
                        <span className="score">{scoreB}</span>
                        <span className="teamName">{teamB}</span>
                    </div>
                </div>

                <div className="mapsSection">
                    <h3>Maps Played</h3>
                    {loading ? (
                        <div className="loading">Loading maps...</div>
                    ) : maps.length > 0 ? (
                        <div className="mapsList">
                            {maps.map((map, index) => (
                                <div key={map.id} className="mapCard">
                                    <div className="mapHeader">
                                        <span className="mapNumber">Map {index + 1}</span>
                                        <span className="mapName">{map.mapName}</span>
                                    </div>
                                    <div className="mapScore">
                                        <div className={`mapTeamScore ${getWinnerClass(map.teamAFinalScore, map.teamBFinalScore, true)}`}>
                                            <span className="teamName">{teamA}</span>
                                            <span className="finalScore">{map.teamAFinalScore}</span>
                                            <span className="sideScores">
                                                ({map.teamATRounds}T / {map.teamACTRounds}CT)
                                            </span>
                                        </div>
                                        <div className={`mapTeamScore ${getWinnerClass(map.teamAFinalScore, map.teamBFinalScore, false)}`}>
                                            <span className="teamName">{teamB}</span>
                                            <span className="finalScore">{map.teamBFinalScore}</span>
                                            <span className="sideScores">
                                                ({map.teamBTRounds}T / {map.teamBCTRounds}CT)
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="noMaps">No map details available</div>
                    )}
                </div>
            </div>
        </div>
    );
}