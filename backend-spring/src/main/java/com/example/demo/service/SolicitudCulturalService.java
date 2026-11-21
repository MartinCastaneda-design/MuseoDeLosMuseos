package com.example.demo.service;

import com.example.demo.model.SolicitudCultural;
import com.example.demo.repository.SolicitudCulturalRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SolicitudCulturalService {

    private final SolicitudCulturalRepository repository;

    public SolicitudCulturalService(SolicitudCulturalRepository repository) {
        this.repository = repository;
    }

    public List<SolicitudCultural> listarTodas() {
        return repository.findAll();
    }

    public Optional<SolicitudCultural> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public SolicitudCultural crear(SolicitudCultural solicitud) {
        if (solicitud.getEstado() == null || solicitud.getEstado().isBlank()) {
            solicitud.setEstado("PENDIENTE");
        }
        return repository.save(solicitud);
    }

    public Optional<SolicitudCultural> actualizar(Long id, SolicitudCultural datos) {
        return repository.findById(id).map(existente -> {
            existente.setTitulo(datos.getTitulo());
            existente.setDescripcion(datos.getDescripcion());
            existente.setTipoSolicitud(datos.getTipoSolicitud());
            existente.setMuseoNombre(datos.getMuseoNombre());
            existente.setSolicitanteNombre(datos.getSolicitanteNombre());
            existente.setSolicitanteCorreo(datos.getSolicitanteCorreo());
            if (datos.getEstado() != null && !datos.getEstado().isBlank()) {
                existente.setEstado(datos.getEstado());
            }
            if (datos.getRespuesta() != null) {
                existente.setRespuesta(datos.getRespuesta());
            }
            return repository.save(existente);
        });
    }

    public boolean eliminar(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
