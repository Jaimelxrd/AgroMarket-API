package mz.co.agromarket.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import mz.co.agromarket.model.enums.EstadoProduto;
import mz.co.agromarket.model.enums.UnidadeMedida;
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
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(length = 3000)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produtor_id", nullable = false)
    private Utilizador produtor;

    @Column(name = "preco_unidade", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoUnidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false, length = 20)
    private UnidadeMedida unidadeMedida;

    @Column(name = "quantidade_disponivel", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidadeDisponivel;

    @Column(nullable = false, length = 80)
    private String provincia;

    @Column(nullable = false, length = 100)
    private String distrito;

    @Column(length = 120)
    private String localidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProduto estado = EstadoProduto.PENDENTE;

    @Column(name = "data_publicacao", nullable = false, updatable = false)
    private LocalDateTime dataPublicacao;

    @Version
    private Long versao;

    @PrePersist
    void definirDataPublicacao() {
        if (dataPublicacao == null) {
            dataPublicacao = LocalDateTime.now();
        }
    }
}
