package clinica.storage;

import clinica.models.Cita;
import clinica.models.Medico;
import clinica.models.Paciente;

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
    void guardarEspecialidad(String nombreEspecialidad);
    void eliminarEspecialidad(int idEspecialidad);
    List<String> obtenerEspecialidadesDesdeTabla();
    int obtenerIdEspecialidad(String nombre);

    List<Medico> buscarPorNombreMedico(String nombre);
    List<Paciente> buscarPorNombrePaciente(String nombre);

}
