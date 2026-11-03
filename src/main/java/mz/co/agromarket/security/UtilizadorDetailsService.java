package mz.co.agromarket.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.EstadoConta;
import mz.co.agromarket.repository.UtilizadorRepository;

@Service
public class UtilizadorDetailsService implements UserDetailsService {

    private final UtilizadorRepository utilizadorRepository;

    public UtilizadorDetailsService(UtilizadorRepository utilizadorRepository) {
        this.utilizadorRepository = utilizadorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilizador utilizador = utilizadorRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));

        return User.withUsername(utilizador.getEmail())
                .password(utilizador.getPalavraPasseHash())
                .authorities("ROLE_" + utilizador.getTipo().name())
                .disabled(utilizador.getEstado() != EstadoConta.ATIVA)
                .build();
    }
}
