package mx.iteso.pam2017.a705164.cooperativetrip;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by paco on 29/04/2017.
 */

public class AuthenticationManager {
    private static String key;
    private static Usuario usuario;
    private static Activity logginActivity;

    public static String getKey() {
        //return "2143c0cb1fb4d3849400eade3bfc37re";
        return (isLogged()? key : "");
    }

    public static Usuario getUsuario() {
        return (isLogged()? usuario : null);
    }

    public static void setActivity(Activity activity) {
        logginActivity = activity;
    }

    public static void logOut() {
        if (isLogged()) {
            setKey("");
            setUsuario(null);
        }
    }

    public static boolean isLogged() {
        /*usuario = new Usuario();
        key = "paco";*/
        if (usuario == null || key.equals("")) {
            if (logginActivity != null) {
                SharedPreferences settings = logginActivity.getPreferences(0);
                key = settings.getString("key", "");
                if (!key.equals("")) {
                    usuario = new Usuario();
                    usuario.id = settings.getInt("id", 0);
                    usuario.correo = settings.getString("correo", "");
                    usuario.telefono = settings.getString("telefono", "");
                    usuario.nombreUsuario = settings.getString("nombre_usuario", "");
                    usuario.nombre = settings.getString("nombre", "");
                    usuario.apellido = settings.getString("apellido", "");
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public static void setKey(String key) {
        AuthenticationManager.key = key;
        if (logginActivity != null) {
            SharedPreferences settings = logginActivity.getPreferences(0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("key", key);
            editor.commit();
        }

    }

    public static void setUsuario(Usuario usuario) {
        AuthenticationManager.usuario = usuario;
        if (logginActivity != null) {
            SharedPreferences settings = logginActivity.getPreferences(0);
            SharedPreferences.Editor editor = settings.edit();
            if (usuario != null) {
                editor.putInt("id", usuario.id);
                editor.putString("correo", usuario.correo);
                editor.putString("telefono", usuario.telefono);
                editor.putString("nombre_usuario", usuario.nombreUsuario);
                editor.putString("nombre", usuario.nombre);
                editor.putString("apellido", usuario.apellido);
            } else {
                editor.putInt("id", 0);
                editor.putString("correo", "");
                editor.putString("telefono", "");
                editor.putString("nombre_usuario", "");
                editor.putString("nombre", "");
                editor.putString("apellido", "");
            }
            editor.commit();
        }
    }
}

class Usuario {
    public int id;
    public String correo;
    public String telefono;
    public String nombreUsuario;
    public String nombre;
    public String apellido;
}