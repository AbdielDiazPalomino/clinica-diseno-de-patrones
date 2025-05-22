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
    public void guardar(List<Cita> citas) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            for (Cita c : citas) {
                String linea = c.getMedico().getId() + "," +
                               c.getMedico().getNombre() + "," +
                               c.getMedico().getEspecialidad() + "," +
                               c.getPaciente().getId() + "," +
                               c.getPaciente().getNombre() + "," +
                               c.getPaciente().getEdad() + "," +
                               c.getFechaHora();
                writer.write(linea);
                writer.newLine();
            }
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
        String sql = "INSERT INTO medicos (nombre, especialidad) VALUES (?, ?)";
        
        /*try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, medico.getNombre());
            pstmt.setString(2, medico.getEspecialidad());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al guardar médico: " + e.getMessage());
        }*/
    }
    
}
