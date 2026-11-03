package mz.co.agromarket.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mensagens")
@Getter
@Setter
@NoArgsConstructor
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remetente_id", nullable = false)
    private Utilizador remetente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Utilizador destinatario;

    @Column(nullable = false, length = 4000)
    private String conteudo;

    @Column(name = "data_envio", nullable = false, updatable = false)
    private LocalDateTime dataEnvio;

    @Column(name = "lida", nullable = false)
    private boolean lida = false;

    @PrePersist
    void definirDataEnvio() {
        if (dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }
    }
}
