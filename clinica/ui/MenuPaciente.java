package clinica.ui;

import clinica.models.Cita;
import clinica.models.Medico;
import clinica.models.Paciente;
import clinica.services.AgendaService;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import clinica.storage.RepositorioPostgreSQL;

import java.util.Comparator;
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
            new CancelarCitaDialog(this, agenda).setVisible(true); 
        });

        panel.add(btnAgendar);
        panel.add(btnCancelar);

        add(panel);
    }

    private int generarIdPaciente() {
        return -1;
    }

    private static class EstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value.equals("Completada")) {
                c.setBackground(Color.LIGHT_GRAY);
                c.setForeground(Color.DARK_GRAY);
            } else {
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());
            }
            return c;
        }
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private AgendaService agenda;
        private JTable table;
        private int row;

        // Constructor actualizado
        public ButtonEditor(JCheckBox checkBox, AgendaService agenda, JTable table) {
            super(checkBox);
            this.agenda = agenda;
            this.table = table;
            button = new JButton("Cancelar");
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row; // Guardar la fila actual
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            // Obtener datos de la fila seleccionada
            String fechaStr = (String) table.getModel().getValueAt(row, 0);
            String nombrePaciente = ((JTextField) ((JPanel) table.getParent().getParent().getComponent(0)).getComponent(1)).getText();
            
            // Cancelar la cita
            int respuesta = JOptionPane.showConfirmDialog(
                null, 
                "¿Cancelar cita del " + fechaStr + "?", 
                "Confirmar", 
                JOptionPane.YES_NO_OPTION
            );
            
            if (respuesta == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(null, "Cita cancelada!"); // Mostrar mensaje
            }
            
            return "Cancelar";
        }
    }

    // Diálogo para Cancelar Citas (Actualizado)
    private static class CancelarCitaDialog extends JDialog {
        public CancelarCitaDialog(JFrame parent, AgendaService agenda) {
            super(parent, "Cancelar Cita", true);
            setSize(600, 400);
            setLocationRelativeTo(parent);
            
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Campo para ingresar nombre del paciente
            JPanel panelBusqueda = new JPanel(new FlowLayout());
            JTextField txtNombre = new JTextField(20);
            JButton btnBuscar = new JButton("Buscar Citas");
            panelBusqueda.add(new JLabel("Nombre del paciente:"));
            panelBusqueda.add(txtNombre);
            panelBusqueda.add(btnBuscar);

            // Modelo de tabla para citas
            String[] columnas = {"Fecha", "Médico", "Especialidad", "Estado", "Acción"};
            DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Tabla no editable
                }
            };
            JTable tablaCitas = new JTable(modeloTabla);
            tablaCitas.setRowHeight(30);

            // Renderizado personalizado para el estado y botones
            tablaCitas.getColumnModel().getColumn(3).setCellRenderer(new EstadoRenderer());
            tablaCitas.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
            tablaCitas.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox(), agenda, tablaCitas)
);

            btnBuscar.addActionListener(e -> {
                String nombre = txtNombre.getText().trim();
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Ingrese un nombre", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                modeloTabla.setRowCount(0); // Limpiar tabla
                List<Cita> citas = agenda.listar().stream()
                    .filter(c -> c.getPaciente().getNombre().equalsIgnoreCase(nombre))
                    .sorted(Comparator.comparing(Cita::getFechaHora))
                    .toList();

                if (citas.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No se encontraron citas", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                for (Cita c : citas) {
                    boolean esPendiente = c.getFechaHora().isAfter(LocalDateTime.now());
                    Object[] fila = {
                        c.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        c.getMedico().getNombre(),
                        c.getMedico().getEspecialidad(),
                        esPendiente ? "Pendiente" : "Completada",
                        esPendiente ? "Cancelar" : ""
                    };
                    modeloTabla.addRow(fila);
                }
            });

            panel.add(panelBusqueda, BorderLayout.NORTH);
            panel.add(new JScrollPane(tablaCitas), BorderLayout.CENTER);
            add(panel);
        }
    }

    

    public static void main(String[] args) {
        // Ejemplo de uso
        SwingUtilities.invokeLater(() -> {
            RepositorioPostgreSQL repoPostgres = new RepositorioPostgreSQL();
            new MenuPaciente(new AgendaService(repoPostgres)).setVisible(true);
        });
    }
}