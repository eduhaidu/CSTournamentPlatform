import { useEffect, useState } from "react";
import { useAuth } from "../../hooks/useAuth";
import TopBar from "../../components/TopBar";
import axios from "../../config/axios";
import "../../styles/PlayersManager.css";

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
    status?: 'ACTIVE' | 'BENCHED' | 'FREE_AGENT';
    teamId?: number;
}

interface Team {
    id: number;
    name: string;
    alias?: string;
}

export default function PlayersManager() {
    const { user } = useAuth();
    const [players, setPlayers] = useState<Player[]>([]);
    const [teams, setTeams] = useState<Team[]>([]);
    const [editingPlayer, setEditingPlayer] = useState<Player | null>(null);
    const [showModal, setShowModal] = useState(false);
    const [formData, setFormData] = useState<Partial<Player>>({});

    useEffect(() => {
        fetchPlayers();
        fetchTeams();
    }, []);

    const fetchPlayers = async () => {
        try {
            const response = await axios.get('/players/all');
            setPlayers(response.data);
        } catch (error) {
            console.error("Error fetching players:", error);
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

    const handleEdit = (player: Player) => {
        setEditingPlayer(player);
        setFormData(player);
        setShowModal(true);
    };

    const handleCreate = () => {
        setEditingPlayer(null);
        setFormData({
            status: 'FREE_AGENT'
        });
        setShowModal(true);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            if (editingPlayer) {
                await axios.put(`/players/${editingPlayer.id}`, formData);
            } else {
                await axios.post('/players', formData);
            }
            setShowModal(false);
            fetchPlayers();
        } catch (error) {
            console.error("Error saving player:", error);
            alert("Failed to save player. Please check the console for details.");
        }
    };

    const handleDelete = async (id: number) => {
        if (!confirm("Are you sure you want to delete this player?")) return;
        
        try {
            await axios.delete(`/players/${id}`);
            fetchPlayers();
        } catch (error) {
            console.error("Error deleting player:", error);
            alert("Failed to delete player.");
        }
    };

    const handleStatusChange = async (playerId: number, newStatus: string) => {
        try {
            const player = players.find(p => p.id === playerId);
            if (!player) return;

            const updatedData: Partial<Player> = {
                ...player,
                status: newStatus as 'ACTIVE' | 'BENCHED' | 'FREE_AGENT'
            };

            if (newStatus === 'FREE_AGENT') {
                updatedData.teamId = undefined;
            }

            await axios.put(`/players/${playerId}`, updatedData);
            fetchPlayers();
        } catch (error) {
            console.error("Error updating player status:", error);
            alert("Failed to update player status.");
        }
    };

    const handleTeamChange = async (playerId: number, teamId: string) => {
        try {
            const player = players.find(p => p.id === playerId);
            if (!player) return;

            const updatedData: Partial<Player> = {
                ...player,
                teamId: teamId ? parseInt(teamId) : undefined,
                status: teamId ? 'ACTIVE' : 'FREE_AGENT'
            };

            await axios.put(`/players/${playerId}`, updatedData);
            fetchPlayers();
        } catch (error) {
            console.error("Error updating player team:", error);
            alert("Failed to update player team.");
        }
    };

    const getStatusBadge = (status?: string) => {
        const statusClass = status ? status.toLowerCase() : 'free_agent';
        return <span className={`statusBadge ${statusClass}`}>{status || 'FREE_AGENT'}</span>;
    };

    const getTeamName = (teamId?: number) => {
        if (!teamId) return "Free Agent";
        const team = teams.find(t => t.id === teamId);
        return team ? team.name : "Unknown Team";
    };

    return (
        <div className="playersManager">
            <TopBar user={user} />
            <div className="playersManagerContainer">
                <div className="header">
                    <h1>Players Manager</h1>
                    <button className="createButton" onClick={handleCreate}>
                        + Create New Player
                    </button>
                </div>

                <div className="playersTable">
                    <table>
                        <thead>
                            <tr>
                                <th>Nickname</th>
                                <th>Real Name</th>
                                <th>Role</th>
                                <th>Team</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {players.map((player) => (
                                <tr key={player.id}>
                                    <td className="playerNickname">{player.nickname}</td>
                                    <td>{player.firstName} {player.lastName}</td>
                                    <td>{player.role || '-'}</td>
                                    <td>
                                        <select
                                            value={player.teamId || ''}
                                            onChange={(e) => handleTeamChange(player.id, e.target.value)}
                                            className="teamSelect"
                                        >
                                            <option value="">Free Agent</option>
                                            {teams.map(team => (
                                                <option key={team.id} value={team.id}>
                                                    {team.name}
                                                </option>
                                            ))}
                                        </select>
                                    </td>
                                    <td>
                                        <select
                                            value={player.status || 'FREE_AGENT'}
                                            onChange={(e) => handleStatusChange(player.id, e.target.value)}
                                            className="statusSelect"
                                        >
                                            <option value="ACTIVE">Active</option>
                                            <option value="BENCHED">Benched</option>
                                            <option value="FREE_AGENT">Free Agent</option>
                                        </select>
                                    </td>
                                    <td className="actions">
                                        <button 
                                            className="editButton"
                                            onClick={() => handleEdit(player)}
                                        >
                                            Edit
                                        </button>
                                        <button 
                                            className="deleteButton"
                                            onClick={() => handleDelete(player.id)}
                                        >
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {showModal && (
                    <div className="modal">
                        <div className="modalContent">
                            <h2>{editingPlayer ? 'Edit Player' : 'Create New Player'}</h2>
                            <form onSubmit={handleSubmit}>
                                <div className="formGroup">
                                    <label>Nickname *</label>
                                    <input
                                        type="text"
                                        value={formData.nickname || ''}
                                        onChange={(e) => setFormData({...formData, nickname: e.target.value})}
                                        required
                                    />
                                </div>

                                <div className="formRow">
                                    <div className="formGroup">
                                        <label>First Name</label>
                                        <input
                                            type="text"
                                            value={formData.firstName || ''}
                                            onChange={(e) => setFormData({...formData, firstName: e.target.value})}
                                        />
                                    </div>

                                    <div className="formGroup">
                                        <label>Last Name</label>
                                        <input
                                            type="text"
                                            value={formData.lastName || ''}
                                            onChange={(e) => setFormData({...formData, lastName: e.target.value})}
                                        />
                                    </div>
                                </div>

                                <div className="formRow">
                                    <div className="formGroup">
                                        <label>Country</label>
                                        <input
                                            type="text"
                                            value={formData.country || ''}
                                            onChange={(e) => setFormData({...formData, country: e.target.value})}
                                        />
                                    </div>

                                    <div className="formGroup">
                                        <label>Role</label>
                                        <input
                                            type="text"
                                            value={formData.role || ''}
                                            onChange={(e) => setFormData({...formData, role: e.target.value})}
                                        />
                                    </div>
                                </div>

                                <div className="formGroup">
                                    <label>Photo Path</label>
                                    <input
                                        type="text"
                                        value={formData.photoPath || ''}
                                        onChange={(e) => setFormData({...formData, photoPath: e.target.value})}
                                        placeholder="/uploads/players/player.jpg"
                                    />
                                </div>

                                <div className="formGroup">
                                    <label>Steam ID</label>
                                    <input
                                        type="text"
                                        value={formData.steamId || ''}
                                        onChange={(e) => setFormData({...formData, steamId: e.target.value})}
                                    />
                                </div>

                                <div className="formGroup">
                                    <label>FACEIT URL</label>
                                    <input
                                        type="text"
                                        value={formData.faceitUrl || ''}
                                        onChange={(e) => setFormData({...formData, faceitUrl: e.target.value})}
                                    />
                                </div>

                                <div className="formGroup">
                                    <label>Twitter Handle</label>
                                    <input
                                        type="text"
                                        value={formData.twitterHandle || ''}
                                        onChange={(e) => setFormData({...formData, twitterHandle: e.target.value})}
                                    />
                                </div>

                                <div className="formRow">
                                    <div className="formGroup">
                                        <label>Team</label>
                                        <select
                                            value={formData.teamId || ''}
                                            onChange={(e) => setFormData({
                                                ...formData, 
                                                teamId: e.target.value ? parseInt(e.target.value) : undefined,
                                                status: e.target.value ? 'ACTIVE' : 'FREE_AGENT'
                                            })}
                                        >
                                            <option value="">Free Agent</option>
                                            {teams.map(team => (
                                                <option key={team.id} value={team.id}>
                                                    {team.name}
                                                </option>
                                            ))}
                                        </select>
                                    </div>

                                    <div className="formGroup">
                                        <label>Status</label>
                                        <select
                                            value={formData.status || 'FREE_AGENT'}
                                            onChange={(e) => setFormData({
                                                ...formData, 
                                                status: e.target.value as 'ACTIVE' | 'BENCHED' | 'FREE_AGENT'
                                            })}
                                        >
                                            <option value="ACTIVE">Active</option>
                                            <option value="BENCHED">Benched</option>
                                            <option value="FREE_AGENT">Free Agent</option>
                                        </select>
                                    </div>
                                </div>

                                <div className="modalActions">
                                    <button type="submit" className="saveButton">
                                        {editingPlayer ? 'Update' : 'Create'}
                                    </button>
                                    <button 
                                        type="button" 
                                        className="cancelButton"
                                        onClick={() => setShowModal(false)}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
