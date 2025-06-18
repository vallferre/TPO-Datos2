package model;

public class Usuario {
    private String nombre;
    private String direccion;
    private String documento;
    private String condicionIva;
    private String email;

    public Usuario(String nombre, String direccion, String documento, String condicionIva, String email) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.documento = documento;
        this.condicionIva = condicionIva;
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getDocumento() {
        return documento;
    }

    public String getCondicionIva() {
        return condicionIva;
    }

    public String getEmail() {
        return email;
    }
}
