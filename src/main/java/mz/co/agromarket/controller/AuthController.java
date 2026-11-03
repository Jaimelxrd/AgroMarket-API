package mz.co.agromarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import mz.co.agromarket.dto.request.LoginRequest;
import mz.co.agromarket.dto.request.RegistoUtilizadorRequest;
import mz.co.agromarket.dto.response.AuthTokenResponse;
import mz.co.agromarket.dto.response.UtilizadorResponse;
import mz.co.agromarket.service.AuthService;

@RestController
@Validated
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registo")
    public ResponseEntity<UtilizadorResponse> registar(@Valid @RequestBody RegistoUtilizadorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registar(request));
    }

    @PostMapping("/login")
    public AuthTokenResponse autenticar(@Valid @RequestBody LoginRequest request) {
        return authService.autenticar(request);
    }
}
