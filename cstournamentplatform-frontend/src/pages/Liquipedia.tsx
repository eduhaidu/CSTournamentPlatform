import { useState } from 'react';
import axios from '../config/axios';
import '../styles/Liquipedia.css';

interface ImportResult {
    message: string;
    event?: any;
    team?: any;
    error?: string;
    imported?: number;
    requested?: number;
}

export default function Liquipedia() {
    const [tournamentPage, setTournamentPage] = useState('');
    const [teamPage, setTeamPage] = useState('');
    const [bulkTournaments, setBulkTournaments] = useState('');
    const [bulkTeams, setBulkTeams] = useState('');
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState<ImportResult | null>(null);
    const [samples, setSamples] = useState<{ tournaments: string[], teams: string[] }>({ tournaments: [], teams: [] });

    const fetchSamples = async () => {
        try {
            const [tournamentsRes, teamsRes] = await Promise.all([
                axios.get('/api/admin/liquipedia/sample-tournaments'),
                axios.get('/api/admin/liquipedia/sample-teams')
            ]);
            setSamples({
                tournaments: tournamentsRes.data.pageTitles,
                teams: teamsRes.data.pageTitles
            });
        } catch (error) {
            console.error('Error fetching samples:', error);
        }
    };

    useState(() => {
        fetchSamples();
    });

    const handleImportTournament = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!tournamentPage.trim()) return;

        setLoading(true);
        setResult(null);

        try {
            const response = await axios.post(
                `/api/admin/liquipedia/tournament?pageTitle=${encodeURIComponent(tournamentPage)}`
            );
            setResult(response.data);
            setTournamentPage('');
        } catch (error: any) {
            setResult({ message: 'Error', error: error.response?.data?.error || error.message });
        } finally {
            setLoading(false);
        }
    };

    const handleImportTeam = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!teamPage.trim()) return;

        setLoading(true);
        setResult(null);

        try {
            const response = await axios.post(
                `/api/admin/liquipedia/team?pageTitle=${encodeURIComponent(teamPage)}`
            );
            setResult(response.data);
            setTeamPage('');
        } catch (error: any) {
            setResult({ message: 'Error', error: error.response?.data?.error || error.message });
        } finally {
            setLoading(false);
        }
    };

    const handleBulkImportTournaments = async (e: React.FormEvent) => {
        e.preventDefault();
        const pages = bulkTournaments.split('\n').map(p => p.trim()).filter(p => p);
        if (pages.length === 0) return;

        setLoading(true);
        setResult(null);

        try {
            const response = await axios.post(
                '/api/admin/liquipedia/tournaments/bulk',
                pages
            );
            setResult(response.data);
            setBulkTournaments('');
        } catch (error: any) {
            setResult({ message: 'Error', error: error.response?.data?.error || error.message });
        } finally {
            setLoading(false);
        }
    };

    const handleBulkImportTeams = async (e: React.FormEvent) => {
        e.preventDefault();
        const pages = bulkTeams.split('\n').map(p => p.trim()).filter(p => p);
        if (pages.length === 0) return;

        setLoading(true);
        setResult(null);

        try {
            const response = await axios.post(
                '/api/admin/liquipedia/teams/bulk',
                pages
            );
            setResult(response.data);
            setBulkTeams('');
        } catch (error: any) {
            setResult({ message: 'Error', error: error.response?.data?.error || error.message });
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="liquipediaPage">
            <h1>Import Data from Liquipedia</h1>
            <p className="description">
                Fetch tournament and team data from Liquipedia and store it in the database.
                Use the exact page title from Liquipedia (e.g., "FaZe_Clan" or "Intel_Extreme_Masters/2024/Katowice").
            </p>

            {result && (
                <div className={`result ${result.error ? 'error' : 'success'}`}>
                    <h3>{result.message}</h3>
                    {result.error && <p className="errorText">{result.error}</p>}
                    {result.event && <p>Tournament imported: {result.event.name}</p>}
                    {result.team && <p>Team imported: {result.team.name}</p>}
                    {result.imported !== undefined && (
                        <p>Successfully imported {result.imported} out of {result.requested} items</p>
                    )}
                </div>
            )}

            <div className="importSection">
                <div className="importCard">
                    <h2>Import Single Tournament</h2>
                    <form onSubmit={handleImportTournament}>
                        <input
                            type="text"
                            value={tournamentPage}
                            onChange={(e) => setTournamentPage(e.target.value)}
                            placeholder="e.g., Intel_Extreme_Masters/2024/Katowice"
                            disabled={loading}
                        />
                        <button type="submit" disabled={loading || !tournamentPage.trim()}>
                            {loading ? 'Importing...' : 'Import Tournament'}
                        </button>
                    </form>
                    {samples.tournaments.length > 0 && (
                        <div className="samples">
                            <p>Sample page titles:</p>
                            <ul>
                                {samples.tournaments.map((title, idx) => (
                                    <li key={idx} onClick={() => setTournamentPage(title)}>
                                        {title}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>

                <div className="importCard">
                    <h2>Import Single Team</h2>
                    <form onSubmit={handleImportTeam}>
                        <input
                            type="text"
                            value={teamPage}
                            onChange={(e) => setTeamPage(e.target.value)}
                            placeholder="e.g., FaZe_Clan"
                            disabled={loading}
                        />
                        <button type="submit" disabled={loading || !teamPage.trim()}>
                            {loading ? 'Importing...' : 'Import Team'}
                        </button>
                    </form>
                    {samples.teams.length > 0 && (
                        <div className="samples">
                            <p>Sample page titles:</p>
                            <ul>
                                {samples.teams.map((title, idx) => (
                                    <li key={idx} onClick={() => setTeamPage(title)}>
                                        {title}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>
            </div>

            <div className="bulkImportSection">
                <div className="bulkCard">
                    <h2>Bulk Import Tournaments</h2>
                    <form onSubmit={handleBulkImportTournaments}>
                        <textarea
                            value={bulkTournaments}
                            onChange={(e) => setBulkTournaments(e.target.value)}
                            placeholder="Enter one page title per line..."
                            rows={6}
                            disabled={loading}
                        />
                        <button type="submit" disabled={loading || !bulkTournaments.trim()}>
                            {loading ? 'Importing...' : 'Bulk Import Tournaments'}
                        </button>
                    </form>
                </div>

                <div className="bulkCard">
                    <h2>Bulk Import Teams</h2>
                    <form onSubmit={handleBulkImportTeams}>
                        <textarea
                            value={bulkTeams}
                            onChange={(e) => setBulkTeams(e.target.value)}
                            placeholder="Enter one page title per line..."
                            rows={6}
                            disabled={loading}
                        />
                        <button type="submit" disabled={loading || !bulkTeams.trim()}>
                            {loading ? 'Importing...' : 'Bulk Import Teams'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}