package clinica.storage;

import clinica.models.Cita;
import clinica.models.Medico;
import clinica.models.Paciente;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RepositorioPostgreSQL implements Repositorio {
    private final String url = "jdbc:postgresql://localhost:5432/clinica-diseno-patrones";
    private final String user = "postgres";
    private final String password = "1234";

    @Override
    public void guardar(List<Cita> citas) {
        String sqlMedico = "INSERT INTO medicos (nombre, especialidad) VALUES (?, ?) ON CONFLICT DO NOTHING";
        String sqlPaciente = "INSERT INTO pacientes (nombre, edad) VALUES (?, ?) ON CONFLICT DO NOTHING";
        String sqlCita = "INSERT INTO citas (id_medico, id_paciente, fecha_hora) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            
            // Guardar médicos y pacientes primero
            for (Cita c : citas) {
                // Insertar médico
                try (PreparedStatement pstmt = conn.prepareStatement(sqlMedico, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, c.getMedico().getNombre());
                    pstmt.setString(2, c.getMedico().getEspecialidad());
                    pstmt.executeUpdate();
                }
                
                // Insertar paciente
                try (PreparedStatement pstmt = conn.prepareStatement(sqlPaciente, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, c.getPaciente().getNombre());
                    pstmt.setInt(2, c.getPaciente().getEdad());
                    pstmt.executeUpdate();
                }
            }

            // Guardar citas
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCita)) {
                for (Cita c : citas) {
                    // Obtener IDs de médico y paciente
                    int idMedico = obtenerIdMedico(conn, c.getMedico());
                    int idPaciente = obtenerIdPaciente(conn, c.getPaciente());
                    
                    pstmt.setInt(1, idMedico);
                    pstmt.setInt(2, idPaciente);
                    pstmt.setTimestamp(3, Timestamp.valueOf(c.getFechaHora()));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            
        } catch (SQLException e) {
            System.err.println("Error al guardar en PostgreSQL: " + e.getMessage());
        }
    }

    @Override
    public void guardarMedico(Medico medico) {
        String sql = "INSERT INTO medicos (nombre, especialidad) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, medico.getNombre());
            pstmt.setString(2, medico.getEspecialidad());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al guardar médico: " + e.getMessage());
        }
    }

    @Override
    public List<Cita> cargar() {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT m.id as mid, m.nombre as mnombre, m.especialidad, "
                   + "p.id as pid, p.nombre as pnombre, p.edad, c.fecha_hora "
                   + "FROM citas c "
                   + "JOIN medicos m ON c.id_medico = m.id "
                   + "JOIN pacientes p ON c.id_paciente = p.id";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Medico medico = new Medico(
                    rs.getInt("mid"),
                    rs.getString("mnombre"),
                    rs.getString("especialidad")
                );
                
                Paciente paciente = new Paciente(
                    rs.getInt("pid"),
                    rs.getString("pnombre"),
                    rs.getInt("edad")
                );
                
                LocalDateTime fecha = rs.getTimestamp("fecha_hora").toLocalDateTime();
                
                citas.add(new Cita(medico, paciente, fecha));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al cargar desde PostgreSQL: " + e.getMessage());
        }
        return citas;
    }

    // Métodos auxiliares para obtener IDs
    private int obtenerIdMedico(Connection conn, Medico medico) throws SQLException {
        String sql = "SELECT id FROM medicos WHERE nombre = ? AND especialidad = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, medico.getNombre());
            pstmt.setString(2, medico.getEspecialidad());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return -1;
    }

    private int obtenerIdPaciente(Connection conn, Paciente paciente) throws SQLException {
        String sql = "SELECT id FROM pacientes WHERE nombre = ? AND edad = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, paciente.getNombre());
            pstmt.setInt(2, paciente.getEdad());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return -1;
    }
}
