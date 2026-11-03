package mz.co.agromarket.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import mz.co.agromarket.model.enums.EstadoPedido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pedidos_compra")
@Getter
@Setter
@NoArgsConstructor
public class PedidoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Utilizador comprador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produtor_id", nullable = false)
    private Utilizador produtor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(name = "quantidade_solicitada", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidadeSolicitada;

    @Column(name = "preco_unitario_acordado", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoUnitarioAcordado;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(length = 1000)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDENTE;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @PrePersist
    void antesDeInserir() {
        LocalDateTime agora = LocalDateTime.now();
        if (dataCriacao == null) {
            dataCriacao = agora;
        }
        dataAtualizacao = agora;
    }

    @PreUpdate
    void antesDeAtualizar() {
        dataAtualizacao = LocalDateTime.now();
    }
}
