package mz.co.agromarket.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import mz.co.agromarket.model.entity.Utilizador;

import mz.co.agromarket.model.entity.Mensagem;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    @Query("""
            select m from Mensagem m
            where (m.remetente.id = :utilizadorA and m.destinatario.id = :utilizadorB)
               or (m.remetente.id = :utilizadorB and m.destinatario.id = :utilizadorA)
            order by m.dataEnvio desc
            """)
    Page<Mensagem> encontrarConversa(
            @Param("utilizadorA") Long utilizadorA,
            @Param("utilizadorB") Long utilizadorB,
            Pageable pageable);

    Page<Mensagem> findAllByDestinatarioIdAndLidaFalse(Long destinatarioId, Pageable pageable);

    @Query("""
            select distinct case when m.remetente.id = :utilizadorId then m.destinatario else m.remetente end
            from Mensagem m
            where m.remetente.id = :utilizadorId or m.destinatario.id = :utilizadorId
            """)
    List<Utilizador> encontrarInterlocutores(@Param("utilizadorId") Long utilizadorId);

    @Query("""
            select case when count(m) > 0 then true else false end
            from Mensagem m
            where (m.remetente.id = :utilizadorA and m.destinatario.id = :utilizadorB)
               or (m.remetente.id = :utilizadorB and m.destinatario.id = :utilizadorA)
            """)
    boolean existeConversa(@Param("utilizadorA") Long utilizadorA, @Param("utilizadorB") Long utilizadorB);

    Optional<Mensagem> findByIdAndDestinatarioId(Long id, Long destinatarioId);
}
