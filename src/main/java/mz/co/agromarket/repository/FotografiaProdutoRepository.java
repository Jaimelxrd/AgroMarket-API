package mz.co.agromarket.repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import mz.co.agromarket.model.entity.FotografiaProduto;

public interface FotografiaProdutoRepository extends JpaRepository<FotografiaProduto, Long> {
    List<FotografiaProduto> findAllByProdutoIdOrderByOrdemAsc(Long produtoId);
    List<FotografiaProduto> findAllByProdutoIdInOrderByOrdemAsc(Collection<Long> produtoIds);
    Optional<FotografiaProduto> findByIdAndProdutoId(Long id, Long produtoId);
}
