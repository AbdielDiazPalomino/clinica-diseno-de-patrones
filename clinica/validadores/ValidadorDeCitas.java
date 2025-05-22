package clinica.validadores;

import clinica.models.Cita;
import java.util.List;

public interface ValidadorDeCitas {
    
    public boolean esValida(Cita nuevaCita, List<Cita> citasExistentes);
}