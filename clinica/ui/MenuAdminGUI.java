package clinica.ui;

import clinica.models.Medico;
import clinica.services.AgendaService;
import clinica.services.BuscadorCitas;
import clinica.services.BuscadorDeCitas;


import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.stream.Collectors;
import java.util.List;


public class MenuAdminGUI extends JFrame {
    private final AgendaService agenda;
    private final BuscadorCitas buscador;

    public MenuAdminGUI(AgendaService agenda) {
        this.agenda = agenda;
        this.buscador = new BuscadorDeCitas(agenda);
        configurarVentana();
        initComponents();
    }

    private void configurarVentana() {
        setTitle("Panel de Administración");
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Botones principales
        JButton btnAgregarMedico = new JButton("Agregar Médico");
        JButton btnModificarMedico = new JButton("Modificar Médico");
        JButton btnBuscarMedico = new JButton("Buscar citas por médico");
        JButton btnBuscarPaciente = new JButton("Buscar citas por paciente");
        JButton btnBuscarFecha = new JButton("Buscar citas por fecha");
        JButton btnRegresar = new JButton("Regresar");

        // Acciones de los botones
        btnAgregarMedico.addActionListener(e -> mostrarFormularioMedico());
        btnModificarMedico.addActionListener(e -> mostrarFormularioModificarMedico());
        btnBuscarMedico.addActionListener(e -> mostrarBusquedaPorMedico());
        btnBuscarPaciente.addActionListener(e -> mostrarBusquedaPorPaciente());
        btnBuscarFecha.addActionListener(e -> mostrarBusquedaPorFecha());
        btnRegresar.addActionListener(e -> dispose());

        panel.add(btnAgregarMedico);
        panel.add(btnModificarMedico);
        panel.add(btnBuscarMedico);
        panel.add(btnBuscarPaciente);
        panel.add(btnBuscarFecha);
        panel.add(btnRegresar);

        add(panel);
    }

    private void mostrarFormularioMedico() {
        JDialog dialog = new JDialog(this, "Nuevo Médico", true);
        dialog.setLayout(new GridLayout(4, 2, 8, 8));
        
        JTextField txtNombre = new JTextField();

        String[] especialidades = {
        "Cardiología", "Pediatría", "Neurología", "Dermatología", 
        "Ginecología", "Psiquiatría", "Oftalmología", "Oncología"};

        JComboBox<String> cbEspecialidad = new JComboBox<>(especialidades);
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        dialog.add(new JLabel("Nombre:"));
        dialog.add(txtNombre);
        dialog.add(new JLabel("Especialidad:"));
        dialog.add(cbEspecialidad);
        dialog.add(btnGuardar);
        dialog.add(btnCancelar);
        

        btnGuardar.addActionListener(e -> {

            String nombre = txtNombre.getText().trim();
            String especialidad = (String) cbEspecialidad.getSelectedItem();

            if (!nombre.isEmpty() && !especialidad.isEmpty()) {
                Medico nuevoMedico = new Medico(0, nombre, especialidad); // ID se genera en la base de datos
                agenda.agregarMedico(nuevoMedico);
                JOptionPane.showMessageDialog(dialog, "Médico registrado exitosamente!");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Todos los campos son obligatorios", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void mostrarFormularioModificarMedico() {
        JDialog dialog = new JDialog(this, "Modificar Médico", true);
        dialog.setLayout(new GridLayout(5, 2, 8, 8));

        JTextField txtBusquedaNombre = new JTextField();
        JComboBox<Medico> cbMedicos = new JComboBox<>();
        JTextField txtNombre = new JTextField();
        String[] especialidades = {
            "Cardiología", "Pediatría", "Neurología", "Dermatología", 
            "Ginecología", "Psiquiatría", "Oftalmología", "Oncología"
        };
        JComboBox<String> cbEspecialidad = new JComboBox<>(especialidades);
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        // Cargar todos los médicos en el JComboBox
        List<Medico> medicos = agenda.obtenerTodosLosMedicos(); // New method needed in AgendaService
        for (Medico medico : medicos) {
            cbMedicos.addItem(medico);
        }
        cbMedicos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Medico) {
                    Medico medico = (Medico) value;
                    setText(medico.getNombre() + " (" + medico.getEspecialidad() + ")");
                }
                return this;
            }
        });

