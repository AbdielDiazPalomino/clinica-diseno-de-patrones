package clinica.services;

import clinica.models.Cita;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BuscadorDeCitas implements BuscadorCitas { // Implementa la interfaz
    private final AgendaService agenda;

    public BuscadorDeCitas(AgendaService agenda) {
        this.agenda = agenda;
    }

    @Override 
    public List<Cita> buscarPorMedico(int idMedico) {
        return agenda.listar().stream()
                .filter(cita -> cita.getMedico().getId() == idMedico)
                .collect(Collectors.toList());
    }

    @Override 
    public List<Cita> buscarPorPaciente(int idPaciente) {
        return agenda.listar().stream()
                .filter(c -> c.getPaciente().getId() == idPaciente)
                .collect(Collectors.toList());
    }

    @Override 
    public List<Cita> buscarPorFecha(LocalDate fecha) {
        return agenda.listar().stream()
                .filter(c -> c.getFechaHora().toLocalDate().equals(fecha))
                .collect(Collectors.toList());
    }

    @Override
    public List<Cita> buscarPorRangoFechas(LocalDate inicio, LocalDate fin) {
        return agenda.listar().stream()
                .filter(cita -> !cita.getFechaHora().toLocalDate().isBefore(inicio) && 
                                !cita.getFechaHora().toLocalDate().isAfter(fin))
                .collect(Collectors.toList());
    }
}