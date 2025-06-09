package clinica.services;

import clinica.models.Cita;
import clinica.models.Medico;
import clinica.storage.Repositorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import clinica.validadores.ValidadorBase;
import clinica.validadores.ValidadorDeCitas;


public class AgendaService {
    private List<Cita> citas;
    private final Repositorio repositorio;
    private ValidadorDeCitas validadorChain;


    public AgendaService(Repositorio repositorio) {
        this.repositorio = repositorio;
        this.citas = repositorio.cargar();
        
        this.validadorChain = new ValidadorBase(); // Inicia con validación base
    }

    // Método para agregar decoradores externamente
    public void agregarValidador(ValidadorDeCitas decorador) {
        this.validadorChain = decorador;
    }


    public boolean agendar(Cita nuevaCita) {
        if (validadorChain.esValida(nuevaCita, citas)) {
            citas.add(nuevaCita);
            repositorio.guardar(nuevaCita);
            System.out.println("Cita agendada correctamente para " + nuevaCita.getPaciente().getNombre());
            return true;
        } else {
            System.out.println("Este horario se cruza con otra cita");
            return false;
        }
    }

    public boolean cancelarCita(int idPaciente, LocalDateTime fechaHora) {
        for (Cita cita : citas) {
            if (cita.getPaciente().getId() == idPaciente && cita.getFechaHora().equals(fechaHora)) {
                citas.remove(cita);
                repositorio.cancelarCita(idPaciente, fechaHora); // 👈 Llamada al repositorio
                System.out.println("Cita cancelada.");
                return true;
            }
        }
        System.out.println("No se encontró la cita para cancelar.");
        return false;
    }

    public List<Cita> listar() {
        return new ArrayList<>(citas);
    }

    public void agregarMedico(Medico medico) {
        repositorio.guardarMedico(medico);
    }

    public void actualizarMedico(Medico medico) {
        repositorio.actualizarMedico(medico);
    }

    public Medico obtenerMedicoPorId(int id) {
        return repositorio.obtenerMedicoPorId(id);
    }

    public List<String> obtenerEspecialidades() {
        List<String> especialidades = repositorio.obtenerEspecialidades();
        return especialidades != null ? especialidades : Collections.emptyList();
    }

    public List<Medico> obtenerMedicosPorEspecialidad(String especialidad) {
        return repositorio.obtenerMedicosPorEspecialidad(especialidad);
    }

    public List<Medico> obtenerTodosLosMedicos() {
        return repositorio.obtenerTodosLosMedicos();
    }
}