        // Filtrar médicos según el texto ingresado
        txtBusquedaNombre.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String texto = txtBusquedaNombre.getText().trim().toLowerCase();
                cbMedicos.removeAllItems();
                List<Medico> filtrados = medicos.stream()
                    .filter(m -> m.getNombre().toLowerCase().contains(texto))
                    .collect(Collectors.toList());
                for (Medico medico : filtrados) {
                    cbMedicos.addItem(medico);
                }
                if (!filtrados.isEmpty()) {
                    cbMedicos.setSelectedIndex(0);
                    Medico selected = (Medico) cbMedicos.getSelectedItem();
                    txtNombre.setText(selected.getNombre());
                    cbEspecialidad.setSelectedItem(selected.getEspecialidad());
                } else {
                    txtNombre.setText("");
                    cbEspecialidad.setSelectedIndex(0);
                }
            }
        });

        // Actualizar campos cuando se selecciona un médico
        cbMedicos.addActionListener(e -> {
            Medico selected = (Medico) cbMedicos.getSelectedItem();
            if (selected != null) {
                txtNombre.setText(selected.getNombre());
                cbEspecialidad.setSelectedItem(selected.getEspecialidad());
            }
        });

        dialog.add(new JLabel("Buscar Médico:"));
        dialog.add(txtBusquedaNombre);
        dialog.add(new JLabel("Seleccionar Médico:"));
        dialog.add(cbMedicos);
        dialog.add(new JLabel("Nombre:"));
        dialog.add(txtNombre);
        dialog.add(new JLabel("Especialidad:"));
        dialog.add(cbEspecialidad);
        dialog.add(btnGuardar);
        dialog.add(btnCancelar);

        btnGuardar.addActionListener(e -> {
            Medico selected = (Medico) cbMedicos.getSelectedItem();
            String nombre = txtNombre.getText().trim();
            String especialidad = (String) cbEspecialidad.getSelectedItem();

            if (selected != null && !nombre.isEmpty() && !especialidad.isEmpty()) {
                Medico medicoActualizado = new Medico(selected.getId(), nombre, especialidad);
                agenda.actualizarMedico(medicoActualizado);
                JOptionPane.showMessageDialog(dialog, "Médico actualizado exitosamente!");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Seleccione un médico y complete todos los campos", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void mostrarBusquedaPorMedico() {
        JDialog dialog = new JDialog(this, "Buscar por Médico", true);
        dialog.setLayout(new BorderLayout());
        
        JTextField txtIdMedico = new JTextField(10);
        JButton btnBuscar = new JButton("Buscar");
        JTextArea resultados = new JTextArea();
        
        btnBuscar.addActionListener(e -> {
             try {
            int id = Integer.parseInt(txtIdMedico.getText().trim());
            resultados.setText("");
            var citas = buscador.buscarPorMedico(id);

        if (citas.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, 
                "No se encontraron citas para el médico con ID " + id, 
                "Sin resultados", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            citas.forEach(c -> resultados.append(c.toString() + "\n"));
        }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "Ingrese un ID válido", "Error", JOptionPane.ERROR_MESSAGE);
        }
        });

        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("ID Médico:"));
        panelSuperior.add(txtIdMedico);
        panelSuperior.add(btnBuscar);

        dialog.add(panelSuperior, BorderLayout.NORTH);
        dialog.add(new JScrollPane(resultados), BorderLayout.CENTER);
        
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void mostrarBusquedaPorPaciente() {
        // Implementación similar a mostrarBusquedaPorMedico()
        JDialog dialog = new JDialog(this, "Buscar por Paciente", true);
        dialog.setLayout(new BorderLayout());
        
        JTextField txtIdPaciente = new JTextField(10);
        JButton btnBuscar = new JButton("Buscar");
        JTextArea resultados = new JTextArea();
        
        btnBuscar.addActionListener(e -> {
             try {
            int id = Integer.parseInt(txtIdPaciente.getText().trim());
            resultados.setText("");
            var citas = buscador.buscarPorPaciente(id);

        if (citas.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, 
                "No se encontraron citas para el paciente con ID " + id, 
                "Sin resultados", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            citas.forEach(c -> resultados.append(c.toString() + "\n"));
        }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "Ingrese un ID válido", "Error", JOptionPane.ERROR_MESSAGE);
        }
        });

        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("ID Paciente:"));
        panelSuperior.add(txtIdPaciente);
        panelSuperior.add(btnBuscar);

        dialog.add(panelSuperior, BorderLayout.NORTH);
        dialog.add(new JScrollPane(resultados), BorderLayout.CENTER);
        
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void mostrarBusquedaPorFecha() {
        JDialog dialog = new JDialog(this, "Buscar por Fecha", true);
        dialog.setLayout(new BorderLayout());
        
        JTextField txtFecha = new JTextField(LocalDate.now().toString());
        JButton btnBuscar = new JButton("Buscar");
        JTextArea resultados = new JTextArea();
        
        btnBuscar.addActionListener(e -> {
            LocalDate fecha = LocalDate.parse(txtFecha.getText());
            resultados.setText("");
            buscador.buscarPorFecha(fecha).forEach(c -> 
                resultados.append(c.toString() + "\n")
            );
        });

        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("Fecha (yyyy-MM-dd):"));
        panelSuperior.add(txtFecha);
        panelSuperior.add(btnBuscar);

        dialog.add(panelSuperior, BorderLayout.NORTH);
        dialog.add(new JScrollPane(resultados), BorderLayout.CENTER);
        
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}