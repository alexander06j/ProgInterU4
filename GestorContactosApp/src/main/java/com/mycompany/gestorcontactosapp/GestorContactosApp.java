package com.mycompany.gestorcontactosapp;

import com.formdev.flatlaf.FlatLightLaf;
import controlador.ControladorAgenda;
import idiomas.GestorIdioma;
import java.util.Locale;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import vista.VentanaPrincipal;

public class GestorContactosApp {

    public static void main(String[] args) {

        try {
            //Aplica el tema claro de FlatLaf
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.out.println("No se puso aplicar FlatLaf.");
        }
        //Inicia interfaz
        javax.swing.SwingUtilities.invokeLater(() -> {
            //crea la vista
            VentanaPrincipal vista = new VentanaPrincipal();
            //muestra la ventana
            vista.setVisible(true);
            vista.setLocationRelativeTo(null);

            // 1. Establecer idioma por defecto (Español)
            GestorIdioma.setLocale(new Locale("es", "ES"));
            //crea el controlador
            ControladorAgenda controlador = new ControladorAgenda(vista);
            //carga contactos al iniciar
            controlador.cargarContactos();

        });

    }
}
