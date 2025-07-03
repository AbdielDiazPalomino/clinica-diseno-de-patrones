package clinica.ui;

import clinica.models.Medico;
import clinica.services.AgendaService;
import clinica.services.BuscadorCitas;
import clinica.services.BuscadorDeCitas;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Botones principales
        JButton btnAgregarMedico = new JButton("Agregar Médico");
        JButton btnModificarMedico = new JButton("Modificar Médico");
        JButton btnBuscarMedico = new JButton("Buscar citas por médico");
        JButton btnBuscarPaciente = new JButton("Buscar citas por paciente");
        JButton btnBuscarFecha = new JButton("Buscar citas por fecha");
        JButton btnRegresar = new JButton("Regresar");
        JButton btnGestionEspecialidades = new JButton("Gestionar Especialidades");

        // Acciones de los botones
        btnAgregarMedico.addActionListener(e -> mostrarFormularioMedico());
        btnModificarMedico.addActionListener(e -> mostrarFormularioModificarMedico());
        btnBuscarMedico.addActionListener(e -> mostrarBusquedaPorMedico());
        btnBuscarPaciente.addActionListener(e -> mostrarBusquedaPorPaciente());
        btnBuscarFecha.addActionListener(e -> mostrarBusquedaPorFecha());
        btnRegresar.addActionListener(e -> dispose());
        btnGestionEspecialidades.addActionListener(e -> mostrarGestionEspecialidades());

        panel.add(btnAgregarMedico);
        panel.add(btnModificarMedico);
        panel.add(btnBuscarMedico);
        panel.add(btnBuscarPaciente);
        panel.add(btnBuscarFecha);
        panel.add(btnRegresar);
        panel.add(btnGestionEspecialidades);

        add(panel);
    }

    private void mostrarFormularioMedico() {
        JDialog dialog = new JDialog(this, "Nuevo Médico", true);
        dialog.setLayout(new GridLayout(4, 2, 8, 8));

        JTextField txtNombre = new JTextField();

        JComboBox<String> cbEspecialidad = new JComboBox<>(
                agenda.obtenerEspecialidadesDesdeTabla().toArray(new String[0]));
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

    private void mostrarGestionEspecialidades() {
        JDialog dialog = new JDialog(this, "Gestionar Especialidades", true);
        dialog.setLayout(new BorderLayout());

        JTextField txtNombreEspecialidad = new JTextField(20);
        JButton btnAgregar = new JButton("Agregar");
        JButton btnEliminar = new JButton("Eliminar");
        JList<String> lstEspecialidades = new JList<>(agenda.obtenerEspecialidadesDesdeTabla().toArray(new String[0]));

        JPanel panelSuperior = new JPanel(new GridLayout(1, 4, 10, 10));
        panelSuperior.add(new JLabel("Nombre de Especialidad:"));
        panelSuperior.add(txtNombreEspecialidad);
        panelSuperior.add(btnAgregar);
        panelSuperior.add(btnEliminar);

        dialog.add(panelSuperior, BorderLayout.NORTH);
        dialog.add(new JScrollPane(lstEspecialidades), BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> {
            String nombre = txtNombreEspecialidad.getText().trim();
            if (!nombre.isEmpty()) {
                agenda.guardarEspecialidad(nombre);
                actualizarListaEspecialidades(lstEspecialidades);
                txtNombreEspecialidad.setText("");
            } else {
                JOptionPane.showMessageDialog(dialog, "Ingrese un nombre para la especialidad", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnEliminar.addActionListener(e -> {
            if (lstEspecialidades.getSelectedIndex() != -1) {
                String nombre = lstEspecialidades.getSelectedValue();
                int id = obtenerIdEspecialidad(nombre);
                if (id != -1) {
                    agenda.eliminarEspecialidad(id);
                    actualizarListaEspecialidades(lstEspecialidades);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Seleccione una especialidad para eliminar", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void actualizarListaEspecialidades(JList<String> lstEspecialidades) {
        lstEspecialidades.setListData(agenda.obtenerEspecialidadesDesdeTabla().toArray(new String[0]));
    }

    private int obtenerIdEspecialidad(String nombre) {
        // Utilizar el repositorio existente
        return agenda.obtenerIdEspecialidad(nombre);
    }

    private void mostrarFormularioModificarMedico() {
        JDialog dialog = new JDialog(this, "Modificar Médico", true);
        dialog.setLayout(new GridLayout(5, 2, 8, 8));

        JTextField txtBusquedaNombre = new JTextField();
        JComboBox<Medico> cbMedicos = new JComboBox<>();
        JTextField txtNombre = new JTextField();

        JComboBox<String> cbEspecialidad = new JComboBox<>(
                agenda.obtenerEspecialidadesDesdeTabla().toArray(new String[0]));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        // Cargar todos los médicos en el JComboBox
        List<Medico> medicos = agenda.obtenerTodosLosMedicos();
        for (Medico medico : medicos) {
            cbMedicos.addItem(medico);
        }
        cbMedicos.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
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

    private void actualizarMedicosPorNombre(String nombre, JComboBox<Medico> cbMedicos) {
        if (!nombre.isEmpty()) {
            List<Medico> medicos = agenda.buscarPorNombreMedico(nombre);
            cbMedicos.removeAllItems();
            for (Medico medico : medicos) {
                cbMedicos.addItem(medico);
            }
            if (!medicos.isEmpty()) {
                cbMedicos.setSelectedIndex(0);
            }
        } else {
            cbMedicos.removeAllItems();
        }
    }

    private void mostrarBusquedaPorMedico() {
        JDialog dialog = new JDialog(this, "Buscar por Médico", true);
        dialog.setLayout(new BorderLayout());

        JTextField txtNombreMedico = new JTextField(20);
        JComboBox<Medico> cbMedicos = new JComboBox<>();
        JButton btnBuscar = new JButton("Buscar");

        // Tabla para mostrar resultados
        String[] columnas = { "Doctor", "Paciente", "Día", "Hora" };
        DefaultTableModel modeloTabla = new DefaultTableModel(columnas, 0);
        JTable tablaResultados = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaResultados);

        // Acción al escribir en el JTextField
        txtNombreMedico.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarMedicosPorNombre(txtNombreMedico.getText().trim(), cbMedicos);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarMedicosPorNombre(txtNombreMedico.getText().trim(), cbMedicos);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarMedicosPorNombre(txtNombreMedico.getText().trim(), cbMedicos);
            }
        });

        // Acción al seleccionar un médico del JComboBox
        cbMedicos.addActionListener(e -> {
            Medico medico = (Medico) cbMedicos.getSelectedItem();
            if (medico != null) {
                Integer medicoSeleccionado = medico.getId(); // Almacenar el ID del médico
            }
        });

        // Acción del botón Buscar
        btnBuscar.addActionListener(e -> {
            Medico medicoSeleccionado = (Medico) cbMedicos.getSelectedItem();
            if (medicoSeleccionado != null) {
                var citas = buscador.buscarPorMedico(medicoSeleccionado.getId());

                // Limpiar el modelo de la tabla
                modeloTabla.setRowCount(0);

                // Agregar las citas al modelo
                for (var cita : citas) {
                    String dia = cita.getFechaHora().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    String hora = cita.getFechaHora().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                    modeloTabla.addRow(new Object[] {
                            cita.getMedico().getNombre(),
                            cita.getPaciente().getNombre(),
                            dia,
                            hora
                    });
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Seleccione un médico para buscar sus citas", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("Nombre del Médico:"));
        panelSuperior.add(txtNombreMedico);
        panelSuperior.add(btnBuscar);

        JPanel panelMedicos = new JPanel();
        panelMedicos.add(new JLabel("Seleccionar Médico:"));
        panelMedicos.add(cbMedicos);

        dialog.add(panelSuperior, BorderLayout.NORTH);
        dialog.add(panelMedicos, BorderLayout.CENTER);
        dialog.add(scrollPane, BorderLayout.SOUTH);


        dialog.setSize(500, 600);
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
            buscador.buscarPorFecha(fecha).forEach(c -> resultados.append(c.toString() + "\n"));
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