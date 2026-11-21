package com.example.demo.repository;

import com.example.demo.model.SolicitudCultural;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SolicitudCulturalRepository extends JpaRepository<SolicitudCultural, Long> {

    List<SolicitudCultural> findByEstadoIgnoreCase(String estado);

    List<SolicitudCultural> findBySolicitanteCorreoIgnoreCase(String correo);
}
