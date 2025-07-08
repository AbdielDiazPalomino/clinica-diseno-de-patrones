package clinica.services;

import clinica.models.Cita;
import clinica.models.Medico;
import clinica.models.Paciente;
import clinica.storage.Repositorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import clinica.validadores.ValidadorBase;
import clinica.validadores.ValidadorDeCitas;

import clinica.observer.Observable;


public class AgendaService extends Observable {
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
            notificarObservadores("Nueva cita agendada para " + 
                nuevaCita.getPaciente().getNombre());
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
                repositorio.cancelarCita(idPaciente, fechaHora);
                notificarObservadores("Cita cancelada correctamente");
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

    public void guardarEspecialidad(String nombreEspecialidad) {
        repositorio.guardarEspecialidad(nombreEspecialidad);
    }

    public void eliminarEspecialidad(int idEspecialidad) {
        repositorio.eliminarEspecialidad(idEspecialidad);
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

    public List<String> obtenerEspecialidadesDesdeTabla() {
        return repositorio.obtenerEspecialidadesDesdeTabla();
    }

    public int obtenerIdEspecialidad(String nombre) {
        return repositorio.obtenerIdEspecialidad(nombre);
    }

    public List<Medico> buscarPorNombreMedico(String nombre) {
        return repositorio.buscarPorNombreMedico(nombre);
    }

    public List<Paciente> buscarPorNombrePaciente(String nombre) {
        return repositorio.buscarPorNombrePaciente(nombre);
    }

    public List<Cita> obtenerCitasPorMes(int year, int month) {
        return repositorio.obtenerCitasPorMes(year, month);
    }

    public List<Cita> obtenerCitasPorAno(int year) {
        return repositorio.obtenerCitasPorAno(year);
    }

}

