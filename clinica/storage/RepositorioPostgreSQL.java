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
    public void guardar(Cita nuevaCita) {
        String sqlPaciente = "INSERT INTO pacientes (nombre, edad) VALUES (?, ?) ON CONFLICT (nombre, edad) DO NOTHING RETURNING id";
        String sqlCita = "INSERT INTO citas (id_medico, id_paciente, fecha_hora) VALUES (?, ?, ?)";
        System.out.println("GUARDAR");
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);

            int idPaciente;

            // 1. Insertar paciente (si no existe) y obtener su ID
            try (PreparedStatement pstmtPaciente = conn.prepareStatement(sqlPaciente)) {
                pstmtPaciente.setString(1, nuevaCita.getPaciente().getNombre());
                pstmtPaciente.setInt(2, nuevaCita.getPaciente().getEdad());
                ResultSet rs = pstmtPaciente.executeQuery();

                if (rs.next()) {
                    idPaciente = rs.getInt("id");
                    System.out.println("[DEBUG] Nuevo ID Paciente: " + idPaciente);
                } else {
                    idPaciente = obtenerIdPaciente(conn, nuevaCita.getPaciente());
                    System.out.println("[DEBUG] ID Paciente existente: " + idPaciente);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

            // 2. Obtener ID médico
            int idMedico = nuevaCita.getMedico().getId();
            System.out.println("[DEBUG] ID Médico: " + idMedico); // 👈 Log

            // 3. Insertar cita
            try (PreparedStatement pstmtCita = conn.prepareStatement(sqlCita)) {
                pstmtCita.setInt(1, idMedico);
                pstmtCita.setInt(2, idPaciente);
                pstmtCita.setTimestamp(3, Timestamp.valueOf(nuevaCita.getFechaHora()));
                pstmtCita.executeUpdate();
                conn.commit(); // ✅ Confirmar transacción
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            System.err.println("Error al guardar cita: " + e.getMessage());
        }
    }

    @Override
    public void guardarMedico(Medico medico) {
        String sql = "INSERT INTO medicos (nombre, especialidad) VALUES (?, ?) ON CONFLICT (nombre, especialidad) DO NOTHING RETURNING id";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Insertar o obtener ID existente
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, medico.getNombre());
                pstmt.setString(2, medico.getEspecialidad());
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    medico.setId(rs.getInt("id")); // ID nuevo
                } else {
                    // Si ya existe, obtener su ID
                    String selectSql = "SELECT id FROM medicos WHERE nombre = ? AND especialidad = ?";
                    try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                        selectStmt.setString(1, medico.getNombre());
                        selectStmt.setString(2, medico.getEspecialidad());
                        ResultSet selectRs = selectStmt.executeQuery();
                        if (selectRs.next()) {
                            medico.setId(selectRs.getInt("id"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar médico: " + e.getMessage());
        }
    }

    @Override
    public void actualizarMedico(Medico medico) {
        String sql = "UPDATE medicos SET nombre = ?, especialidad = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, medico.getNombre());
            pstmt.setString(2, medico.getEspecialidad());
            pstmt.setInt(3, medico.getId());
            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Médico actualizado en la base de datos");
            } else {
                System.out.println("No se encontró médico con ID " + medico.getId());
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar médico: " + e.getMessage());
        }
    }

    @Override
    public Medico obtenerMedicoPorId(int id) {
        String sql = "SELECT id, nombre, especialidad FROM medicos WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Medico(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("especialidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener médico: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Medico> obtenerTodosLosMedicos() {
        List<Medico> medicos = new ArrayList<>();
        String sql = "SELECT id, nombre, especialidad FROM medicos";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                medicos.add(new Medico(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("especialidad")));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar médicos: " + e.getMessage());
        }
        return medicos;
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
                        rs.getString("especialidad"));

                Paciente paciente = new Paciente(
                        rs.getInt("pid"),
                        rs.getString("pnombre"),
                        rs.getInt("edad"));

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
            if (rs.next())
                return rs.getInt("id");
        }
        return -1;
    }

    private int obtenerIdPaciente(Connection conn, Paciente paciente) throws SQLException {
        String sql = "SELECT id FROM pacientes WHERE nombre = ? AND edad = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, paciente.getNombre());
            pstmt.setInt(2, paciente.getEdad());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt("id");
        }
        return -1;
    }

    @Override
    public List<String> obtenerEspecialidades() {
        List<String> especialidades = new ArrayList<>();
        String sql = "SELECT DISTINCT especialidad FROM medicos";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                especialidades.add(rs.getString("especialidad"));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar especialidades: " + e.getMessage());
        }
        return especialidades;
    }

    @Override
    public List<Medico> obtenerMedicosPorEspecialidad(String especialidad) {
        List<Medico> medicos = new ArrayList<>();
        String sql = "SELECT id, nombre FROM medicos WHERE especialidad = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, especialidad);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                medicos.add(new Medico(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        especialidad));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar médicos: " + e.getMessage());
        }
        return medicos;
    }

    @Override
    public void cancelarCita(int idPaciente, LocalDateTime fechaHora) {
        String sql = "DELETE FROM citas WHERE id_paciente = ? AND fecha_hora = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idPaciente);
            pstmt.setTimestamp(2, Timestamp.valueOf(fechaHora));
            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Cita eliminada de la base de datos");
            } else {
                System.out.println("No se encontró la cita en la base de datos");
            }

        } catch (SQLException e) {
            System.err.println("Error al cancelar cita: " + e.getMessage());
        }
    }

    @Override
    public void guardarEspecialidad(String nombreEspecialidad) {
        String sql = "INSERT INTO especialidades (nombre) VALUES (?) ON CONFLICT (nombre) DO NOTHING";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreEspecialidad);
            pstmt.executeUpdate();

            System.out.println("Especialidad guardada exitosamente");
        } catch (SQLException e) {
            System.err.println("Error al guardar especialidad: " + e.getMessage());
        }
    }

    @Override
    public void eliminarEspecialidad(int idEspecialidad) {
        String sql = "DELETE FROM especialidades WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idEspecialidad);
            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("Especialidad eliminada exitosamente");
            } else {
                System.out.println("No se encontró especialidad con ID " + idEspecialidad);
            }

        } catch (SQLException e) {
            System.err.println("Error al eliminar especialidad: " + e.getMessage());
        }
    }

    @Override
    public List<String> obtenerEspecialidadesDesdeTabla() {
        List<String> especialidades = new ArrayList<>();
        String sql = "SELECT nombre FROM especialidades";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                especialidades.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar especialidades: " + e.getMessage());
        }
        return especialidades;
    }

    @Override
    public int obtenerIdEspecialidad(String nombre) {
        String sql = "SELECT id FROM especialidades WHERE nombre = ?";
        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException ex) {
            System.err.println("Error al obtener ID de especialidad: " + ex.getMessage());
        }
        return -1;
    }

    @Override
    public List<Medico> buscarPorNombreMedico(String nombre) {
        List<Medico> medicos = new ArrayList<>();
        String sql = "SELECT id, nombre, especialidad FROM medicos WHERE nombre LIKE ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nombre + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                medicos.add(new Medico(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("especialidad")));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar médicos por nombre: " + e.getMessage());
        }
        return medicos;
    }

    @Override
    public List<Paciente> buscarPorNombrePaciente(String nombre) {
        List<Paciente> pacientes = new ArrayList<>();
        String sql = "SELECT id, nombre, edad FROM pacientes WHERE nombre LIKE ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nombre + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                pacientes.add(new Paciente(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("edad")));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar pacientes por nombre: " + e.getMessage());
        }
        return pacientes;
    }

    @Override
    public List<Cita> obtenerCitasPorMes(int year, int month) {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT m.id as mid, m.nombre as mnombre, m.especialidad, " +
                "p.id as pid, p.nombre as pnombre, p.edad, c.fecha_hora " +
                "FROM citas c " +
                "JOIN medicos m ON c.id_medico = m.id " +
                "JOIN pacientes p ON c.id_paciente = p.id " +
                "WHERE EXTRACT(YEAR FROM c.fecha_hora) = ? AND EXTRACT(MONTH FROM c.fecha_hora) = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Medico medico = new Medico(
                        rs.getInt("mid"),
                        rs.getString("mnombre"),
                        rs.getString("especialidad"));
                Paciente paciente = new Paciente(
                        rs.getInt("pid"),
                        rs.getString("pnombre"),
                        rs.getInt("edad"));
                LocalDateTime fecha = rs.getTimestamp("fecha_hora").toLocalDateTime();

                citas.add(new Cita(medico, paciente, fecha));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener citas por mes: " + e.getMessage());
        }
        return citas;
    }

    @Override
    public List<Cita> obtenerCitasPorAno(int year) {
        List<Cita> citas = new ArrayList<>();
        String sql = "SELECT m.id as mid, m.nombre as mnombre, m.especialidad, " +
                "p.id as pid, p.nombre as pnombre, p.edad, c.fecha_hora " +
                "FROM citas c " +
                "JOIN medicos m ON c.id_medico = m.id " +
                "JOIN pacientes p ON c.id_paciente = p.id " +
                "WHERE EXTRACT(YEAR FROM c.fecha_hora) = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, year);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Medico medico = new Medico(
                        rs.getInt("mid"),
                        rs.getString("mnombre"),
                        rs.getString("especialidad"));
                Paciente paciente = new Paciente(
                        rs.getInt("pid"),
                        rs.getString("pnombre"),
                        rs.getInt("edad"));
                LocalDateTime fecha = rs.getTimestamp("fecha_hora").toLocalDateTime();

                citas.add(new Cita(medico, paciente, fecha));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener citas por año: " + e.getMessage());
        }
        return citas;
    }
}
