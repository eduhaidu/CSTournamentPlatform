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
          <Route path="/teams" element={<TeamsList />} />
          <Route path="/admin/liquipedia" element={<Liquipedia />} />
        </Routes>
      </Router>
    </AuthProvider>
  )
}

export default App
