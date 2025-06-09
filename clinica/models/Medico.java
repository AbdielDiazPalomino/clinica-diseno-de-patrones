package clinica.models;
import java.io.Serializable;


public class Medico implements Serializable{
    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String especialidad;

    public Medico(int id, String nombre, String especialidad) {

        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    @Override
    public String toString() {
        return nombre + " (" + especialidad + ")";
    }
}
