import { useState, useEffect } from "react";
import axios from "../../config/axios";
import "../../styles/MatchesManager.css";

interface Tournament {
    id: number;
    name: string;
}

interface Team {
    id: number;
    name: string;
}

interface Match {
    id: number;
    tournamentId: number;
    teamAId: number;
    teamBId: number;
    matchDate: string;
    result: string;
    status: string;
    teamAScore: number;
    teamBScore: number;
    winnerTeamId?: number;
    matchType: string;
    stage: string;
}

interface Map {
    id?: number;
    mapName: string;
    matchId?: number;
    teamAFinalScore: number;
    teamBFinalScore: number;
    teamATRounds: number;
    teamACTRounds: number;
    teamBTRounds: number;
    teamBCTRounds: number;
}

export default function MatchesManager() {
    const [tournaments, setTournaments] = useState<Tournament[]>([]);
    const [teams, setTeams] = useState<Team[]>([]);
    const [matches, setMatches] = useState<Match[]>([]);
    const [selectedTournament, setSelectedTournament] = useState<number | null>(null);
    const [editingMatch, setEditingMatch] = useState<Match | null>(null);
    const [isCreating, setIsCreating] = useState(false);
    const [maps, setMaps] = useState<Map[]>([]);
    const [formData, setFormData] = useState<Partial<Match>>({
        tournamentId: undefined,
        teamAId: undefined,
        teamBId: undefined,
        matchDate: "",
        result: "",
        status: "Scheduled",
        teamAScore: 0,
        teamBScore: 0,
        winnerTeamId: undefined,
        matchType: "Best of 3",
        stage: "Group Stage",
    });

    useEffect(() => {
        fetchTournaments();
        fetchTeams();
    }, []);

    useEffect(() => {
        if (selectedTournament) {
            fetchMatches(selectedTournament);
        }
    }, [selectedTournament]);

    const fetchTournaments = async () => {
        try {
            const response = await axios.get('/events/all');
            setTournaments(response.data);
        } catch (error) {
            console.error("Error fetching tournaments:", error);
        }
    };

    const fetchTeams = async () => {
        try {
            const response = await axios.get('/teams/all');
            setTeams(response.data);
        } catch (error) {
            console.error("Error fetching teams:", error);
        }
    };

    const fetchMatches = async (tournamentId: number) => {
        try {
            const response = await axios.get(`/matches/tournament/${tournamentId}`);
            setMatches(response.data);
        } catch (error) {
            console.error("Error fetching matches:", error);
        }
    };

    const fetchMapsForMatch = async (matchId: number) => {
        try {
            const response = await axios.get(`/maps/match/${matchId}`);
            setMaps(response.data);
        } catch (error) {
            console.error("Error fetching maps:", error);
            setMaps([]);
        }
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: name.includes('Id') || name.includes('Score') ? parseInt(value) || undefined : value
        }));
    };

    const handleCreate = () => {
        setIsCreating(true);
        setEditingMatch(null);
        setFormData({
            tournamentId: selectedTournament || undefined,
            teamAId: undefined,
            teamBId: undefined,
            matchDate: "",
            result: "",
            status: "Scheduled",
            teamAScore: 0,
            teamBScore: 0,
            winnerTeamId: undefined,
            matchType: "Best of 3",
            stage: "Group Stage",
        });
        setMaps([]);
    };

    const handleEdit = async (match: Match) => {
        setEditingMatch(match);
        setIsCreating(false);
        setFormData({
            tournamentId: match.tournamentId,
            teamAId: match.teamAId,
            teamBId: match.teamBId,
            matchDate: match.matchDate,
            result: match.result,
            status: match.status,
            teamAScore: match.teamAScore,
            teamBScore: match.teamBScore,
            winnerTeamId: match.winnerTeamId,
            matchType: match.matchType,
            stage: match.stage,
        });
        await fetchMapsForMatch(match.id);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        try {
            let matchId: number;
            
            if (isCreating) {
                const response = await axios.post('/matches/add', formData);
                matchId = response.data.id;
            } else if (editingMatch) {
                await axios.put(`/matches/update/${editingMatch.id}`, formData);
                matchId = editingMatch.id;
            } else {
                return;
            }

            // Save maps
            for (const map of maps) {
                if (map.id) {
                    await axios.put(`/maps/update/${map.id}`, { ...map, matchId });
                } else {
                    await axios.post('/maps/add', { ...map, matchId });
                }
            }

            // Reset form and refresh
            setIsCreating(false);
            setEditingMatch(null);
            setFormData({
                tournamentId: selectedTournament || undefined,
                teamAId: undefined,
                teamBId: undefined,
                matchDate: "",
                result: "",
                status: "Scheduled",
                teamAScore: 0,
                teamBScore: 0,
                winnerTeamId: undefined,
                matchType: "Best of 3",
                stage: "Group Stage",
            });
            setMaps([]);
            if (selectedTournament) {
                fetchMatches(selectedTournament);
            }
        } catch (error) {
            console.error("Error saving match:", error);
            alert("Failed to save match");
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm("Are you sure you want to delete this match?")) {
            return;
        }

        try {
            await axios.delete(`/matches/${id}`);
            if (selectedTournament) {
                fetchMatches(selectedTournament);
            }
        } catch (error) {
            console.error("Error deleting match:", error);
            alert("Failed to delete match");
        }
    };

    const handleCancel = () => {
        setIsCreating(false);
        setEditingMatch(null);
        setFormData({
            tournamentId: selectedTournament || undefined,
            teamAId: undefined,
            teamBId: undefined,
            matchDate: "",
            result: "",
            status: "Scheduled",
            teamAScore: 0,
            teamBScore: 0,
            winnerTeamId: undefined,
            matchType: "Best of 3",
            stage: "Group Stage",
        });
        setMaps([]);
    };

    const addMap = () => {
        setMaps([...maps, {
            mapName: "Dust2",
            teamAFinalScore: 0,
            teamBFinalScore: 0,
            teamATRounds: 0,
            teamACTRounds: 0,
            teamBTRounds: 0,
            teamBCTRounds: 0,
        }]);
    };

    const removeMap = (index: number) => {
        setMaps(maps.filter((_, i) => i !== index));
    };

    const updateMap = (index: number, field: keyof Map, value: string | number) => {
        const updatedMaps = [...maps];
        updatedMaps[index] = { ...updatedMaps[index], [field]: value };
        setMaps(updatedMaps);
    };

    const getTeamName = (teamId: number) => {
        const team = teams.find(t => t.id === teamId);
        return team ? team.name : "Unknown Team";
    };

    return (
        <div className="matchesManager">
            <div className="matchesManagerHeader">
                <h1>Matches Manager</h1>
            </div>

            <div className="tournamentSelector">
                <label htmlFor="tournamentSelect">Select Tournament:</label>
                <select
                    id="tournamentSelect"
                    value={selectedTournament || ""}
                    onChange={(e) => setSelectedTournament(parseInt(e.target.value))}
                >
                    <option value="">-- Select Tournament --</option>
                    {tournaments.map(tournament => (
                        <option key={tournament.id} value={tournament.id}>
                            {tournament.name}
                        </option>
                    ))}
                </select>
                {selectedTournament && !isCreating && !editingMatch && (
                    <button onClick={handleCreate} className="btnCreate">
                        Create New Match
                    </button>
                )}
            </div>

            {selectedTournament && !isCreating && !editingMatch && (
                <div className="matchesList">
                    <h2>Matches</h2>
                    {matches.length === 0 ? (
                        <p>No matches found for this tournament.</p>
                    ) : (
                        <table>
                            <thead>
                                <tr>
                                    <th>Stage</th>
                                    <th>Team A</th>
                                    <th>Score</th>
                                    <th>Team B</th>
                                    <th>Date</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {matches.map(match => (
                                    <tr key={match.id}>
                                        <td>{match.stage}</td>
                                        <td>{getTeamName(match.teamAId)}</td>
                                        <td>{match.teamAScore} - {match.teamBScore}</td>
                                        <td>{getTeamName(match.teamBId)}</td>
                                        <td>{new Date(match.matchDate).toLocaleString()}</td>
                                        <td>{match.status}</td>
                                        <td>
                                            <button onClick={() => handleEdit(match)}>Edit</button>
                                            <button onClick={() => handleDelete(match.id)} className="btnDelete">Delete</button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            )}

            {(isCreating || editingMatch) && (
                <div className="matchForm">
                    <h2>{isCreating ? "Create New Match" : "Edit Match"}</h2>
                    <form onSubmit={handleSubmit}>
                        <div className="formRow">
                            <div className="formGroup">
                                <label htmlFor="teamAId">Team A *</label>
                                <select
                                    id="teamAId"
                                    name="teamAId"
                                    value={formData.teamAId || ""}
                                    onChange={handleInputChange}
                                    required
                                >
                                    <option value="">-- Select Team --</option>
                                    {teams.map(team => (
                                        <option key={team.id} value={team.id}>{team.name}</option>
                                    ))}
                                </select>
                            </div>

                            <div className="formGroup">
                                <label htmlFor="teamBId">Team B *</label>
                                <select
                                    id="teamBId"
                                    name="teamBId"
                                    value={formData.teamBId || ""}
                                    onChange={handleInputChange}
                                    required
                                >
                                    <option value="">-- Select Team --</option>
                                    {teams.map(team => (
                                        <option key={team.id} value={team.id}>{team.name}</option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className="formRow">
                            <div className="formGroup">
                                <label htmlFor="stage">Stage *</label>
                                <select
                                    id="stage"
                                    name="stage"
                                    value={formData.stage || ""}
                                    onChange={handleInputChange}
                                    required
                                >
                                    <option value="Group Stage">Group Stage</option>
                                    <option value="Quarterfinals">Quarterfinals</option>
                                    <option value="Semifinals">Semifinals</option>
                                    <option value="Grand Final">Grand Final</option>
                                    <option value="Third Place Match">Third Place Match</option>
                                </select>
                            </div>

                            <div className="formGroup">
                                <label htmlFor="matchType">Match Type *</label>
                                <select
                                    id="matchType"
                                    name="matchType"
                                    value={formData.matchType || ""}
                                    onChange={handleInputChange}
                                    required
                                >
                                    <option value="Best of 1">Best of 1</option>
                                    <option value="Best of 3">Best of 3</option>
                                    <option value="Best of 5">Best of 5</option>
                                </select>
                            </div>
                        </div>

                        <div className="formRow">
                            <div className="formGroup">
                                <label htmlFor="matchDate">Match Date *</label>
                                <input
                                    type="datetime-local"
                                    id="matchDate"
                                    name="matchDate"
                                    value={formData.matchDate ? formData.matchDate.slice(0, 16) : ""}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            <div className="formGroup">
                                <label htmlFor="status">Status *</label>
                                <select
                                    id="status"
                                    name="status"
                                    value={formData.status || ""}
                                    onChange={handleInputChange}
                                    required
                                >
                                    <option value="Scheduled">Scheduled</option>
                                    <option value="Live">Live</option>
                                    <option value="Completed">Completed</option>
                                    <option value="Cancelled">Cancelled</option>
                                </select>
                            </div>
                        </div>

                        <div className="formRow">
                            <div className="formGroup">
                                <label htmlFor="teamAScore">Team A Score</label>
                                <input
                                    type="number"
                                    id="teamAScore"
                                    name="teamAScore"
                                    value={formData.teamAScore ?? 0}
                                    onChange={handleInputChange}
                                    min="0"
                                />
                            </div>

                            <div className="formGroup">
                                <label htmlFor="teamBScore">Team B Score</label>
                                <input
                                    type="number"
                                    id="teamBScore"
                                    name="teamBScore"
                                    value={formData.teamBScore ?? 0}
                                    onChange={handleInputChange}
                                    min="0"
                                />
                            </div>
                        </div>

                        <div className="mapsSection">
                            <h3>Maps</h3>
                            {maps.map((map, index) => (
                                <div key={index} className="mapEntry">
                                    <div className="mapHeader">
                                        <h4>Map {index + 1}</h4>
                                        <button type="button" onClick={() => removeMap(index)} className="btnDelete">Remove</button>
                                    </div>
                                    <div className="formRow">
                                        <div className="formGroup">
                                            <label>Map Name</label>
                                            <select
                                                value={map.mapName}
                                                onChange={(e) => updateMap(index, 'mapName', e.target.value)}
                                            >
                                                <option value="Dust2">Dust2</option>
                                                <option value="Mirage">Mirage</option>
                                                <option value="Inferno">Inferno</option>
                                                <option value="Nuke">Nuke</option>
                                                <option value="Overpass">Overpass</option>
                                                <option value="Vertigo">Vertigo</option>
                                                <option value="Ancient">Ancient</option>
                                                <option value="Anubis">Anubis</option>
                                            </select>
                                        </div>
                                        <div className="formGroup">
                                            <label>Team A Final</label>
                                            <input
                                                type="number"
                                                value={map.teamAFinalScore ?? 0}
                                                onChange={(e) => updateMap(index, 'teamAFinalScore', parseInt(e.target.value) || 0)}
                                                min="0"
                                            />
                                        </div>
                                        <div className="formGroup">
                                            <label>Team B Final</label>
                                            <input
                                                type="number"
                                                value={map.teamBFinalScore ?? 0}
                                                onChange={(e) => updateMap(index, 'teamBFinalScore', parseInt(e.target.value) || 0)}
                                                min="0"
                                            />
                                        </div>
                                    </div>
                                    <div className="formRow">
                                        <div className="formGroup">
                                            <label>Team A T</label>
                                            <input
                                                type="number"
                                                value={map.teamATRounds ?? 0}
                                                onChange={(e) => updateMap(index, 'teamATRounds', parseInt(e.target.value) || 0)}
                                                min="0"
                                            />
                                        </div>
                                        <div className="formGroup">
                                            <label>Team A CT</label>
                                            <input
                                                type="number"
                                                value={map.teamACTRounds ?? 0}
                                                onChange={(e) => updateMap(index, 'teamACTRounds', parseInt(e.target.value) || 0)}
                                                min="0"
                                            />
                                        </div>
                                        <div className="formGroup">
                                            <label>Team B T</label>
                                            <input
                                                type="number"
                                                value={map.teamBTRounds ?? 0}
                                                onChange={(e) => updateMap(index, 'teamBTRounds', parseInt(e.target.value) || 0)}
                                                min="0"
                                            />
                                        </div>
                                        <div className="formGroup">
                                            <label>Team B CT</label>
                                            <input
                                                type="number"
                                                value={map.teamBCTRounds ?? 0}
                                                onChange={(e) => updateMap(index, 'teamBCTRounds', parseInt(e.target.value) || 0)}
                                                min="0"
                                            />
                                        </div>
                                    </div>
                                </div>
                            ))}
                            <button type="button" onClick={addMap} className="btnAddMap">Add Map</button>
                        </div>

                        <div className="formActions">
                            <button type="submit" className="btnSubmit">
                                {isCreating ? "Create Match" : "Update Match"}
                            </button>
                            <button type="button" onClick={handleCancel} className="btnCancel">
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}
        </div>
    );
}
