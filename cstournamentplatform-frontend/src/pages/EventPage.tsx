import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import "../styles/EventPage.css"
import axios from "../config/axios";
import { useAuth } from "../hooks/useAuth";
import TopBar from "../components/TopBar";
import { formatDateRange } from "../utils/dateFormatter";
import { Bracket, type IRoundProps } from "react-brackets";
import MatchDetails from "../components/MatchDetails";

interface Event {
    id: number;
    name: string;
    startDate: string;
    endDate: string;
    location: string;
    bannerPath?: string;
    description?: string;
}

interface Team {
    id: number;
    name: string;
    logoPath?: string;
}

interface Match {
    id: number;
    tournamentId: number;
    teamAId: number;
    teamBId: number;
    teamAScore: number;
    teamBScore: number;
    matchDate: string;
    stage?: string;
    matchType?: string;
    winnerTeamId?: number;
}
export default function EventPage() {
    const { id } = useParams<{ id: string }>();
    const eventId = id ? parseInt(id, 10) : null;
    const [event, setEvent] = useState<Event | null>(null);
    const {user} = useAuth();
    const [matches, setMatches] = useState<Match[]>([]);
    const [teams, setTeams] = useState<Team[]>([]);
    const [selectedMatch, setSelectedMatch] = useState<Match | null>(null);

    useEffect(() => {
        const fetchEvent = async () => {
            try {
                const response = await axios.get(`/events/${eventId}`);
                setEvent(response.data);
            } catch (error) {
                console.error("Error fetching event:", error);
            }
        };
        fetchEvent();
    }, [eventId]);

    useEffect(()=>{
        const fetchMatches = async () => {
            try {
                const response = await axios.get(`/matches/tournament/${eventId}`);
                setMatches(response.data);
            } catch (error) {
                console.error("Error fetching matches:", error);
            }
        }
        fetchMatches();
    }, [eventId]);

    useEffect(() => {
        const fetchTeams = async () => {
            try {
                const response = await axios.get('/teams/all');
                setTeams(response.data);
            } catch (error) {
                console.error("Error fetching teams:", error);
            }
        }
        fetchTeams();
    }, []);

    // Transform matches into react-brackets format
    const getBracketRounds = (): IRoundProps[] => {
        if (!matches || matches.length === 0) return [];

        // Group matches by stage and sort by roundId
        const matchesByStage = matches.reduce((acc, match) => {
            const stage = match.stage || 'Unknown';
            if (!acc[stage]) acc[stage] = [];
            acc[stage].push(match);
            return acc;
        }, {} as Record<string, Match[]>);

        // Define stage order for proper bracket progression
        const stageOrder = ['Round of 16', 'Quarterfinals', 'Semifinals', 'Grand Final', 'Playoffs'];
        
        return stageOrder
            .filter(stage => matchesByStage[stage])
            .map(stage => ({
                title: stage,
                seeds: matchesByStage[stage].map(match => {
                    const teamA = teams.find(t => t.id === match.teamAId);
                    const teamB = teams.find(t => t.id === match.teamBId);
                    
                    return {
                        id: match.id,
                        date: match.matchDate ? new Date(match.matchDate).toLocaleDateString() : 'TBD',
                        teams: [
                            { 
                                name: teamA?.name || 'TBD', 
                                score: match.teamAScore ?? 0 
                            },
                            { 
                                name: teamB?.name || 'TBD', 
                                score: match.teamBScore ?? 0 
                            }
                        ]
                    };
                })
            }));
    };

    const handleMatchClick = (matchId: number) => {
        const match = matches.find(m => m.id === matchId);
        if (match) {
            setSelectedMatch(match);
        }
    };

    if (!event) {
        return <div>Loading...</div>;
    }

    const bracketRounds = getBracketRounds();

    return (
        <div className="eventPage">
            <TopBar user={user}/>
            <div className="eventHeader">
                {event.bannerPath && (
                    <img 
                        src={`http://localhost:8080${event.bannerPath}`} 
                        alt={event.name} 
                        className="eventBanner"
                        onError={(e) => {
                            e.currentTarget.style.display = 'none';
                        }}
                    />
                )}
                <div className="eventInfo">
                    <h1>{event.name}</h1>
                    <p><strong>Date:</strong> {formatDateRange(event.startDate, event.endDate)}</p>
                    <p><strong>Location:</strong> {event.location}</p>
                    {event.description && <p className="eventDescription">{event.description}</p>}
                </div>
            </div>
            
            <h2>Playoffs Bracket</h2>
            <div className="playoffsBracket">
                {bracketRounds.length > 0 ? (
                    <Bracket 
                        rounds={bracketRounds}
                        renderSeedComponent={(props) => {
                            const team1Score = props.seed.teams[0]?.score || 0;
                            const team2Score = props.seed.teams[1]?.score || 0;
                            const team1Wins = team1Score > team2Score;
                            const team2Wins = team2Score > team1Score;
                            
                            return (
                                <div 
                                    className="bracket-seed-wrapper"
                                    onClick={() => handleMatchClick(props.seed.id as number)}
                                    style={{ cursor: 'pointer' }}
                                >
                                    <div className="bracket-seed-info">
                                        <div className={`bracket-team ${team1Wins ? 'winner' : team2Wins ? 'loser' : ''}`}>
                                            <span className="bracket-team-name">{props.seed.teams[0]?.name}</span>
                                            <span className="bracket-team-score">{props.seed.teams[0]?.score}</span>
                                        </div>
                                        <div className={`bracket-team ${team2Wins ? 'winner' : team1Wins ? 'loser' : ''}`}>
                                            <span className="bracket-team-name">{props.seed.teams[1]?.name}</span>
                                            <span className="bracket-team-score">{props.seed.teams[1]?.score}</span>
                                        </div>
                                    </div>
                                {props.seed.date && (
                                    <div className="bracket-seed-date">{props.seed.date}</div>
                                )}
                            </div>
                        );
                        }}
                    />
                ) : (
                    <p>No playoff matches available</p>
                )}
            </div>

            {selectedMatch && (
                <MatchDetails
                    matchId={selectedMatch.id}
                    teamA={teams.find(t => t.id === selectedMatch.teamAId)?.name || 'TBD'}
                    teamB={teams.find(t => t.id === selectedMatch.teamBId)?.name || 'TBD'}
                    scoreA={selectedMatch.teamAScore}
                    scoreB={selectedMatch.teamBScore}
                    matchDate={selectedMatch.matchDate}
                    matchType={selectedMatch.matchType}
                    onClose={() => setSelectedMatch(null)}
                />
            )}
        </div>
    );
}