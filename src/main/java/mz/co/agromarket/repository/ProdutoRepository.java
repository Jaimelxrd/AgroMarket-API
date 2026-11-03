package mz.co.agromarket.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mz.co.agromarket.model.entity.Produto;
import mz.co.agromarket.model.enums.EstadoProduto;
import jakarta.persistence.LockModeType;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Page<Produto> findAllByProdutorId(Long produtorId, Pageable pageable);

    Page<Produto> findAllByEstadoOrderByDataPublicacaoDesc(EstadoProduto estado, Pageable pageable);

    long countByEstado(EstadoProduto estado);
    long countByProdutorId(Long produtorId);
    long countByProdutorIdAndEstado(Long produtorId, EstadoProduto estado);

    Optional<Produto> findByIdAndProdutorId(Long id, Long produtorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> encontrarPorIdComBloqueio(@Param("id") Long id);

    @Query(value = "select c.nome, count(p.id) from categorias c left join produtos p on p.categoria_id = c.id group by c.id, c.nome order by c.nome", nativeQuery = true)
    java.util.List<Object[]> contarProdutosPorCategoria();

    @Query(value = "select provincia, count(id) from produtos group by provincia order by provincia", nativeQuery = true)
    java.util.List<Object[]> contarProdutosPorProvincia();

    @Query("""
            select p from Produto p
            where (:nome is null or lower(p.nome) like lower(concat('%', :nome, '%')))
              and (:categoriaId is null or p.categoria.id = :categoriaId)
              and (:provincia is null or lower(p.provincia) = lower(:provincia))
              and (:distrito is null or lower(p.distrito) = lower(:distrito))
              and (:precoMinimo is null or p.precoUnidade >= :precoMinimo)
              and (:precoMaximo is null or p.precoUnidade <= :precoMaximo)
              and (:produtorId is null or p.produtor.id = :produtorId)
              and (:estado is null or p.estado = :estado)
              and p.estado not in (mz.co.agromarket.model.enums.EstadoProduto.PENDENTE, mz.co.agromarket.model.enums.EstadoProduto.REJEITADO)
              and (:disponivel is null or (:disponivel = true and p.estado = mz.co.agromarket.model.enums.EstadoProduto.DISPONIVEL and p.quantidadeDisponivel > 0)
                   or (:disponivel = false and (p.estado <> mz.co.agromarket.model.enums.EstadoProduto.DISPONIVEL or p.quantidadeDisponivel <= 0)))
            """)
    Page<Produto> pesquisar(
            @Param("nome") String nome,
            @Param("categoriaId") Long categoriaId,
            @Param("provincia") String provincia,
            @Param("distrito") String distrito,
            @Param("precoMinimo") BigDecimal precoMinimo,
            @Param("precoMaximo") BigDecimal precoMaximo,
            @Param("produtorId") Long produtorId,
            @Param("estado") EstadoProduto estado,
            @Param("disponivel") Boolean disponivel,
            Pageable pageable);
}
