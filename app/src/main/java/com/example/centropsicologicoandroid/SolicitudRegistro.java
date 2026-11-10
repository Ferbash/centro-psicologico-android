package com.example.centropsicologicoandroid;

/**
 * Cuerpo de POST registrar.php. matricula y especialidad solo se mandan
 * para el rol "profesional" (si son null, Gson no los incluye en el JSON).
 */
public class SolicitudRegistro {
    private final String nombre;
    private final String email;
    private final String password;
    private final String ciudad;
    private final String rol;
    private final String telefono;
    private final String matricula;
    private final String especialidad;

    public SolicitudRegistro(String nombre, String email, String password, String ciudad,
                             String rol, String telefono, String matricula, String especialidad) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.ciudad = ciudad;
        this.rol = rol;
        this.telefono = telefono;
        this.matricula = matricula;
        this.especialidad = especialidad;
    }
}
