package modelo;

public class Persona {

    // Declaración de variables privadas de la clase "persona"
    private String nombre, telefono, email, categoria;
    private boolean favorito;

    // Constructor público de la clase "persona"
    public Persona() {
    }

    public Persona(String nombre, String telefono, String email, String categoria, boolean favorito) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.categoria = categoria;
        this.favorito = favorito;
    }

    //Getter and Setter
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }

    //metodos de la clase Persona
    // Método para proveer un formato para almacenar en un archivo
    public String datosContacto() {

        // Estructurar el siguiente formato: nombre;telefono;email;categoria;favorito
        // Por ejemplo: Daniela Poma;097145478;dpoma2024@gmail.com;amigo;true
        String contacto = String.format("%s;%s;%s;%s;%s", nombre, telefono, email, categoria, favorito); // Crea una cadena formateada con los valores de las variables
        return contacto; // Retorna la cadena formateada
    }

    //Método para proveer el formato de los campos que se van a imprimir en la lista
    public String formatoLista() {
        String contacto = String.format("%-40s%-40s%-40s%-40s", nombre, telefono, email, categoria);
        return contacto;
    }

}
