package clinica.observer;

import java.util.ArrayList;
import java.util.List;

public class Observable {
    private List<Observer> observadores = new ArrayList<>();

    public void agregarObservador(Observer observador) {
        observadores.add(observador);
    }

    protected void notificarObservadores(String mensaje) {
        observadores.forEach(obs -> obs.actualizar(mensaje));
    }
}