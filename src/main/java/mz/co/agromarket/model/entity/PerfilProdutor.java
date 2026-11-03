package mz.co.agromarket.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "perfis_produtor")
@Getter
@Setter
@NoArgsConstructor
public class PerfilProdutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilizador_id", nullable = false, unique = true)
    private Utilizador utilizador;

    @Column(name = "nome_exploracao", nullable = false, length = 160)
    private String nomeExploracao;

    @Column(length = 2000)
    private String descricao;

    @Column(nullable = false, length = 80)
    private String provincia;

    @Column(nullable = false, length = 100)
    private String distrito;

    @Column(length = 120)
    private String localidade;

    @Column(length = 300)
    private String endereco;

    @Column(name = "tipo_producao", length = 120)
    private String tipoProducao;

    @Column(name = "contacto_alternativo", length = 20)
    private String contactoAlternativo;
}
