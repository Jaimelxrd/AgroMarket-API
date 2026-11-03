package mz.co.agromarket.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import mz.co.agromarket.model.entity.PedidoCompra;
import mz.co.agromarket.model.enums.EstadoPedido;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {
    Page<PedidoCompra> findAllByCompradorId(Long compradorId, Pageable pageable);
    Page<PedidoCompra> findAllByProdutorId(Long produtorId, Pageable pageable);
    Page<PedidoCompra> findAllByProdutorIdAndEstado(Long produtorId, EstadoPedido estado, Pageable pageable);
    Page<PedidoCompra> findAllByEstado(EstadoPedido estado, Pageable pageable);
    long countByEstado(EstadoPedido estado);
    long countByCompradorId(Long compradorId);
    long countByCompradorIdAndEstado(Long compradorId, EstadoPedido estado);
    long countByProdutorIdAndEstado(Long produtorId, EstadoPedido estado);
    long countByProdutorId(Long produtorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoCompra p where p.id = :id")
    java.util.Optional<PedidoCompra> encontrarPorIdComBloqueio(@Param("id") Long id);
}
