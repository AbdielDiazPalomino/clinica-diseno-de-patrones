package clinica.services;

import clinica.models.Cita;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class BuscadorDeCitas implements BuscadorCitas { // Implementa la interfaz
    private final List<Cita> citas;

    public BuscadorDeCitas(List<Cita> citas) {
        this.citas = citas;
    }

    @Override // Añadir anotación
    public List<Cita> buscarPorMedico(int idMedico) {
        return citas.stream()
                .filter(c -> c.getMedico().getId() == idMedico)
                .collect(Collectors.toList());
    }

    @Override // Añadir anotación
    public List<Cita> buscarPorPaciente(int idPaciente) {
        return citas.stream()
                .filter(c -> c.getPaciente().getId() == idPaciente)
                .collect(Collectors.toList());
    }

    @Override // Añadir anotación
    public List<Cita> buscarPorFecha(LocalDate fecha) {
        return citas.stream()
                .filter(c -> c.getFechaHora().toLocalDate().equals(fecha))
                .collect(Collectors.toList());
    }
}