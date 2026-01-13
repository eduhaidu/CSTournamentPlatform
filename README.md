# CS2 Tournament Platform

A comprehensive web platform for managing and viewing Counter-Strike 2 tournaments, teams, players, and matches. Built with Spring Boot backend and React TypeScript frontend.

## 🎮 Features

### Public Features

- **Tournament Brackets**: Interactive bracket visualization with real-time match scores
- **Event Calendar**: View upcoming and past tournament events
- **Teams Directory**: Browse all registered teams with logos and rosters
- **Team Profiles**: Detailed team pages with roster information, country, coach, and founding date
- **Player Profiles**: Individual player pages with stats, photos, social links (Steam, FACEIT, Twitter), and team affiliation
- **Match Details**: View detailed match information including map scores

### Admin Features

- **Event Management**: Create, edit, and delete tournament events
- **Team Management**: Manage teams, upload logos, set team details
- **Player Management**:
  - Create and edit player profiles
  - Assign players to teams
  - Manage player status (Active, Benched, Free Agent)
  - Handle player social links and photos
- **Match Management**: Create and manage tournament matches
- **Liquipedia Integration**: Fetch tournament data from Liquipedia

## 🛠️ Tech Stack

### Backend

- **Java 17** with Spring Boot 3.x
- **Spring Security** with JWT authentication
- **Spring Data JPA** with Hibernate
- **MySQL** database
- **Maven** for dependency management
- **RESTful API** architecture

### Frontend

- **React 18** with TypeScript
- **Vite** for fast development and building
- **React Router** v6 for navigation
- **Axios** for HTTP requests
- **react-brackets** for tournament visualization
- **CSS3** with modern gradients and animations

## 📁 Project Structure

```
CSTournamentPlatform/
├── CSTournamentPlatform_Backend/
│   └── tournamentserver/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/cs2/tournamentsite/tournamentserver/
│       │   │   │   ├── config/          # Security & CORS configuration
│       │   │   │   ├── controllers/     # REST API controllers
│       │   │   │   ├── dto/             # Data Transfer Objects
│       │   │   │   ├── models/          # JPA entities
│       │   │   │   ├── repositories/    # Database repositories
│       │   │   │   └── services/        # Business logic
│       │   │   └── resources/
│       │   │       ├── application.properties
│       │   │       └── uploads/         # Team logos & player photos
│       │   └── test/
│       └── pom.xml
│
└── cstournamentplatform-frontend/
    ├── src/
    │   ├── components/          # Reusable React components
    │   │   ├── AuthProvider.tsx
    │   │   ├── TopBar.tsx
    │   │   ├── Bracket.tsx
    │   │   └── ...
    │   ├── pages/              # Page components
    │   │   ├── Home.tsx
    │   │   ├── EventPage.tsx
    │   │   ├── TeamProfile.tsx
    │   │   ├── PlayerProfile.tsx
    │   │   └── Admin/
    │   │       ├── EventsManager.tsx
    │   │       ├── TeamsManager.tsx
    │   │       ├── PlayersManager.tsx
    │   │       └── MatchesManager.tsx
    │   ├── config/             # Axios configuration
    │   ├── hooks/              # Custom React hooks
    │   ├── styles/             # CSS files
    │   └── utils/              # Utility functions
    └── package.json
```

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Node.js 20.19+** or 22.12+
- **MySQL 8.0+**
- **Maven 3.8+**

### Database Setup

1. Create a MySQL database:

```sql
CREATE DATABASE cs2_tournament_platform;
```

2. Update database credentials in `CSTournamentPlatform_Backend/tournamentserver/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cs2_tournament_platform
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Backend Setup

1. Navigate to the backend directory:

```bash
cd CSTournamentPlatform_Backend/tournamentserver
```

2. Install dependencies and run:

```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:

```bash
cd cstournamentplatform-frontend
```

2. Install dependencies:

```bash
npm install
```

3. Start the development server:

```bash
npm run dev
```

The frontend will start on `http://localhost:5173`

## 🔐 Authentication

The platform uses JWT (JSON Web Tokens) for authentication:

- **Public endpoints**: Events, teams, players, matches (read-only)
- **Protected endpoints**: Admin management pages require authentication
- **Token storage**: JWT tokens are stored in localStorage
- **Auto-refresh**: Sessions expire after inactivity

### Default Admin Setup

Create an admin user by registering and manually setting the role in the database, or use the registration endpoint with admin privileges.

## 📡 API Endpoints

### Public Endpoints

```
GET  /events/all              - Get all events
GET  /events/{id}             - Get event details
GET  /teams/all               - Get all teams
GET  /teams/{id}              - Get team details
GET  /players/all             - Get all players
GET  /players/{id}            - Get player details
GET  /players/team/{teamId}   - Get players by team
GET  /matches/tournament/{id} - Get tournament matches
GET  /maps/match/{matchId}    - Get match map details
```

### Admin Endpoints (Require Authentication)

```
POST   /events                - Create event
PUT    /events/{id}           - Update event
DELETE /events/{id}           - Delete event

POST   /teams                 - Create team
PUT    /teams/{id}            - Update team
DELETE /teams/{id}           - Delete team

POST   /players               - Create player
PUT    /players/{id}          - Update player
DELETE /players/{id}          - Delete player

POST   /matches               - Create match
PUT    /matches/{id}          - Update match
DELETE /matches/{id}          - Delete match
```

## 🎨 Key Features Explained

### Bracket Visualization

- Uses `react-brackets` library for tournament bracket display
- Real-time score updates
- Winner highlighting with blue accent
- Loser styling with strikethrough effect
- Dark theme with modern gradients

### Player Status Management

- **Active**: Currently playing on a team
- **Benched**: Part of a team but not in active roster
- **Free Agent**: Not associated with any team

### Team & Player Navigation

- Clickable team cards navigate to detailed team profiles
- Clickable player cards navigate to individual player pages
- Team profiles show full roster with player photos
- Player profiles link back to their current team

### Responsive Design

- Mobile-friendly layouts
- Adaptive grid systems
- Touch-friendly navigation
- Optimized for tablets and phones

## 🔧 Configuration

### Backend Configuration

Edit `application.properties` for:

- Database connection
- JWT secret key
- File upload paths
- Server port
- CORS settings

### Frontend Configuration

Edit `src/config/axios.ts` for:

- API base URL
- Request interceptors
- Response handling

## 🧪 Testing

### Backend Tests

```bash
cd CSTournamentPlatform_Backend/tournamentserver
mvn test
```

### Frontend Tests

```bash
cd cstournamentplatform-frontend
npm test
```

## 📦 Building for Production

### Backend

```bash
mvn clean package
java -jar target/tournamentserver-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
npm run build
```

The build output will be in the `dist/` directory.

## 🐛 Known Issues

- Node.js version warning with Vite (requires 20.19+ or 22.12+)
- File uploads require proper server permissions
- CORS must be configured for production deployment

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- **Eduard Haidu** - Initial development

## 🙏 Acknowledgments

- react-brackets for tournament bracket visualization
- Spring Boot for robust backend framework
- React community for excellent documentation
- Liquipedia for CS2 tournament data

## 📞 Support

For issues, questions, or contributions, please open an issue on GitHub.

---

**Last Updated**: January 2026
