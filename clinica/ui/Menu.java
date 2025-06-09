package clinica.ui;

import clinica.services.AgendaService;
import clinica.storage.RepositorioArchivo;

import javax.swing.*;
import java.awt.*;


public class Menu extends JFrame {
    private final AgendaService agenda;

    public Menu(AgendaService agenda) {
        this.agenda = agenda;
        configurarVentana();
        initComponents();
    }

    private void configurarVentana() {
        setTitle("Sistema de Citas - Principal");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        // Botón Paciente
        JButton btnPaciente = new JButton("Soy Paciente");
        btnPaciente.addActionListener(e -> {
            new MenuPaciente(agenda).setVisible(true);
        });

        // Botón Administrador
        JButton btnAdmin = new JButton("Soy Administrador");
        btnAdmin.addActionListener(e -> {
            mostrarLoginAdmin();
        });

        panel.add(btnPaciente);
        panel.add(btnAdmin);

        add(panel);
    }

    private void mostrarLoginAdmin() {
        JDialog loginDialog = new JDialog(this, "Login Administrador", true);       
        
        loginDialog.setSize(300, 200);
        loginDialog.setLayout(new GridLayout(3, 2, 10, 10));
        loginDialog.setLocationRelativeTo(this);

        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JButton btnLogin = new JButton("Ingresar");

        loginDialog.add(new JLabel("Usuario:"));
        loginDialog.add(txtUser);
        loginDialog.add(new JLabel("Clave:"));
        loginDialog.add(txtPass);
        loginDialog.add(new JLabel());
        loginDialog.add(btnLogin);

        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            if (user.equals("admin") && pass.equals("admin123")) {
                loginDialog.dispose();
                new MenuAdminGUI(agenda).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(loginDialog, 
                    "Credenciales incorrectas", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        loginDialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Menu(new AgendaService(new RepositorioArchivo("citas.txt"))).setVisible(true);
        });
    }
}

