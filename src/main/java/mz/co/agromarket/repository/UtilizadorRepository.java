package mz.co.agromarket.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import mz.co.agromarket.model.entity.Utilizador;
import mz.co.agromarket.model.enums.TipoUtilizador;

public interface UtilizadorRepository extends JpaRepository<Utilizador, Long> {
    Optional<Utilizador> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Page<Utilizador> findAllByTipo(TipoUtilizador tipo, Pageable pageable);
    long countByTipo(TipoUtilizador tipo);
    boolean existsByTipo(TipoUtilizador tipo);
}
