package mz.co.agromarket.model.entity;

import java.time.LocalDateTime;

import mz.co.agromarket.model.enums.EstadoConta;
import mz.co.agromarket.model.enums.TipoUtilizador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "utilizadores")
@Getter
@Setter
@NoArgsConstructor
public class Utilizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "numero_telefone", nullable = false, length = 20)
    private String numeroTelefone;

    @Column(name = "palavra_passe_hash", nullable = false, length = 255)
    private String palavraPasseHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoUtilizador tipo = TipoUtilizador.COMPRADOR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoConta estado = EstadoConta.ATIVA;

    @Column(name = "data_registo", nullable = false, updatable = false)
    private LocalDateTime dataRegisto;

    @PrePersist
    void definirDataRegisto() {
        if (dataRegisto == null) {
            dataRegisto = LocalDateTime.now();
        }
    }
}
