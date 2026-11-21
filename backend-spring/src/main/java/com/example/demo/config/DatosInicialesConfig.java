package com.example.demo.config;

import com.example.demo.model.SolicitudCultural;
import com.example.demo.repository.SolicitudCulturalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class DatosInicialesConfig {

    @Bean
    public CommandLineRunner cargarDatosIniciales(SolicitudCulturalRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                SolicitudCultural s1 = new SolicitudCultural(
                        null,
                        "Acceso a Archivo Histórico Colonial",
                        "Solicitud para consultar manuscritos del siglo XVIII sobre arte virreinal en Bogotá.",
                        "INVESTIGACION",
                        "Museo de Arte Colonial",
                        "Dra. Elena Ramos",
                        "investigador@museo.org",
                        "APROBADA",
                        "Autorizado para visita técnica el viernes en sala de investigadores."
                );

                SolicitudCultural s2 = new SolicitudCultural(
                        null,
                        "Visita Guiada Colegial 2026",
                        "Reserva de recorrido virtual y presencial para delegación de 30 estudiantes.",
                        "VISITA_GUIADA",
                        "Museo Nacional de Bellas Artes",
                        "María Fernanda López",
                        "maria@email.com",
                        "PENDIENTE",
                        null
                );

                SolicitudCultural s3 = new SolicitudCultural(
                        null,
                        "Ficha Técnica Obra Las Meninas",
                        "Consulta de informe de conservación y restauración de la pinacoteca.",
                        "INFORMACION_TECNICA",
                        "Museo Nacional del Prado",
                        "Dra. Elena Ramos",
                        "investigador@museo.org",
                        "EN_REVISION",
                        "En proceso de digitalización por el departamento técnico."
                );

                repository.saveAll(List.of(s1, s2, s3));
            }
        };
    }
}
