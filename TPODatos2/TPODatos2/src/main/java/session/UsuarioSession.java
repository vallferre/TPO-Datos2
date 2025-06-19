package session;

import com.datastax.oss.driver.api.core.CqlSession;
import connectors.CassandraConnector;
import model.Usuario;


public class UsuarioSession {
    private String nombre;
    private String direccion;
    private String documento;
    private String condicionIVA;
    private String email;

    public UsuarioSession(Usuario usuario) {
        this.nombre = usuario.getNombre();
        this.direccion = usuario.getDireccion();
        this.documento = usuario.getDocumento();
        this.condicionIVA = usuario.getCondicionIva();
        this.email = usuario.getEmail();
    }

    public void guardarUsuario() {
        CqlSession session = CassandraConnector.getSession();

        var result = session.execute("""
        SELECT documento FROM usuarios WHERE documento = ?
    """, documento);

        if (!result.iterator().hasNext()) {
            session.execute("""
            INSERT INTO usuarios (documento, nombre, direccion, condicioniva, email)
            VALUES (?, ?, ?, ?, ?)
        """, documento, nombre, direccion, condicionIVA, email);
            System.out.println(documento + " guardado con éxito.");
        } else {
            System.out.println("⚠️ El usuario con documento " + documento + " ya existe.");
        }
    }
}
