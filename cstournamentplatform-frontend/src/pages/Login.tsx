import axios from "axios";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export default function Login(){
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = (event: React.FormEvent) => {
        event.preventDefault();
        const formData = new FormData(event.target as HTMLFormElement);
        const data = {
            username: formData.get("username"),
            password: formData.get("password"),
        };
        axios.post("http://localhost:8080/api/auth/login", data)
            .then(response => {
                console.log("Login successful:", response.data);
                
                // Store token in localStorage
                localStorage.setItem("token", response.data.token);
                
                // Update auth context with user data
                login({
                    id: response.data.id,
                    username: response.data.username,
                    email: response.data.email
                });
                
                // Navigate to admin page
                navigate("/admin");
            })
            .catch(error => {
                console.error("Login failed:", error);
                alert("Login failed. Please check your credentials.");
            });
    };
    
    return (
    <div className="login">
        <h2>Login</h2>
        <form onSubmit={handleSubmit}>
            <label>
                Username:
                <input type="text" name="username" />
            </label>
            <br />
            <label>
                Password:
                <input type="password" name="password" />
            </label>
            <br />
            <input type="submit" value="Login" />
        </form>
        <p>Don't have an account? <a href="/register">Register here</a>.</p>
    </div>);
}