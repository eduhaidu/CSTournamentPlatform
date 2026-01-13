import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "../config/axios";
import TopBar from "../components/TopBar";
import { useAuth } from "../hooks/useAuth";
import { getCountryFlag } from "../utils/countryFlags";
import "../styles/PlayerProfile.css";

interface Player {
    id: number;
    nickname: string;
    firstName?: string;
    lastName?: string;
    country?: string;
    role?: string;
    photoPath?: string;
    steamId?: string;
    faceitUrl?: string;
    twitterHandle?: string;
}

interface Team {
    id: number;
    name: string;
    alias?: string;
    logoPath?: string;
}

interface PlayerWithTeam extends Player {
    team?: Team;
}

export default function PlayerProfile() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { user } = useAuth();
    const [player, setPlayer] = useState<PlayerWithTeam | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchPlayerDetails = async () => {
            try {
                setLoading(true);
                const response = await axios.get(`/players/${id}`);
                setPlayer(response.data);
            } catch (error) {
                console.error("Error fetching player details:", error);
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            fetchPlayerDetails();
        }
    }, [id]);

    if (loading) {
        return (
            <div className="playerProfile">
                <TopBar user={user} />
                <div className="loading">Loading player details...</div>
            </div>
        );
    }

    if (!player) {
        return (
            <div className="playerProfile">
                <TopBar user={user} />
                <div className="error">Player not found</div>
            </div>
        );
    }

    const photoUrl = player.photoPath 
        ? (player.photoPath.startsWith('http') ? player.photoPath : `http://localhost:8080${player.photoPath}`)
        : undefined;

    const teamLogoUrl = player.team?.logoPath 
        ? `http://localhost:8080${player.team.logoPath}` 
        : undefined;

    return (
        <div className="playerProfile">
            <TopBar user={user} />
            <div className="playerProfileContainer">
                <button className="backButton" onClick={() => navigate(-1)}>
                    ← Back
                </button>

                <div className="playerHeader">
                    {photoUrl && (
                        <img 
                            src={photoUrl} 
                            alt={`${player.nickname} Photo`} 
                            className="playerPhoto"
                            onError={(e) => {
                                e.currentTarget.style.display = 'none';
                            }}
                        />
                    )}
                    <div className="playerHeaderInfo">
                        <h1>{player.nickname}</h1>
                        {(player.firstName || player.lastName) && (
                            <p className="playerRealName">
                                {player.firstName} {player.lastName}
                            </p>
                        )}
                        {player.country && (
                            <div className="playerCountry">
                                <span className="countryFlag" title={player.country}>
                                    {getCountryFlag(player.country)}
                                </span>
                                <span>{player.country}</span>
                            </div>
                        )}
                        {player.role && (
                            <div className="playerRoleBadge">{player.role}</div>
                        )}
                    </div>
                </div>

                <div className="playerInfo">
                    {player.team && (
                        <div 
                            className="infoCard teamCard"
                            onClick={() => navigate(`/team/${player.team?.id}`)}
                            style={{ cursor: 'pointer' }}
                        >
                            <h3>Current Team</h3>
                            <div className="teamInfo">
                                {teamLogoUrl && (
                                    <img 
                                        src={teamLogoUrl} 
                                        alt={`${player.team.name} Logo`} 
                                        className="teamLogo"
                                        onError={(e) => {
                                            e.currentTarget.style.display = 'none';
                                        }}
                                    />
                                )}
                                <div>
                                    <p className="teamName">{player.team.name}</p>
                                    {player.team.alias && (
                                        <p className="teamAlias">"{player.team.alias}"</p>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                {(player.steamId || player.faceitUrl || player.twitterHandle) && (
                    <div className="playerLinks">
                        <h2>Links</h2>
                        <div className="linksGrid">
                            {player.steamId && (
                                <a 
                                    href={`https://steamcommunity.com/profiles/${player.steamId}`}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="linkCard"
                                >
                                    <span className="linkIcon">🎮</span>
                                    <span>Steam Profile</span>
                                </a>
                            )}
                            {player.faceitUrl && (
                                <a 
                                    href={player.faceitUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="linkCard"
                                >
                                    <span className="linkIcon">🎯</span>
                                    <span>FACEIT</span>
                                </a>
                            )}
                            {player.twitterHandle && (
                                <a 
                                    href={`https://twitter.com/${player.twitterHandle.replace('@', '')}`}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="linkCard"
                                >
                                    <span className="linkIcon">🐦</span>
                                    <span>Twitter</span>
                                </a>
                            )}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
