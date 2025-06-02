package models;

public class Usuario {
    public int id;
    public String nombre;
    public String apellido;
    public String email;

    public Usuario(int id, String nombre, String apellido, String email) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
    }
}
