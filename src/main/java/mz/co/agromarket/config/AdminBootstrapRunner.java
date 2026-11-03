package mz.co.agromarket.config;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.EstadoConta;
import mz.co.agromarket.model.enums.TipoUtilizador;
import mz.co.agromarket.repository.UtilizadorRepository;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Pattern TELEFONE_MOCAMBICANO = Pattern.compile("^(\\+258)?(8[2-7]\\d{7}|2[1-7]\\d{7})$");

    private final UtilizadorRepository utilizadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean ativo;
    private final String nome;
    private final String email;
    private final String telefone;
    private final String palavraPasse;

    public AdminBootstrapRunner(
            UtilizadorRepository utilizadorRepository,
            PasswordEncoder passwordEncoder,
            @Value("${agromarket.admin.bootstrap.enabled:false}") boolean ativo,
            @Value("${agromarket.admin.bootstrap.nome:}") String nome,
            @Value("${agromarket.admin.bootstrap.email:}") String email,
            @Value("${agromarket.admin.bootstrap.telefone:}") String telefone,
            @Value("${agromarket.admin.bootstrap.palavra-passe:}") String palavraPasse) {
        this.utilizadorRepository = utilizadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.ativo = ativo;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.palavraPasse = palavraPasse;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!ativo || utilizadorRepository.existsByTipo(TipoUtilizador.ADMIN)) {
            return;
        }
        validarConfiguracao();

        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        if (utilizadorRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new IllegalStateException("ADMIN_EMAIL já está associado a outra conta.");
        }

        Utilizador admin = new Utilizador();
        admin.setNomeCompleto(nome.trim());
        admin.setEmail(emailNormalizado);
        admin.setNumeroTelefone(telefone.trim());
        admin.setPalavraPasseHash(passwordEncoder.encode(palavraPasse));
        admin.setTipo(TipoUtilizador.ADMIN);
        admin.setEstado(EstadoConta.ATIVA);
        utilizadorRepository.save(admin);
    }

    private void validarConfiguracao() {
        if (nome.isBlank() || email.isBlank() || !email.contains("@")
                || !TELEFONE_MOCAMBICANO.matcher(telefone.trim()).matches()
                || palavraPasse.length() < 12 || palavraPasse.length() > 72) {
            throw new IllegalStateException(
                    "Bootstrap ADMIN ativo: defina ADMIN_NAME, ADMIN_EMAIL, ADMIN_PHONE e ADMIN_PASSWORD válida (12 a 72 caracteres).");
        }
    }
}
