package com.project_estramipyme_backend.Security.controllers;

import com.project_estramipyme_backend.Security.security.JwtUtilRec;
import com.project_estramipyme_backend.user.model.UserModel;
import com.project_estramipyme_backend.user.repository.IUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthControllerRec {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private JwtUtilRec jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    @Operation(
            summary = "Forgot Password",
            description = "Initiates the password recovery process by sending a reset link to the user's email address."
    )
    @ApiResponse(responseCode = "200", description = "Password recovery email successfully sent")
    @ApiResponse(responseCode = "404", description = "User not found with the provided email")
    @ApiResponse(responseCode = "400", description = "Invalid email format or missing email field")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody String email) {
        // Verifica si el usuario existe en la base de datos
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Genera un token JWT con el email del usuario y un tiempo de expiración corto
        String token = jwtUtil.generateTokenForPasswordReset(email);

        // Envía el token al correo electrónico del usuario
        sendPasswordResetEmail(user.getEmail(), token);

        return ResponseEntity.ok("Correo de restablecimiento de contraseña enviado");
    }

    private void sendPasswordResetEmail(String email, String token) {
        String url = "http://localhost:8081/api/auth/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Restablecer tu contraseña");
        message.setText("Haz clic en el siguiente enlace para restablecer tu contraseña: \n" + url);

        mailSender.send(message);
    }
}
