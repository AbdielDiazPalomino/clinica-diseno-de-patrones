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
import java.util.List;



public class MenuPaciente extends JFrame {
    private final AgendaService agenda;
    private Paciente paciente;

    private JComboBox<String> cbEspecialidades;
    private JComboBox<Medico> cbMedicos;

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
        cbEspecialidades = new JComboBox<>();
        cbMedicos = new JComboBox<>();

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

        // Lista de especialidades
        //JComboBox<String> cbEspecialidades = new JComboBox<>();
        DefaultComboBoxModel<String> modeloEspecialidades = new DefaultComboBoxModel<>();
        modeloEspecialidades.addElement("Seleccione especialidad");

        List<String> especialidades = agenda.obtenerEspecialidades();
        
        if (especialidades != null) {
            especialidades.forEach(modeloEspecialidades::addElement);
        }

        cbEspecialidades.setModel(modeloEspecialidades);
        

        // Lista de médicos (actualizable)
        cbMedicos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Medico) {
                    Medico m = (Medico) value;
                    setText(m.getNombre() + " (" + m.getEspecialidad() + ")");
                }
                return this;
            }
        });

        // Actualizar médicos al seleccionar especialidad
        cbEspecialidades.addActionListener(e -> {
            if (cbEspecialidades.getSelectedIndex() > 0) {
                String especialidad = (String) cbEspecialidades.getSelectedItem();
                List<Medico> medicos = agenda.obtenerMedicosPorEspecialidad(especialidad);
                
                DefaultComboBoxModel<Medico> modeloMedicos = new DefaultComboBoxModel<>();
                medicos.forEach(modeloMedicos::addElement);
                cbMedicos.setModel(modeloMedicos);
            }
        });

        // Fecha y Hora
        JLabel lblFecha = new JLabel("Fecha y Hora (dd/MM/yyyy HH:mm):");
        JTextField txtFecha = new JTextField();
        panel.add(lblFecha);
        panel.add(txtFecha);
        panel.add(new JLabel("Especialidad:"));
        panel.add(cbEspecialidades);
        panel.add(new JLabel("Médico:"));
        panel.add(cbMedicos);

        // Botones
        JButton btnAgendar = new JButton("Agendar Cita");
        JButton btnCancelar = new JButton("Cancelar Cita");

        btnAgendar.addActionListener(e -> {
            Medico medicoSeleccionado = cbMedicos.getItemAt(cbMedicos.getSelectedIndex());
            // Validar selección
            if (medicoSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Seleccione un médico", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Generar ID automático (simulado)
            int idPaciente = generarIdPaciente(); 
            paciente = new Paciente(idPaciente, txtNombre.getText(), (int) spnEdad.getValue());
            
            
            // Parsear fecha
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                LocalDateTime fecha = LocalDateTime.parse(txtFecha.getText(), formatter);
                
                agenda.agendar(new Cita(medicoSeleccionado, paciente, fecha));
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