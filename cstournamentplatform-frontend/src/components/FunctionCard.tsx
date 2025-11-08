import '../styles/FunctionCard.css';

interface FunctionCardProps {
    title: string;
    description: string;
    link: string;
    buttonText: string;
}

export default function FunctionCard({ title, description, link, buttonText }: FunctionCardProps) {
    return <div className="functionCard">
        <h2>{title}</h2>
        <p>{description}</p>
        <a href={link}>
            <button>{buttonText}</button>
        </a>
    </div>
}