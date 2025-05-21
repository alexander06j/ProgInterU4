package controlador;

import idiomas.GestorIdioma;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import modelo.Persona;
import modelo.PersonaDAO;
import vista.VentanaPrincipal;

public class ControladorAgenda implements ActionListener {

    private VentanaPrincipal vista;
//    private Locale localeActual;
//    private ResourceBundle mensajes;
    private ActionListener listenerFiltroCategoria;

    //Constructor vacio
    public ControladorAgenda() {
    }

    //contructor
    public ControladorAgenda(VentanaPrincipal vista) {
        this.vista = vista;
        //asociar los botones al Listener
        this.vista.btnAgregar.addActionListener(this);
        this.vista.btnEditar.addActionListener(this);
        this.vista.btnEliminar.addActionListener(this);
        this.vista.btnExportar.addActionListener(e -> exportarContactosEnSegundoPlano());
        this.vista.btnImportarJSON.addActionListener(e -> importarContactosDesdeJSON());

        this.vista.cmbCategoriaFiltro.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filtrarPorCategoria();
            }
        });
        this.vista.txtBuscarContacto.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                buscarContactoPorNombre();
            }
        });
        //configura idioma
        this.vista.cmbIdioma.addActionListener(this);
        // Traducir la interfaz según idioma por defecto
        traducirInterfaz();
        //carga los contactos al iniciar
        cargarContactos();
        configurarAtajosTeclado();
        configurarMenuContextualTabla();

        listenerFiltroCategoria = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filtrarPorCategoria();
            }
        };
        vista.cmbCategoriaFiltro.addActionListener(listenerFiltroCategoria);
        agregarListenersBusqueda();//lama al metodo que agrega el DocumentListener

    }

    //Metodo traducir interfaz
    private void traducirInterfaz() {
        String idiomaSeleccionado = vista.cmbIdioma.getSelectedItem().toString();
        Locale locale;

        switch (idiomaSeleccionado) {
            case "Inglés":
                locale = new Locale("en", "US");
                break;
            case "Francés":
                locale = new Locale("fr", "FR");
                break;
            default:
                locale = new Locale("es", "ES");
                break;
        }

        // Establece el nuevo idioma ANTES de obtener el bundle
        GestorIdioma.setLocale(locale);

        // Ahora obtenemos el bundle actualizado
        ResourceBundle bundle = GestorIdioma.getBundle();

        // Traducción de etiquetas
        vista.lblNombre.setText(bundle.getString("VentanaPrincipal.lblNombre.text"));
        vista.lblTelefono.setText(bundle.getString("VentanaPrincipal.lblTelefono.text"));
        vista.lblCorreo.setText(bundle.getString("VentanaPrincipal.lblCorreo.text"));

        // Traducción de títulos de pestañas
        vista.jTabbedPane2.setTitleAt(0, bundle.getString("VentanaPrincipal.jPanel3.TabConstraints.tabTitle"));
        vista.jTabbedPane2.setTitleAt(1, bundle.getString("VentanaPrincipal.jPanel4.TabConstraints.tabTitle"));

        // Actualizar cmbCategoria
        vista.cmbCategoria.removeAllItems();
        vista.cmbCategoria.addItem(bundle.getString("VentanaPrincipal.cmbCategoria.item1"));
        vista.cmbCategoria.addItem(bundle.getString("VentanaPrincipal.cmbCategoria.item2"));
        vista.cmbCategoria.addItem(bundle.getString("VentanaPrincipal.cmbCategoria.item3"));

        // QUITAMOS el ActionListener para evitar que se dispare mientras traducimos
        vista.cmbCategoriaFiltro.removeActionListener(listenerFiltroCategoria);
        // Actualizar cmbCategoriaFiltro
        vista.cmbCategoriaFiltro.removeAllItems();
        vista.cmbCategoriaFiltro.addItem(bundle.getString("VentanaPrincipal.cmbCategoriaFiltro.item1"));
        vista.cmbCategoriaFiltro.addItem(bundle.getString("VentanaPrincipal.cmbCategoriaFiltro.item2"));
        vista.cmbCategoriaFiltro.addItem(bundle.getString("VentanaPrincipal.cmbCategoriaFiltro.item3"));
        vista.cmbCategoriaFiltro.addItem(bundle.getString("VentanaPrincipal.cmbCategoriaFiltro.item4"));
        //selecciona el primer elemento como predeterminado
        vista.cmbCategoriaFiltro.setSelectedIndex(0);
        // VOLVEMOS a agregar el ActionListener
        vista.cmbCategoriaFiltro.addActionListener(listenerFiltroCategoria);

        //actualizar tabla
        vista.tablaContactos.getColumnModel().getColumn(0).setHeaderValue(bundle.getString("tabla.column1"));
        vista.tablaContactos.getColumnModel().getColumn(1).setHeaderValue(bundle.getString("tabla.column2"));
        vista.tablaContactos.getColumnModel().getColumn(2).setHeaderValue(bundle.getString("tabla.column3"));
        vista.tablaContactos.getColumnModel().getColumn(3).setHeaderValue(bundle.getString("tabla.column4"));
        vista.tablaContactos.getColumnModel().getColumn(4).setHeaderValue(bundle.getString("tabla.column5"));
        // Esto fuerza a la tabla a refrescar los encabezados
        vista.tablaContactos.getTableHeader().repaint();

    }

    //Metodo cambiarIdioma
    private void cambiarIdioma() {
        traducirInterfaz();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == vista.btnAgregar) {
            agregarContacto();
            mostrarNotificacion("Contactos guardado con éxito", 3000);
        } else if (source == vista.btnEditar) {
            editarContacto();

        } else if (source == vista.btnEliminar) {
            eliminarContacto();
            mostrarNotificacion("Contacto eliminado", 3000);
        } else if (source == vista.btnExportar) {
            exportarContactos();
            mostrarNotificacion("Exportación completada", 3000);
        } else if (source == vista.cmbIdioma) {
            cambiarIdioma();
        }

    }
    //Metodos de la clase

    //Buscar los contactos en la TablaContacto por nombre
    private void buscarContactoPorNombre() {
        String texto = vista.txtBuscarContacto.getText().toLowerCase();
        try {
            PersonaDAO dao = new PersonaDAO();
            List<Persona> lista = dao.leerArchivo();

            DefaultTableModel modeloTabla = (DefaultTableModel) vista.tablaContactos.getModel();
            modeloTabla.setRowCount(0);

            for (Persona p : lista) {
                if (p.getNombre().toLowerCase().contains(texto)) {
                    modeloTabla.addRow(new Object[]{
                        p.getNombre(), p.getTelefono(), p.getEmail(), p.getCategoria(), p.isFavorito()
                    });
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al buscar contacto.");
        }
    }

    //filtra por categoria la tablaContactos
    private void filtrarPorCategoria() {
        Object itemSeleccionado = vista.cmbCategoriaFiltro.getSelectedItem();

        if (itemSeleccionado == null) {
            return; // Evitamos NullPointerException si aún no hay selección
        }

        String categoriaSeleccionada = itemSeleccionado.toString();

        // Obtenemos la traducción actual de "Todos"
        ResourceBundle bundle = GestorIdioma.getBundle();
        String textoTodos = bundle.getString("VentanaPrincipal.cmbCategoriaFiltro.item1");

        try {
            PersonaDAO dao = new PersonaDAO();
            List<Persona> lista = dao.leerArchivo();

            DefaultTableModel modeloTabla = (DefaultTableModel) vista.tablaContactos.getModel();
            modeloTabla.setRowCount(0);

            for (Persona p : lista) {
                // Si se selecciona "Todos" (en cualquier idioma), se muestra todo
                if (categoriaSeleccionada.equals(textoTodos) || p.getCategoria().equals(categoriaSeleccionada)) {
                    modeloTabla.addRow(new Object[]{
                        p.getNombre(), p.getTelefono(), p.getEmail(), p.getCategoria(), p.isFavorito()
                    });
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al filtrar contactos.");
        }
    }

    //cargar contactos al iniciar el programa
    private void agregarContacto() {
        String nombre = vista.txtNombre.getText();
        String telefono = vista.txtTelefono.getText();
        String correo = vista.txtCorreo.getText();
        String categoria = (String) vista.cmbCategoria.getSelectedItem();
        boolean favorito = vista.chbFavorito.isSelected();

        if (nombre.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios.");
            return;
        }

        //mostrar la barra de progreso
        vista.barraProgreso.setVisible(true);

        new Thread(() -> {
            PersonaDAO dao = new PersonaDAO();
            boolean existe = dao.existeContacto(nombre, telefono);

            if (existe) {
                SwingUtilities.invokeLater(() -> {
                    vista.barraProgreso.setVisible(false);
                    JOptionPane.showMessageDialog(vista, "El contactos ya está registrado");
                });
            } else {
                Persona nuevo = new Persona(nombre, telefono, correo, categoria, favorito);
                new PersonaDAO(nuevo).escribir();//Guardar
                SwingUtilities.invokeLater(() -> {
                    vista.barraProgreso.setVisible(false);
                    cargarContactos();//actualiza lista
                    limpiarCampos();//limpiar los JtextField
                    JOptionPane.showMessageDialog(vista, "Contactos agregado correctamente");
                });
            }
        }).start();
//        Persona persona = new Persona(nombre, telefono, correo, categoria, favorito);
//        PersonaDAO dao = new PersonaDAO(persona);
//        dao.escribir();
//
//        JOptionPane.showMessageDialog(null, "Contacto agregado con éxito.");
//        limpiarCampos();
//        cargarContactos();
    }

    //Edita los contactos cargados anteriormente
    private void editarContacto() {
        int filaSeleccionada = vista.tablaContactos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un contacto para editar.");
            return;
        }

        String nombre = vista.txtNombre.getText();
        String telefono = vista.txtTelefono.getText();
        String correo = vista.txtCorreo.getText();
        String categoria = (String) vista.cmbCategoria.getSelectedItem();
        boolean favorito = vista.chbFavorito.isSelected();

        if (nombre.isEmpty() || telefono.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nombre y teléfono son obligatorios.");
            return;
        }

        try {
            PersonaDAO dao = new PersonaDAO();
            List<Persona> lista = dao.leerArchivo();

            if (filaSeleccionada >= 0 && filaSeleccionada < lista.size()) {
                // Reemplazar el contacto con nuevos datos
                Persona nueva = new Persona(nombre, telefono, correo, categoria, favorito);
                lista.set(filaSeleccionada, nueva);
                dao.actualizarContactos(lista);
                JOptionPane.showMessageDialog(null, "Contacto editado correctamente.");
                limpiarCampos();
                cargarContactos();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al editar contacto.");
        }
    }

    //Elimina un contacto creado
    private void eliminarContacto() {
        int filaSeleccionada = vista.tablaContactos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un contacto para eliminar.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(null, "¿Estás seguro de eliminar este contacto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            PersonaDAO dao = new PersonaDAO();
            List<Persona> lista = dao.leerArchivo();

            if (filaSeleccionada >= 0 && filaSeleccionada < lista.size()) {
                lista.remove(filaSeleccionada);
                dao.actualizarContactos(lista);
                JOptionPane.showMessageDialog(null, "Contacto eliminado.");
                limpiarCampos();
                cargarContactos();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al eliminar contacto.");
        }
    }

    //exporta los contactos
    private void exportarContactos() {
        PersonaDAO dao = new PersonaDAO();
        List<Persona> lista;

        try {
            lista = dao.leerArchivo(); // ✅ Se inicializa la variable aquí

            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "No hay contactos para exportar.");
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Exportar contactos");

            FileNameExtensionFilter filtroCSV = new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv");
            FileNameExtensionFilter filtroJSON = new FileNameExtensionFilter("Archivos JSON (*.json)", "json");

            fileChooser.addChoosableFileFilter(filtroCSV);
            fileChooser.addChoosableFileFilter(filtroJSON);
            fileChooser.setFileFilter(filtroCSV); // Filtro predeterminado

            int seleccion = fileChooser.showSaveDialog(vista);

            if (seleccion == JFileChooser.APPROVE_OPTION) {
                File archivo = fileChooser.getSelectedFile();
                String ruta = archivo.getAbsolutePath();
                FileNameExtensionFilter filtro = (FileNameExtensionFilter) fileChooser.getFileFilter();

                if (filtro.getDescription().contains("JSON")) {
                    if (!ruta.toLowerCase().endsWith(".json")) {
                        ruta += ".json";
                    }
                    PersonaDAO.exportarContactosJSON(lista, ruta);
                } else {
                    if (!ruta.toLowerCase().endsWith(".csv")) {
                        ruta += ".csv";
                    }
                    PersonaDAO.exportarContactosCSV(lista, ruta);
                }

                JOptionPane.showMessageDialog(vista, "Contactos exportados correctamente a:\n" + ruta);
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(vista, "Error al exportar los contactos.");
        }

    }

    //limpia los campos
    private void limpiarCampos() {
        vista.txtNombre.setText("");
        vista.txtTelefono.setText("");
        vista.txtCorreo.setText("");
        vista.cmbCategoria.setSelectedIndex(0);
        vista.chbFavorito.setSelected(false);
    }

    public void cargarContactos() {
        // Mostrar la barra al 0%
        vista.barraProgreso.setVisible(true);
        vista.barraProgreso.setValue(0);

        // Crear un hilo para simular la carga progresiva
        new Thread(() -> {
            try {
                // Paso 1: Leer contactos
                PersonaDAO dao = new PersonaDAO();
                List<Persona> lista = dao.leerArchivo();

                // Paso 2: Simular avance
                for (int i = 0; i <= 100; i += 10) {
                    Thread.sleep(50); // Simula el tiempo de carga
                    final int progreso = i;
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        vista.barraProgreso.setValue(progreso);
                    });
                }

                // Paso 3: Cargar en la tabla
                DefaultTableModel modeloTabla = (DefaultTableModel) vista.tablaContactos.getModel();
                modeloTabla.setRowCount(0);
                for (Persona p : lista) {
                    modeloTabla.addRow(new Object[]{
                        p.getNombre(), p.getTelefono(), p.getEmail(), p.getCategoria(), p.isFavorito()
                    });
                }

                // Paso 4: Cargar en la lista
                DefaultListModel<String> modeloLista = new DefaultListModel<>();
                int inicio = Math.max(0, lista.size() - 5);
                for (int i = lista.size() - 1; i >= inicio; i--) {
                    modeloLista.addElement(lista.get(i).formatoLista());
                }
                vista.listContactos.setModel(modeloLista);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al cargar contactos.");
            } finally {
                // Ocultar barra al finalizar
                javax.swing.SwingUtilities.invokeLater(() -> {
                    vista.barraProgreso.setValue(100);
                    vista.barraProgreso.setVisible(false);
                });
            }
        }).start();

    }

    //Metodo atajos teclado
    private void configurarAtajosTeclado() {
        // Ctrl + N: Agregar contacto
        vista.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control N"), "nuevoContacto");
        vista.getRootPane().getActionMap()
                .put("nuevoContacto", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        agregarContacto();
                    }
                });

        // Ctrl + E: Exportar contactos
        vista.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control E"), "exportarContactos");
        vista.getRootPane().getActionMap()
                .put("exportarContactos", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        exportarContactos();
                    }
                });
    }

    //Atajo mause 
    // clic derecho sobre una fila en tablaContactos, que aparezca un menú con:
    //Editar o Eliminar
    private void configurarMenuContextualTabla() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem editarItem = new JMenuItem("Editar");
        editarItem.addActionListener(e -> editarContacto());

        JMenuItem eliminarItem = new JMenuItem("Eliminar");
        eliminarItem.addActionListener(e -> eliminarContacto());

        menu.add(editarItem);
        menu.add(eliminarItem);

        vista.tablaContactos.setComponentPopupMenu(menu);

        // Asegurar que se seleccione la fila donde se hace clic derecho
        vista.tablaContactos.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int fila = vista.tablaContactos.rowAtPoint(e.getPoint());
                if (fila != -1) {
                    vista.tablaContactos.setRowSelectionInterval(fila, fila);
                }
            }
        });
    }

    public void buscarContactoEnSegundoPlano(String textoBusqueda) {
        vista.barraProgreso.setVisible(true);

        SwingWorker<List<Persona>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Persona> doInBackground() {
                PersonaDAO dao = new PersonaDAO();
                return dao.buscarPorNombre(textoBusqueda);
            }

            @Override
            protected void done() {
                try {
                    List<Persona> resultados = get();
                    mostrarResultadosEnTabla(resultados);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    vista.barraProgreso.setVisible(false);
                }
            }
        };

        worker.execute();
    }

    private void mostrarResultadosEnTabla(List<Persona> lista) {
        DefaultTableModel modelo = (DefaultTableModel) vista.tablaContactos.getModel();
        modelo.setRowCount(0); // Limpiar tabla

        for (Persona p : lista) {
            modelo.addRow(new Object[]{
                p.getNombre(),
                p.getTelefono(),
                p.getEmail(),
                p.getCategoria(),
                p.isFavorito() ? "Sí" : "No"
            });
        }
    }

    private void agregarListenersBusqueda() {
        vista.txtBuscarContacto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                buscar();
            }

            public void removeUpdate(DocumentEvent e) {
                buscar();
            }

            public void changedUpdate(DocumentEvent e) {
                buscar();
            }

            private void buscar() {
                String texto = vista.txtBuscarContacto.getText().trim();
                buscarContactoEnSegundoPlano(texto); // Método implementado en Paso 2
            }
        });
    }

    private void exportarContactosEnSegundoPlano() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    PersonaDAO dao = new PersonaDAO();
                    List<Persona> lista = dao.leerArchivo(); // ✅ Inicializar la lista

                    if (lista.isEmpty()) {
                        JOptionPane.showMessageDialog(vista, "No hay contactos para exportar.");
                        return null;
                    }

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Exportar contactos");

                    FileNameExtensionFilter filtroCSV = new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv");
                    FileNameExtensionFilter filtroJSON = new FileNameExtensionFilter("Archivos JSON (*.json)", "json");

                    fileChooser.addChoosableFileFilter(filtroCSV);
                    fileChooser.addChoosableFileFilter(filtroJSON);
                    fileChooser.setFileFilter(filtroCSV); // Filtro predeterminado

                    int userSelection = fileChooser.showSaveDialog(vista);
                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File archivoSeleccionado = fileChooser.getSelectedFile();
                        String ruta = archivoSeleccionado.getAbsolutePath();

                        FileNameExtensionFilter filtroSeleccionado = (FileNameExtensionFilter) fileChooser.getFileFilter();

                        if (filtroSeleccionado.getDescription().contains("JSON")) {
                            if (!ruta.toLowerCase().endsWith(".json")) {
                                ruta += ".json";
                            }
                            PersonaDAO.exportarContactosJSON(lista, ruta);
                        } else {
                            if (!ruta.toLowerCase().endsWith(".csv")) {
                                ruta += ".csv";
                            }
                            PersonaDAO.exportarContactosCSV(lista, ruta);
                        }

                        JOptionPane.showMessageDialog(vista, "Contactos exportados correctamente a: " + ruta);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(vista, "Error al exportar contactos.");
                }

                return null;
            }
        };

        worker.execute(); // Ejecutar en segundo plano
    }

    private String getExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        return (dotIndex == -1) ? "" : name.substring(dotIndex + 1);
    }

    public void mostrarNotificacion(String mensaje, int duracionMilisegundos) {
        new Thread(() -> {
            SwingUtilities.invokeLater(() -> {
                vista.lblNotificacion.setText(mensaje);
            });

            try {
                Thread.sleep(duracionMilisegundos);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            SwingUtilities.invokeLater(() -> {
                vista.lblNotificacion.setText("");
            });
        }).start();
    }

    private void importarContactosDesdeJSON() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona un archivo JSON para importar");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos JSON", "json"));

        int seleccion = fileChooser.showOpenDialog(vista);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            List<Persona> nuevosContactos = PersonaDAO.importarContactosJSON(archivo.getAbsolutePath());

            if (!nuevosContactos.isEmpty()) {
                try {
                    PersonaDAO dao = new PersonaDAO();
                    List<Persona> contactosActuales = dao.leerArchivo();

                    // Crear un set para buscar más rápido
                    Set<String> clavesExistentes = new HashSet<>();
                    for (Persona p : contactosActuales) {
                        clavesExistentes.add(p.getNombre().toLowerCase() + "-" + p.getTelefono());
                    }

                    // Filtrar duplicados
                    List<Persona> contactosFiltrados = new ArrayList<>();
                    for (Persona nuevo : nuevosContactos) {
                        String clave = nuevo.getNombre().toLowerCase() + "-" + nuevo.getTelefono();
                        if (!clavesExistentes.contains(clave)) {
                            contactosFiltrados.add(nuevo);
                            clavesExistentes.add(clave); // Añadir al set para evitar repetidos en el mismo lote
                        }
                    }

                    if (!contactosFiltrados.isEmpty()) {
                        contactosActuales.addAll(contactosFiltrados);
                        dao.actualizarContactos(contactosActuales);
                        cargarContactos();
                        mostrarNotificacion("Importación completada sin duplicados", 3000);
                    } else {
                        JOptionPane.showMessageDialog(vista, "No se importaron contactos nuevos. Todos ya existen.");
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(vista, "Error al actualizar los contactos.");
                }
            } else {
                JOptionPane.showMessageDialog(vista, "El archivo JSON está vacío o malformado.");
            }
        }

    }

}
