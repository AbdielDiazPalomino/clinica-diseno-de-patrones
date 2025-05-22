package clinica.storage;

import clinica.models.Cita;
import clinica.models.Medico;

import java.util.List;

public interface Repositorio {
    void guardar(List<Cita> citas);
    List<Cita> cargar();
    void guardarMedico(Medico medico); 
    List<String> obtenerEspecialidades();
    List<Medico> obtenerMedicosPorEspecialidad(String especialidad);

}
