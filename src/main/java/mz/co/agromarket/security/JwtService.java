package mz.co.agromarket.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import mz.co.agromarket.model.entity.Utilizador;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long duracaoMinutos;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.expiration-minutes:15}") long duracaoMinutos) {
        this.jwtEncoder = jwtEncoder;
        this.duracaoMinutos = duracaoMinutos;
    }

    public String gerarToken(Utilizador utilizador) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("agromarket")
                .issuedAt(agora)
                .expiresAt(agora.plus(duracaoMinutos, ChronoUnit.MINUTES))
                .subject(utilizador.getEmail())
                .claim("uid", utilizador.getId())
                .claim("role", utilizador.getTipo().name())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public static SecretKey criarChave(String segredoBase64) {
        if (segredoBase64 == null || segredoBase64.isBlank()) {
            throw new IllegalStateException("Defina a variável de ambiente JWT_SECRET com uma chave Base64.");
        }
        final byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(segredoBase64);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET deve conter uma chave codificada em Base64.", exception);
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET deve descodificar para pelo menos 32 bytes.");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
