import './App.css'
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './components/AuthProvider';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Admin from './pages/Admin';
import Logout from './pages/Logout';
import EventsList from './pages/EventsList';
import Liquipedia from './pages/Liquipedia';
import TeamsList from './pages/TeamsList';
import TeamProfile from './pages/TeamProfile';
import PlayerProfile from './pages/PlayerProfile';
import EventsManager from './pages/Admin/EventsManager';
import TeamsManager from './pages/Admin/TeamsManager';
import PlayersManager from './pages/Admin/PlayersManager';
import MatchesManager from './pages/Admin/MatchesManager';
import EventPage from './pages/EventPage';
import EventCalendar from './pages/EventCalendar';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/admin" element={<Admin />} />
          <Route path="/logout" element={<Logout />} />
          <Route path="/events" element={<EventsList />} />
          <Route path="/events/:id" element={<EventPage/>} />
          <Route path="/events/calendar" element={<EventCalendar />} />
          <Route path="/teams" element={<TeamsList />} />
          <Route path="/team/:id" element={<TeamProfile />} />
          <Route path="/player/:id" element={<PlayerProfile />} />
          <Route path="/admin/liquipedia" element={<Liquipedia />} />
          <Route path="/admin/events" element={<EventsManager />} />
          <Route path="/admin/teams" element={<TeamsManager />} />          <Route path="/admin/players" element={<PlayersManager />} />          <Route path="/admin/players" element={<PlayersManager />} />
          <Route path="/admin/matches" element={<MatchesManager />} />
        </Routes>
      </Router>
    </AuthProvider>
  )
}

export default App
