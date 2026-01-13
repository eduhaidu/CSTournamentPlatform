import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "../config/axios";
import TopBar from "../components/TopBar";
import { useAuth } from "../hooks/useAuth";
import { getCountryFlag } from "../utils/countryFlags";
import "../styles/TeamProfile.css";

interface Player {
    id: number;
    nickname: string;
    firstName?: string;
    lastName?: string;
    country?: string;
    role?: string;
    photoPath?: string;
}

interface Team {
    id: number;
    name: string;
    alias?: string;
    logoPath?: string;
    country?: string;
    foundedOn?: string;
    coachName?: string;
    pageTitle?: string;
}

export default function TeamProfile() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { user } = useAuth();
    const [team, setTeam] = useState<Team | null>(null);
    const [players, setPlayers] = useState<Player[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchTeamDetails = async () => {
            try {
                setLoading(true);
                const teamResponse = await axios.get(`/teams/${id}`);
                setTeam(teamResponse.data);

                const playersResponse = await axios.get(`/players/team/${id}`);
                setPlayers(playersResponse.data);
            } catch (error) {
                console.error("Error fetching team details:", error);
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            fetchTeamDetails();
        }
    }, [id]);

    if (loading) {
        return (
            <div className="teamProfile">
                <TopBar user={user} />
                <div className="loading">Loading team details...</div>
            </div>
        );
    }

    if (!team) {
        return (
            <div className="teamProfile">
                <TopBar user={user} />
                <div className="error">Team not found</div>
            </div>
        );
    }

    const logoUrl = team.logoPath ? `http://localhost:8080${team.logoPath}` : undefined;

    return (
        <div className="teamProfile">
            <TopBar user={user} />
            <div className="teamProfileContainer">
                <button className="backButton" onClick={() => navigate('/teams')}>
                    ← Back to Teams
                </button>

                <div className="teamHeader">
                    {logoUrl && (
                        <img 
                            src={logoUrl} 
                            alt={`${team.name} Logo`} 
                            className="teamLogo"
                            onError={(e) => {
                                e.currentTarget.style.display = 'none';
                            }}
                        />
                    )}
                    <div className="teamHeaderInfo">
                        <h1>{team.name}</h1>
                        {team.alias && <p className="teamAlias">"{team.alias}"</p>}
                        {team.country && (
                            <div className="teamCountry">
                                <span className="countryFlag" title={team.country}>
                                    {getCountryFlag(team.country)}
                                </span>
                                <span>{team.country}</span>
                            </div>
                        )}
                    </div>
                </div>

                <div className="teamInfo">
                    {team.foundedOn && (
                        <div className="infoCard">
                            <h3>Founded</h3>
                            <p>{new Date(team.foundedOn).toLocaleDateString()}</p>
                        </div>
                    )}
                    {team.coachName && (
                        <div className="infoCard">
                            <h3>Coach</h3>
                            <p>{team.coachName}</p>
                        </div>
                    )}
                </div>

                <div className="playersSection">
                    <h2>Roster</h2>
                    {players.length > 0 ? (
                        <div className="playersGrid">
                            {players.map((player) => (
                                <div 
                                    key={player.id} 
                                    className="playerCard"
                                    onClick={() => navigate(`/player/${player.id}`)}
                                    style={{ cursor: 'pointer' }}
                                >
                                    {player.photoPath && (
                                        <img 
                                            src={player.photoPath.startsWith('http') ? player.photoPath : `http://localhost:8080${player.photoPath}`} 
                                            alt={`${player.nickname} Photo`} 
                                            className="playerPhoto"
                                            onError={(e) => {
                                                e.currentTarget.style.display = 'none';
                                            }}
                                        />
                                    )}
                                    <div className="playerInfo">
                                        <h3>{player.nickname}</h3>
                                        {(player.firstName || player.lastName) && (
                                            <p className="playerRealName">
                                                {player.firstName} {player.lastName}
                                            </p>
                                        )}
                                        {player.role && <span className="playerRole">{player.role}</span>}
                                        {player.country && (
                                            <div className="playerCountry">
                                                <span className="countryFlag" title={player.country}>
                                                    {getCountryFlag(player.country)}
                                                </span>
                                                <span>{player.country}</span>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <p className="noPlayers">No players found for this team.</p>
                    )}
                </div>
            </div>
        </div>
    );
}