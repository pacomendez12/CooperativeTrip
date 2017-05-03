package mx.iteso.pam2017.a705164.cooperativetrip;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MostrarViajeActivity extends Activity {
    public static final String TAG = "MostrarViajeActivity";
    Button accion;
    boolean usuarioDuenio;
    Viaje viaje;
    RelativeLayout layoutChofer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_viaje);

        viaje = getIntent().getParcelableExtra("viajeTag");
        ((TextView)findViewById(R.id.tv_nombre_content)).setText(viaje.nombre);
        ((TextView)findViewById(R.id.tv_origen_content)).setText(viaje.origen);
        ((TextView)findViewById(R.id.tv_destino_content)).setText(viaje.destino);
        ((TextView)findViewById(R.id.tv_fecha_content)).setText(viaje.fecha.replace('-', '/'));
        ((TextView)findViewById(R.id.tv_hora_content)).setText(viaje.hora);
        ((TextView)findViewById(R.id.tv_vehiculo_content)).setText(viaje.vehiculo);
        ((TextView)findViewById(R.id.tv_precio_content)).setText(String.valueOf(viaje.precio));
        ((TextView)findViewById(R.id.tv_asientos_total_content)).setText(String.valueOf(viaje.asientos));
        ((TextView)findViewById(R.id.tv_asientos_total_libres_content)).setText(String.valueOf(viaje.asientosLibres));

        ListView lvEscalas = (ListView) findViewById(R.id.lv_escalas);
        ListView lvRecoger = (ListView) findViewById(R.id.lv_pick_up);

        ArrayList<String> escalas = new ArrayList<>();
        for (int i = 0; i < viaje.escalas.length; i++) {
            escalas.add(viaje.escalas[i].replace('|', ','));
        }

        ArrayList<String> recoger = new ArrayList<>();
        for (int i = 0; i < viaje.puntosRecoger.length; i++) {
            escalas.add(viaje.puntosRecoger[i].replace('|', ','));
        }

        ArrayAdapter<String> escalasAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, escalas);
        ArrayAdapter<String> recogerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, recoger);

        lvEscalas.setAdapter(escalasAdapter);
        lvRecoger.setAdapter(recogerAdapter);
        /*datosUsuario[0] = usuario.getString("id_usuario");
                    datosUsuario[1] = usuario.getString("correo");
                    datosUsuario[2] = usuario.getString("telefono");
                    datosUsuario[3] = usuario.getString("nombre");
                    datosUsuario[4] = usuario.getString("apellido");*/

        layoutChofer = (RelativeLayout) findViewById(R.id.rl_usuario_info);

        accion = (Button) findViewById(R.id.btn_accion);
        usuarioDuenio = true;

        usuarioDuenio = AuthenticationManager.getUsuario().id == Integer.parseInt(viaje.datosUsuario[0])? true : false;

        if (usuarioDuenio) {
            layoutChofer.setVisibility(View.GONE);
            findViewById(R.id.chofer_divider).setVisibility(View.GONE);
            accion.setText("Iniciar navegación");
            accion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url1  = MostrarViajeActivity.this.viaje.lat_destino + "," + MostrarViajeActivity.this.viaje.lon_destino;
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("google.navigation:mode=d&q=" + url1));
                    intent.setPackage("com.google.android.apps.maps");
                    startActivity(intent);
                }
            });
        } else {
            String nombreChofer = viaje.datosUsuario[3] + " " + viaje.datosUsuario[4];
            ((TextView)findViewById(R.id.tv_usuario_nombre_content)).setText(nombreChofer);
            ((TextView)findViewById(R.id.tv_usuario_telefono_content)).setText(viaje.datosUsuario[2]);

            accion.setText("Solicitar viaje");
            accion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject root = new JSONObject();
                    String path = "viajes/solicitar";

                    try {
                        root.put("id_viaje", viaje.idViaje);
                        root.put("id_usuario", AuthenticationManager.getUsuario().id);
                        root.put("asientos_requeridos", 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    RequestParams params = new RequestParams();
                    params.put("", root.toString());
                    params.setHttpEntityIsRepeatable(true);
                    params.setUseJsonStreamer(false);

                    RestClient.post(path, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Toast.makeText(getApplicationContext(), "Te has subscrito al viaje correctamente", Toast.LENGTH_LONG).show();
                            // Volver a la actividad anterior
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 1000);
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                            Toast.makeText(getApplicationContext(), array.toString(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onStart() {
                            //Toast.makeText(getActivity().getApplicationContext(), "Hola4", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onRetry(int retryNo) {

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, java.lang.Throwable throwable, JSONObject errorResponse) {
                            Toast.makeText(getApplicationContext(), "Error interno del servidor, por favor reportelo", Toast.LENGTH_LONG).show();
                            Log.e(TAG, errorResponse.toString());
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            Toast.makeText(getApplicationContext(), "Error interno del servidor, por favor reportelo", Toast.LENGTH_LONG).show();
                            Log.e(TAG, responseString);
                        }
                    });
                }
            });
        }

    }
}
