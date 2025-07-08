package clinica.observer;

public class NotificadorSMS implements Observer {
    @Override
    public void actualizar(String mensaje) {
        System.out.println("[SMS] Enviando SMS: " + mensaje);
        
    }
}