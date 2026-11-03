package mz.co.agromarket.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mz.co.agromarket.model.entity.PerfilProdutor;

public interface PerfilProdutorRepository extends JpaRepository<PerfilProdutor, Long> {
    Optional<PerfilProdutor> findByUtilizadorId(Long utilizadorId);
    boolean existsByUtilizadorId(Long utilizadorId);
}
