package cs2.tournamentsite.tournamentserver.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import cs2.tournamentsite.tournamentserver.dto.AuthResponse;
import cs2.tournamentsite.tournamentserver.dto.LoginRequest;
import cs2.tournamentsite.tournamentserver.dto.RegisterRequest;
import cs2.tournamentsite.tournamentserver.models.Admin;
import cs2.tournamentsite.tournamentserver.repositories.AdminRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (adminRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new admin
        Admin admin = new Admin();
        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        
        Admin savedAdmin = adminRepository.save(admin);
        String token = jwtService.generateToken(savedAdmin);
        
        return new AuthResponse(token, savedAdmin.getId(), savedAdmin.getUsername(), savedAdmin.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        
        // If authentication successful, get admin details
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate JWT token
        String token = jwtService.generateToken(admin);
        
        return new AuthResponse(token, admin.getId(), admin.getUsername(), admin.getEmail());
    }
}
