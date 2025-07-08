package clinica.observer;

public class NotificadorEmail implements Observer {
    @Override
    public void actualizar(String mensaje) {
        System.out.println("[EMAIL] Enviando email: " + mensaje);
        
    }
}