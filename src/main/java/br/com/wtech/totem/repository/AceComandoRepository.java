// src/main/java/br/com/wtech/totem/repository/AceComandoRepository.java
package br.com.wtech.totem.repository;

import br.com.wtech.totem.entity.Command;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AceComandoRepository extends JpaRepository<Command, Long> {
    /** Busca todos os comandos ainda não executados (status = 0) */
    List<Command> findByStatus(Integer status);
}
