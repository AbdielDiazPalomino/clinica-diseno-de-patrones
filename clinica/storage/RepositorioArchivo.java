package clinica.storage;

import clinica.models.Cita;
import clinica.models.Medico;
import clinica.models.Paciente;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class RepositorioArchivo implements Repositorio {
    private final String archivo;

    public RepositorioArchivo(String archivo) {
        this.archivo = archivo;
    }

    @Override
    public void guardar(Cita citas) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            
                String linea = citas.getMedico().getId() + "," +
                               citas.getMedico().getNombre() + "," +
                               citas.getMedico().getEspecialidad() + "," +
                               citas.getPaciente().getId() + "," +
                               citas.getPaciente().getNombre() + "," +
                               citas.getPaciente().getEdad() + "," +
                               citas.getFechaHora();
                writer.write(linea);
                writer.newLine();
            
        } catch (IOException e) {
            System.out.println("Error al guardar archivo: " + e.getMessage());
        }
    }

    @Override
    public List<Cita> cargar() {
        List<Cita> citas = new ArrayList<>();
        File file = new File(archivo);
        if (!file.exists()) return citas;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] partes = linea.split(",", 7); // 7 campos exactos
                if (partes.length == 7) {
                    Medico medico = new Medico(
                            Integer.parseInt(partes[0]),
                            partes[1],
                            partes[2]);

                    Paciente paciente = new Paciente(
                            Integer.parseInt(partes[3]),
                            partes[4],
                            Integer.parseInt(partes[5]));

                    LocalDateTime fecha = LocalDateTime.parse(partes[6]);

                    citas.add(new Cita(medico, paciente, fecha));
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer archivo: " + e.getMessage());
        }

        return citas;
    }

    @Override
    public void guardarMedico(Medico medico) {
        
    }

    @Override
    public List<String> obtenerEspecialidades(){
        return null;
    }

    @Override
    public List<Medico> obtenerMedicosPorEspecialidad(String especialidad){
        return null;
    }
    
}
