package clinica;

import javax.swing.SwingUtilities;

import clinica.observer.NotificadorEmail;
import clinica.observer.NotificadorSMS;
import clinica.services.AgendaService;
import clinica.storage.Repositorio;
//import clinica.storage.RepositorioArchivo;
import clinica.storage.RepositorioPostgreSQL;
import clinica.ui.Menu;
import clinica.validadores.DisponibilidadPorHorarioMedicoDecorator;
import clinica.validadores.ValidadorBase;
import clinica.validadores.ValidadorDeCitas;

/*
 * El principio Open/Closed dice que una clase debe estar abierta para extensión pero cerrada para 
 * modificación. Aplicamos OCP en:

 * En todo el sistema:
 * Repositorio es una interfaz: Se puede agregar más implementaciones (RepositorioMemoria, 
 * RepositorioMySQL, etc.) sin modificar AgendaService.

 * AgendaService no conoce los detalles del guardado. Solo usa Repositorio (depende de la abstracción).

 * Si más adelante deseamos validaciones adicionales (días festivos, doctores de vacaciones, etc.), 
 * podremos extender con una clase ValidadorDeCitas sin tocar AgendaService.
 * 
 * Open/Closed Principle:

 * Puedes crear nuevos tipos de buscadores implementando la interfaz BuscadorCitas

 * Ejemplo: BuscadorAvanzado con filtros combinados sin modificar el MenuAdmin

 * Inversión de dependencias:

 * El MenuAdmin depende de una abstracción (BuscadorCitas) no de una implementación concreta

 * Open/Closed Principle:

 * AgendaService está cerrado para modificación (no cambia su código al añadir validadores).

 * Abierto para extensión: Puedes añadir nuevos decoradores desde fuera.

 * Implementación correcta del Decorator:

 * La cadena se construye externamente: ValidadorBase -> DisponibilidadDecorator.

 * Cada decorador envuelve al validador anterior.

 * Flexibilidad:

 * se puede añadir validadores desde cualquier parte del código:
 */

public class Main {
    public static void main(String[] args) {
        
        //AgendaService servicio = new AgendaService(new RepositorioArchivo("citas.txt"));
        Repositorio repositorio = new RepositorioPostgreSQL();
        AgendaService servicio = new AgendaService(repositorio);
        
        ValidadorDeCitas validador = new ValidadorBase();
        validador = new DisponibilidadPorHorarioMedicoDecorator(validador);
        servicio.agregarValidador(validador);

        servicio.agregarObservador(new NotificadorEmail());
        servicio.agregarObservador(new NotificadorSMS());
        
        // Mostrar la ventana principal
        SwingUtilities.invokeLater(() -> {
            new Menu(servicio).setVisible(true); 
        });
    }
}


