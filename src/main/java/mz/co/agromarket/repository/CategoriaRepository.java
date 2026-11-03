package mz.co.agromarket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import mz.co.agromarket.model.entity.Categoria;
import mz.co.agromarket.model.enums.EstadoCategoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByEstadoOrderByNomeAsc(EstadoCategoria estado);
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
