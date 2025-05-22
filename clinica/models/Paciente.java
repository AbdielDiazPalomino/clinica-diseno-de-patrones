package clinica.models;
import java.io.Serializable;

public class Paciente implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private int edad;

    public Paciente(int id, String nombre, int edad) {
        this.id = id;
        this.nombre = nombre;
        this.edad = edad;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getEdad() {
        return edad;
    }

    @Override
    public String toString() {
        return nombre + ", " + edad + " años";
    }
}

