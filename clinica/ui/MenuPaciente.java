package clinica.ui;

import clinica.models.Cita;
import clinica.models.Medico;
import clinica.models.Paciente;
import clinica.services.AgendaService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import clinica.storage.RepositorioArchivo; // Añadir esta línea


public class MenuPaciente extends JFrame {
    private final AgendaService agenda;
    private Paciente paciente;
    private JComboBox<String> medicosComboBox;

    public MenuPaciente(AgendaService agenda) {
        this.agenda = agenda;
        configurarVentana();
        initComponents();
    }

    private void configurarVentana() {
        setTitle("Sistema de Citas - Paciente");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Registro de Paciente (ID automático)
        JLabel lblNombre = new JLabel("Nombre:");
        JTextField txtNombre = new JTextField();
        JLabel lblEdad = new JLabel("Edad:");
        JSpinner spnEdad = new JSpinner(new SpinnerNumberModel(18, 1, 120, 1));

        panel.add(lblNombre);
        panel.add(txtNombre);
        panel.add(lblEdad);
        panel.add(spnEdad);

        // Lista de Médicos
        JLabel lblMedico = new JLabel("Médico:");
        medicosComboBox = new JComboBox<>(new String[]{"Dr. Ana López - Cardiología", "Dr. Carlos Ruiz - Dermatología"});
        panel.add(lblMedico);
        panel.add(medicosComboBox);

        // Fecha y Hora
        JLabel lblFecha = new JLabel("Fecha y Hora (dd/MM/yyyy HH:mm):");
        JTextField txtFecha = new JTextField();
        panel.add(lblFecha);
        panel.add(txtFecha);

        // Botones
        JButton btnAgendar = new JButton("Agendar Cita");
        JButton btnCancelar = new JButton("Cancelar Cita");

        btnAgendar.addActionListener(e -> {
            // Generar ID automático (simulado)
            int idPaciente = generarIdPaciente(); 
            paciente = new Paciente(idPaciente, txtNombre.getText(), (int) spnEdad.getValue());
            
            // Obtener médico seleccionado
            String[] datosMedico = medicosComboBox.getSelectedItem().toString().split(" - ");
            Medico medico = new Medico(1, datosMedico[0], datosMedico[1]);
            
            // Parsear fecha
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                LocalDateTime fecha = LocalDateTime.parse(txtFecha.getText(), formatter);
                
                agenda.agendar(new Cita(medico, paciente, fecha));
                JOptionPane.showMessageDialog(this, "Cita agendada exitosamente!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Formato de fecha inválido!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> {
            new CancelarCitaDialog(this, agenda, paciente).setVisible(true);
        });

        panel.add(btnAgendar);
        panel.add(btnCancelar);

        add(panel);
    }

    private int generarIdPaciente() {
        // En una implementación real, consultaría la base de datos
        return (int) (Math.random() * 1000); // Simulación
    }

    // Diálogo para Cancelar Citas
    private static class CancelarCitaDialog extends JDialog {
        public CancelarCitaDialog(JFrame parent, AgendaService agenda, Paciente paciente) {
            super(parent, "Cancelar Cita", true);
            setSize(300, 200);
            setLocationRelativeTo(parent);
            
            JPanel panel = new JPanel(new BorderLayout());
            JList<Cita> listaCitas = new JList<>(agenda.listar().stream()
                .filter(c -> c.getPaciente().getId() == paciente.getId())
                .toArray(Cita[]::new));
            
            JButton btnCancelar = new JButton("Cancelar Seleccionada");
            btnCancelar.addActionListener(e -> {
                Cita seleccionada = listaCitas.getSelectedValue();
                if (seleccionada != null) {
                    agenda.cancelarCita(paciente.getId(), seleccionada.getFechaHora());
                    JOptionPane.showMessageDialog(this, "Cita cancelada!");
                    dispose();
                }
            });
            
            panel.add(new JScrollPane(listaCitas), BorderLayout.CENTER);
            panel.add(btnCancelar, BorderLayout.SOUTH);
            add(panel);
        }
    }

    public static void main(String[] args) {
        // Ejemplo de uso
        SwingUtilities.invokeLater(() -> {
            new MenuPaciente(new AgendaService(new RepositorioArchivo("citas.txt"))).setVisible(true);
        });
    }
}