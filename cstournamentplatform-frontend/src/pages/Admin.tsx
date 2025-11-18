import AdminPageCardContent from '../utils/AdminPageCardContent';
import FunctionCard from '../components/FunctionCard';
import { useAuth } from '../hooks/useAuth';
import TopBar from '../components/TopBar';

export default function Admin() {
    const {user} = useAuth();
    return <div>
        <TopBar user={user} />
        <h1>Admin Dashboard</h1>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '20px' }}>
            {AdminPageCardContent.map((card, index) => (
                <FunctionCard
                    key={index}
                    title={card.title}
                    description={card.description}
                    link={card.link}
                    buttonText={card.buttonText}
                />
            ))}
        </div>
    </div>
}