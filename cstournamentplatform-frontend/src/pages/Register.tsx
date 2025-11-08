import axios from "axios";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export default function Register(){
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = (event: React.FormEvent) => {
        event.preventDefault();
        const formData = new FormData(event.target as HTMLFormElement);
        const data = {
            username: formData.get("username"),
            email: formData.get("email"),
            password: formData.get("password"),
        };
        axios.post("http://localhost:8080/api/auth/register", data)
            .then(response => {
                console.log("Registration successful:", response.data);
                
                // Store token in localStorage
                localStorage.setItem("token", response.data.token);
                
                // Update auth context with user data (auto-login after registration)
                login({
                    id: response.data.id,
                    username: response.data.username,
                    email: response.data.email
                });
                
                // Navigate to admin page
                navigate("/admin");
            })
            .catch(error => {
                console.error("Registration failed:", error);
                alert("Registration failed. Please try again.");
            });
    };
    
    return (
    <div className="register">
        <h2>Register</h2>
        <form onSubmit={handleSubmit}>
            <label>
                Username:
                <input type="text" name="username" />
            </label>
            <br />
            <label>
                Email:
                <input type="email" name="email" />
            </label>
            <br />
            <label>
                Password:
                <input type="password" name="password" />
            </label>
            <br />
            <input type="submit" value="Register"/>
        </form>
        <p>Already have an account? <a href="/login">Login here</a>.</p>
    </div>);
}