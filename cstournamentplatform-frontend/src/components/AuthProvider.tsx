import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import React, { createContext, useState } from 'react';
import SessionExpired from './SessionExpired';

export interface User {
    id: number;
    username: string;
    email: string;
}

interface AuthContextType {
    user: User | null;
    isLoggedIn: boolean;
    login: (userData: User) => void;
    logout: () => void;
}

export const AuthContext = createContext<AuthContextType>({
    user: null,
    isLoggedIn: false,
    login: () => {},
    logout: () => {}
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [sessionExpired, setSessionExpired] = useState<boolean>(false);
    const stompClientRef = React.useRef<Client | null>(null);

    const login = (userData: User) => {
        setUser(userData);
        // Optionally store in localStorage for persistence
        localStorage.setItem('user', JSON.stringify(userData));
        if(stompClientRef.current?.connected){
            stompClientRef.current.deactivate();
        }
        connectWebSocket(userData.username);
    };
    
    const logout = () => {
        if (stompClientRef.current?.connected) {
            stompClientRef.current.deactivate();
        }
        setUser(null);
        localStorage.removeItem('user');
        localStorage.removeItem('token');
    };

    const connectWebSocket = (username: string) =>{
        const client = new Client({
            brokerURL: 'ws://localhost:8080/ws',
            connectHeaders: {},
            debug: function (str) {
                console.log(str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        client.webSocketFactory = () => {
            return new SockJS('http://localhost:8080/ws') as any;
        };
        
        client.onConnect = () => {
            console.log('WebSocket connected');
            client.subscribe(`/user/${username}/queue/session-expiry`, () => {
                console.log('Session expiry notification received');
                setSessionExpired(true);
                client.deactivate();
                logout();
            });
        };
        client.activate();
        stompClientRef.current = client;
    }


    // Load user from localStorage on mount
    React.useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const userData = JSON.parse(storedUser);
            setUser(userData);
            connectWebSocket(userData.username);
        }
    }, []);

    return (
        <AuthContext.Provider value={{ user, isLoggedIn: user !== null, login, logout }}>
            {children}
            {sessionExpired && (
                <div className="sessionExpiredOverlay">
                    <SessionExpired />
                </div>
            )}
        </AuthContext.Provider>
    );
}