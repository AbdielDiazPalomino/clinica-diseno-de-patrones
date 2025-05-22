package clinica.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Cita implements Serializable{
    private static final long serialVersionUID = 1L;

    private Medico medico;
    private Paciente paciente;
    private LocalDateTime fechaHora;

    public Cita(Medico medico, Paciente paciente, LocalDateTime fechaHora) {
        this.medico = medico;
        this.paciente = paciente;
        this.fechaHora = fechaHora;
    }

    public Medico getMedico() {
        return medico;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    @Override
    public String toString() {
        String fecha = fechaHora.toLocalDate().toString();
        String hora = fechaHora.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        return "Cita con " + medico.getNombre() + " para " + paciente.getNombre() +
               " el " + fecha + " a las " + hora;
    }
}
