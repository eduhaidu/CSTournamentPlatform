import AdminPageCardContent from '../utils/AdminPageCardContent';
import FunctionCard from '../components/FunctionCard';

export default function Admin() {
    return <div>
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