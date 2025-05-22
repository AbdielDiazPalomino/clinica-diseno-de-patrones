package clinica.ui;

import clinica.models.Medico;
import clinica.services.AgendaService;
import clinica.services.BuscadorCitas;
import clinica.services.BuscadorDeCitas;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class MenuAdminGUI extends JFrame {
    private final AgendaService agenda;
    private final BuscadorCitas buscador;

    public MenuAdminGUI(AgendaService agenda) {
        this.agenda = agenda;
        this.buscador = new BuscadorDeCitas(agenda.listar());
        configurarVentana();
        initComponents();
    }

    private void configurarVentana() {
        setTitle("Panel de Administración");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Botones principales
        JButton btnAgregarMedico = new JButton("Agregar Médico");
        JButton btnBuscarMedico = new JButton("Buscar citas por médico");
        JButton btnBuscarPaciente = new JButton("Buscar citas por paciente");
        JButton btnBuscarFecha = new JButton("Buscar citas por fecha");

        // Acciones de los botones
        btnAgregarMedico.addActionListener(e -> mostrarFormularioMedico());
        btnBuscarMedico.addActionListener(e -> mostrarBusquedaPorMedico());
        btnBuscarPaciente.addActionListener(e -> mostrarBusquedaPorPaciente());
        btnBuscarFecha.addActionListener(e -> mostrarBusquedaPorFecha());

        panel.add(btnAgregarMedico);
        panel.add(btnBuscarMedico);
        panel.add(btnBuscarPaciente);
        panel.add(btnBuscarFecha);

        add(panel);
    }

    private void mostrarFormularioMedico() {
        JDialog dialog = new JDialog(this, "Nuevo Médico", true);
        dialog.setLayout(new GridLayout(4, 2));
        
        JTextField txtNombre = new JTextField();
        JTextField txtEspecialidad = new JTextField();
        JButton btnGuardar = new JButton("Guardar");

        dialog.add(new JLabel("Nombre:"));
        dialog.add(txtNombre);
        dialog.add(new JLabel("Especialidad:"));
        dialog.add(txtEspecialidad);
        dialog.add(new JLabel());
        dialog.add(btnGuardar);

        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String especialidad = txtEspecialidad.getText().trim();

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

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void mostrarBusquedaPorMedico() {
        JDialog dialog = new JDialog(this, "Buscar por Médico", true);
        dialog.setLayout(new BorderLayout());
        
        JTextField txtIdMedico = new JTextField();
        JButton btnBuscar = new JButton("Buscar");
        JTextArea resultados = new JTextArea();
        
        btnBuscar.addActionListener(e -> {
            int id = Integer.parseInt(txtIdMedico.getText());
            resultados.setText("");
            buscador.buscarPorMedico(id).forEach(c -> 
                resultados.append(c.toString() + "\n")
            );
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