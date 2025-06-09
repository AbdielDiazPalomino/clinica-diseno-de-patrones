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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

        // Fecha y hora con JSpinner
        JLabel lblFecha = new JLabel("Fecha:");
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner spnFechaHora = new JSpinner(dateModel);

        // Formatear el spinner para que muestre fecha y hora legible
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spnFechaHora, "dd/MM/yyyy");
        spnFechaHora.setEditor(dateEditor);

        // Hora con JComboBox
        JLabel lblHora = new JLabel("Hora:");
        String[] horas = new String[24];
        for (int i = 0; i < 24; i++) {
            horas[i] = String.format("%02d", i);
        }
        JComboBox<String> cbHora = new JComboBox<>(horas);

        JLabel lblMinuto = new JLabel("Minutos:");
        String[] minutos = {"00", "15", "30", "45"};
        JComboBox<String> cbMinuto = new JComboBox<>(minutos);

        panel.add(lblFecha);
        panel.add(spnFechaHora);
        panel.add(lblHora);
        panel.add(cbHora);
        panel.add(lblMinuto);
        panel.add(cbMinuto);
        panel.add(new JLabel("Especialidad:"));
        panel.add(cbEspecialidades);
        panel.add(new JLabel("Médico:"));
        panel.add(cbMedicos);

        // Botones
        JButton btnAgendar = new JButton("Agendar Cita");
        JButton btnCancelar = new JButton("Cancelar Cita");

        btnAgendar.addActionListener(e -> {
            Medico medicoSeleccionado = cbMedicos.getItemAt(cbMedicos.getSelectedIndex());

            if (medicoSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Seleccione un médico", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Crear paciente
            int idPaciente = generarIdPaciente(); 
            paciente = new Paciente(idPaciente, txtNombre.getText(), (int) spnEdad.getValue());

            try {
            // Obtener la fecha
            java.util.Date fechaSeleccionada = (java.util.Date) spnFechaHora.getValue();
            LocalDate fecha = fechaSeleccionada.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Obtener hora y minutos del combo
            int hora = Integer.parseInt((String) cbHora.getSelectedItem());
            int minuto = Integer.parseInt((String) cbMinuto.getSelectedItem());

            // Combinar
            LocalDateTime fechaHora = fecha.atTime(hora, minuto);

            // Registrar cita
            boolean exito = agenda.agendar(new Cita(medicoSeleccionado, paciente, fechaHora));
            if(exito){
                JOptionPane.showMessageDialog(this, "Cita agendada exitosamente!");
            } else {
                JOptionPane.showMessageDialog(this, 
                "Este horario se cruza con otra cita. Por favor, elija otro horario.",
                "Error de programación",
                JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar la cita", "Error", JOptionPane.ERROR_MESSAGE);
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

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            // Resetear estado
            setForeground(Color.BLACK);
            setBackground(UIManager.getColor("Button.background"));
            setBorder(UIManager.getBorder("Button.border"));
            
            // Cambiar apariencia en hover/selección
            if (isSelected) {
                setBackground(Color.LIGHT_GRAY);
                setBorder(BorderFactory.createLineBorder(Color.BLUE));
            }
            
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private static class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private AgendaService agenda;
        private JTable table;
        private int row;

        public ButtonEditor(JCheckBox checkBox, AgendaService agenda, JTable table) {
            super(checkBox);
            this.agenda = agenda;
            this.table = table;
            button = new JButton("Cancelar");
            button.setFocusPainted(false); // Evitar borde feo al hacer clic

            // Activar el editor inmediatamente al hacer clic
            button.addActionListener(e -> {
                
                System.out.println("CANCELADO"); 
                
                ((DefaultTableModel) table.getModel()).removeRow(row);
                table.repaint();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            System.out.println("CANCELADO"); // Mensaje en consola
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

            // Listener para detectar clic en cualquier fila
            tablaCitas.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int filaSeleccionada = tablaCitas.getSelectedRow();
                    if (filaSeleccionada != -1) {
                        // Diálogo de confirmación
                        int respuesta = JOptionPane.showConfirmDialog(
                            this,
                            "¿Está seguro que desea cancelar esta cita?",
                            "Confirmar Cancelación",
                            JOptionPane.YES_NO_OPTION
                        );
                        
                        if (respuesta == JOptionPane.YES_OPTION) {
                            System.out.println("Cita CANCELADA");
                        }
                    }
                }
            });

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