package clinica.validadores;

import clinica.models.Cita;
import java.time.LocalDateTime;
import java.util.List;

public class DisponibilidadPorHorarioMedicoDecorator implements ValidadorDeCitas {
    private final ValidadorDeCitas validadorDecorado;

    public DisponibilidadPorHorarioMedicoDecorator(ValidadorDeCitas validador) {
        this.validadorDecorado = validador;
    }

    @Override
    public boolean esValida(Cita nuevaCita, List<Cita> citasExistentes) {
        if (!validadorDecorado.esValida(nuevaCita, citasExistentes)) {
            return false;
        }

        LocalDateTime inicioNueva = nuevaCita.getFechaHora();
        LocalDateTime finNueva = inicioNueva.plusMinutes(30);

        for (Cita citaExistente : citasExistentes) {
            if (citaExistente.getMedico().getId() == nuevaCita.getMedico().getId()) {
                LocalDateTime inicioExistente = citaExistente.getFechaHora();
                LocalDateTime finExistente = inicioExistente.plusMinutes(30);

                boolean seSolapan = !(finNueva.isBefore(inicioExistente) || inicioNueva.isAfter(finExistente.minusSeconds(1)));
                if (seSolapan) return false;
            }
        }
        return true;
    }
}