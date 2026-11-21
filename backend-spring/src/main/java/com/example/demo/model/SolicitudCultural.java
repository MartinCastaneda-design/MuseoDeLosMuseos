package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_culturales")
public class SolicitudCultural {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String titulo;

    @NotBlank
    private String descripcion;

    private String tipoSolicitud;

    private String museoNombre;

    private String solicitanteNombre;

    private String solicitanteCorreo;

    private String estado;

    private String respuesta;

    private LocalDateTime fechaCreacion;

    public SolicitudCultural() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public SolicitudCultural(Long id, String titulo, String descripcion, String tipoSolicitud, String museoNombre, String solicitanteNombre, String solicitanteCorreo, String estado, String respuesta) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipoSolicitud = tipoSolicitud;
        this.museoNombre = museoNombre;
        this.solicitanteNombre = solicitanteNombre;
        this.solicitanteCorreo = solicitanteCorreo;
        this.estado = estado != null ? estado : "PENDIENTE";
        this.respuesta = respuesta;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public void setTipoSolicitud(String tipoSolicitud) {
        this.tipoSolicitud = tipoSolicitud;
    }

    public String getMuseoNombre() {
        return museoNombre;
    }

    public void setMuseoNombre(String museoNombre) {
        this.museoNombre = museoNombre;
    }

    public String getSolicitanteNombre() {
        return solicitanteNombre;
    }

    public void setSolicitanteNombre(String solicitanteNombre) {
        this.solicitanteNombre = solicitanteNombre;
    }

    public String getSolicitanteCorreo() {
        return solicitanteCorreo;
    }

    public void setSolicitanteCorreo(String solicitanteCorreo) {
        this.solicitanteCorreo = solicitanteCorreo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
