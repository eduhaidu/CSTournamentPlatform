import { useState, useEffect } from "react";
import axios from "../../config/axios";
import "../../styles/TeamsManager.css";

interface Team {
    id: number;
    name: string;
    alias?: string;
    country: string;
    foundedOn: string;
    coachName: string;
    logoPath?: string;
}

interface Player {
    id: number;
    nickname: string;
    firstName?: string;
    lastName?: string;
    country?: string;
    role?: string;
}

export default function TeamsManager() {
    const [teams, setTeams] = useState<Team[]>([]);
    const [editingTeam, setEditingTeam] = useState<Team | null>(null);
    const [isCreating, setIsCreating] = useState(false);
    const [formData, setFormData] = useState<Partial<Team>>({
        name: "",
        alias: "",
        country: "",
        foundedOn: "",
        coachName: "",
    });
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [teamPlayers, setTeamPlayers] = useState<{ [key: number]: Player[] }>({});

    useEffect(() => {
        fetchTeams();
    }, []);

    const fetchTeams = async () => {
        try {
            const response = await axios.get('/teams/all');
            const teamsData = response.data;
            setTeams(teamsData);
            
            // Fetch players for each team
            const playersData: { [key: number]: Player[] } = {};
            await Promise.all(teamsData.map(async (team: Team) => {
                try {
                    const membersRes = await axios.get(`/players/team/${team.id}`);
                    playersData[team.id] = membersRes.data;
                } catch (error) {
                    console.error(`Error fetching members for team ${team.id}:`, error);
                    playersData[team.id] = [];
                }
            }));
            setTeamPlayers(playersData);
        } catch (error) {
            console.error("Error fetching teams:", error);
        }
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            setSelectedFile(e.target.files[0]);
        }
    };

    const handleCreate = () => {
        setIsCreating(true);
        setEditingTeam(null);
        setFormData({
            name: "",
            alias: "",
            country: "",
            foundedOn: "",
            coachName: "",
        });
        setSelectedFile(null);
    };

    const handleEdit = (team: Team) => {
        setEditingTeam(team);
        setIsCreating(false);
        setFormData({
            name: team.name,
            alias: team.alias || "",
            country: team.country,
            foundedOn: team.foundedOn,
            coachName: team.coachName,
        });
        setSelectedFile(null);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        try {
            let logoPath = editingTeam?.logoPath;

            // Upload logo if a file is selected
            if (selectedFile) {
                const uploadFormData = new FormData();
                uploadFormData.append('file', selectedFile);
                const uploadResponse = await axios.post('/files/upload', uploadFormData, {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                    },
                });
                logoPath = uploadResponse.data;
            }

            const teamData = {
                ...formData,
                logoPath: logoPath || "",
            };

            if (isCreating) {
                await axios.post('/teams/add', teamData);
            } else if (editingTeam) {
                await axios.put(`/teams/update/${editingTeam.id}`, teamData);
            }

            // Reset form and refresh teams
            setIsCreating(false);
            setEditingTeam(null);
            setFormData({
                name: "",
                alias: "",
                country: "",
                foundedOn: "",
                coachName: "",
            });
            setSelectedFile(null);
            fetchTeams();
        } catch (error) {
            console.error("Error saving team:", error);
            alert("Failed to save team");
        }
    };

    const handleDelete = async (id: number) => {
        if (!window.confirm("Are you sure you want to delete this team? This will also delete all associated players.")) {
            return;
        }

        try {
            await axios.delete(`/teams/${id}`);
            fetchTeams();
        } catch (error) {
            console.error("Error deleting team:", error);
            alert("Failed to delete team");
        }
    };

    const handleCancel = () => {
        setIsCreating(false);
        setEditingTeam(null);
        setFormData({
            name: "",
            alias: "",
            country: "",
            foundedOn: "",
            coachName: "",
        });
        setSelectedFile(null);
    };

    return (
        <div className="teamsManager">
            <div className="teamsManagerHeader">
                <h1>Teams Manager</h1>
                {!isCreating && !editingTeam && (
                    <button onClick={handleCreate} className="btnCreate">
                        Create New Team
                    </button>
                )}
            </div>

            {(isCreating || editingTeam) && (
                <div className="teamForm">
                    <h2>{isCreating ? "Create New Team" : "Edit Team"}</h2>
                    <form onSubmit={handleSubmit}>
                        <div className="formGroup">
                            <label htmlFor="name">Team Name *</label>
                            <input
                                type="text"
                                id="name"
                                name="name"
                                value={formData.name || ""}
                                onChange={handleInputChange}
                                required
                            />
                        </div>

                        <div className="formGroup">
                            <label htmlFor="alias">Alias</label>
                            <input
                                type="text"
                                id="alias"
                                name="alias"
                                value={formData.alias || ""}
                                onChange={handleInputChange}
                                placeholder="e.g., spirit, mouz, navi (used in brackets)"
                            />
                            <small style={{ color: '#888', fontSize: '0.85em', marginTop: '4px', display: 'block' }}>
                                Short identifier used in tournament brackets. Usually lowercase.
                            </small>
                        </div>

                        <div className="formRow">
                            <div className="formGroup">
                                <label htmlFor="country">Country *</label>
                                <input
                                    type="text"
                                    id="country"
                                    name="country"
                                    value={formData.country || ""}
                                    onChange={handleInputChange}
                                    required
                                    placeholder="e.g., USA, Brazil, Sweden"
                                />
                            </div>

                            <div className="formGroup">
                                <label htmlFor="foundedOn">Founded Date</label>
                                <input
                                    type="date"
                                    id="foundedOn"
                                    name="foundedOn"
                                    value={formData.foundedOn || ""}
                                    onChange={handleInputChange}
                                />
                            </div>
                        </div>

                        <div className="formGroup">
                            <label htmlFor="coachName">Coach Name</label>
                            <input
                                type="text"
                                id="coachName"
                                name="coachName"
                                value={formData.coachName || ""}
                                onChange={handleInputChange}
                                placeholder="e.g., John Smith"
                            />
                        </div>

                        <div className="formGroup">
                            <label htmlFor="logo">Team Logo</label>
                            <input
                                type="file"
                                id="logo"
                                accept="image/*"
                                onChange={handleFileChange}
                            />
                            {editingTeam?.logoPath && !selectedFile && (
                                <div className="currentLogo">
                                    <img 
                                        src={`http://localhost:8080${editingTeam.logoPath}`} 
                                        alt="Current logo" 
                                    />
                                </div>
                            )}
                        </div>

                        <div className="formActions">
                            <button type="submit" className="btnSubmit">
                                {isCreating ? "Create Team" : "Update Team"}
                            </button>
                            <button type="button" onClick={handleCancel} className="btnCancel">
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            )}

            <div className="teamsList">
                <h2>All Teams</h2>
                {teams.length === 0 ? (
                    <p className="noTeams">No teams found. Create one to get started!</p>
                ) : (
                    <div className="teamsGrid">
                        {teams.map((team) => (
                            <div key={team.id} className="teamItem">
                                {team.logoPath && (
                                    <img 
                                        src={`http://localhost:8080${team.logoPath}`} 
                                        alt={team.name}
                                        className="teamLogo"
                                    />
                                )}
                                <div className="teamDetails">
                                    <h3>{team.name}</h3>
                                    <div className="teamMeta">
                                        {team.alias && <p><strong>Alias:</strong> {team.alias}</p>}
                                        <p><strong>Country:</strong> {team.country}</p>
                                        {team.foundedOn && <p><strong>Founded:</strong> {team.foundedOn}</p>}
                                        {team.coachName && <p><strong>Coach:</strong> {team.coachName}</p>}
                                        <p><strong>Players:</strong> {teamPlayers[team.id]?.length || 0}</p>
                                    </div>
                                    {teamPlayers[team.id] && teamPlayers[team.id].length > 0 && (
                                        <div className="teamRoster">
                                            <h4>Roster:</h4>
                                            <ul>
                                                {teamPlayers[team.id].map((player) => (
                                                    <li key={player.id}>
                                                        {player.nickname}
                                                        {player.role && ` (${player.role})`}
                                                        {player.country && ` - ${player.country}`}
                                                    </li>
                                                ))}
                                            </ul>
                                        </div>
                                    )}
                                </div>
                                <div className="teamActions">
                                    <button 
                                        onClick={() => handleEdit(team)}
                                        className="btnEdit"
                                    >
                                        Edit
                                    </button>
                                    <button 
                                        onClick={() => handleDelete(team.id)}
                                        className="btnDelete"
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}