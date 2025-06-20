package clinica.storage;

import clinica.models.Cita;
import clinica.models.Medico;

import java.time.LocalDateTime;
import java.util.List;

public interface Repositorio {
    void guardar(Cita cita);
    List<Cita> cargar();

    void guardarMedico(Medico medico); 
    void actualizarMedico(Medico medico);
    Medico obtenerMedicoPorId(int id);

    List<String> obtenerEspecialidades();
    List<Medico> obtenerMedicosPorEspecialidad(String especialidad);
    void cancelarCita(int idPaciente, LocalDateTime fechaHora);

    List<Medico> obtenerTodosLosMedicos(); 
    

}
