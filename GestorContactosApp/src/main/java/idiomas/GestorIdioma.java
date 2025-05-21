package idiomas;
import java.util.Locale;
import java.util.ResourceBundle;
public class GestorIdioma {
    //idioma por defecto al iniciar
    private static Locale locale = new Locale("es", "ES");
    
    //carga el bundle desde src/main/resources/idiomas
    private static ResourceBundle bundle = ResourceBundle.getBundle("idiomas.Bundle", locale);
    
    //cambia el idioma en tiempo de ejecucion
    public static void setLocale(Locale nuevaLocale) {
        locale = nuevaLocale;
        bundle = ResourceBundle.getBundle("idiomas.Bundle", locale);
    }
    //obtiene el bundle actual
    public static ResourceBundle getBundle() {
        return bundle;
    }
}
