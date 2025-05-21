package modelo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAO {

    private static final String RUTA = "c:/gestionContactos/datosContactos.csv";
    private File archivo;
    private Persona persona;

    public static synchronized void exportarContactosJSON(List<Persona> lista, String rutaDestino) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(rutaDestino)) {
            gson.toJson(lista, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Constructor vacío: se usa para leer o actualizar el archivo
    public PersonaDAO() {
        this.archivo = new File(RUTA);
        prepararArchivo();
    }

    // Constructor con persona: se usa para escribir un nuevo contacto
    public PersonaDAO(Persona persona) {
        this.persona = persona;
        this.archivo = new File(RUTA);
        prepararArchivo();
    }

    // Método para crear el archivo si no existe
    private void prepararArchivo() {
        try {
            File carpeta = new File(archivo.getParent());
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }
            if (!archivo.exists()) {
                archivo.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribir un solo contacto (se llama desde agregarContacto)
    public synchronized void escribir() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, true))) {
            bw.write(persona.getNombre() + ","
                    + persona.getTelefono() + ","
                    + persona.getEmail() + ","
                    + persona.getCategoria() + ","
                    + persona.isFavorito());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Leer todos los contactos
    public List<Persona> leerArchivo() throws IOException {
        List<Persona> lista = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 5) {
                    String nombre = datos[0];
                    String telefono = datos[1];
                    String email = datos[2];
                    String categoria = datos[3];
                    boolean favorito = Boolean.parseBoolean(datos[4]);

                    Persona p = new Persona(nombre, telefono, email, categoria, favorito);
                    lista.add(p);
                }
            }
        }

        return lista;
    }

    // Reescribe todo el archivo (usado para editar o eliminar contactos)
    public synchronized void actualizarContactos(List<Persona> lista) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            for (Persona p : lista) {
                bw.write(p.getNombre() + ","
                        + p.getTelefono() + ","
                        + p.getEmail() + ","
                        + p.getCategoria() + ","
                        + p.isFavorito());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Metodo de validacion, verifica si un contacto ya existe
    public boolean existeContacto(String nombre, String telefono) {
        try {
            List<Persona> contactos = leerArchivo();
            for (Persona p : contactos) {
                if (p.getNombre().equalsIgnoreCase(nombre.trim())
                        && p.getTelefono().equalsIgnoreCase(telefono.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Metodo de busqueda
    public List<Persona> buscarPorNombre(String textoBusqueda) {
        List<Persona> resultados = new ArrayList<>();
        try {
            List<Persona> contactos = leerArchivo();
            for (Persona p : contactos) {
                if (p.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase())) {
                    resultados.add(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultados;
    }

    //Metodo estático sincronizado para exportar la lista de contactos a un CSV.
    public static synchronized void exportarContactosCSV(List<Persona> lista, String rutaDestino) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaDestino))) {
            for (Persona p : lista) {
                bw.write(p.getNombre() + "," + p.getTelefono() + "," + p.getEmail() + "," + p.getCategoria() + "," + p.isFavorito());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportarAJason(List<Persona> lista, String rutaArchivoJson) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(rutaArchivoJson)) {
            gson.toJson(lista, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Persona> importarDesdeJson(String rutaArchivoJson) {
        List<Persona> lista = new ArrayList<>();
        Gson gson = new Gson();

        try (Reader reader = new FileReader(rutaArchivoJson)) {
            lista = gson.fromJson(reader, new TypeToken<List<Persona>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public static List<Persona> importarContactosJSON(String ruta) {
        List<Persona> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            Gson gson = new Gson();
            lista = gson.fromJson(br, new TypeToken<List<Persona>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
