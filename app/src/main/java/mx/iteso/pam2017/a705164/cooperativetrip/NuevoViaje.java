package mx.iteso.pam2017.a705164.cooperativetrip;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class NuevoViaje extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener, OnConnectionFailedListener  {
    final private String TAG =  "NuevoViaje";
    EditText horaET;
    EditText fechaET;

    int hora;
    int minuto;

    int anio;
    int mes;
    int dia;

    Place origen;
    Place destino;
    Map<Integer, Place> puntosRecogida;
    Map<Integer, Place> puntosIntermedios;

    private int idDinamico = 55102;

    private static final int PLACE_PICKER_REQUEST = 1;

    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    private Place lastPlace;
    private EditText lastEditTextUsed;
    boolean lasEditTextUsedIsEscala = true;
    private List<Vehiculo> vehiculos;
    private Vehiculo vehiculoSeleccionado;
    private boolean restDataLoaded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_viaje);
        restDataLoaded = false;

        PlaceAutocompleteFragment autocompleteFragmentOrigen = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_origen);

        puntosIntermedios = new HashMap<>();
        puntosRecogida = new HashMap<>();

        autocompleteFragmentOrigen.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                origen = place;

                /*String placeDetailsStr = place.getName() + "\n"
                        + place.getId() + "\n"
                        + place.getLatLng().toString() + "\n"
                        + place.getAddress() + "\n"
                        + place.getAttributions();
                Toast.makeText(getApplicationContext(), placeDetailsStr, Toast.LENGTH_LONG).show();*/
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        PlaceAutocompleteFragment autocompleteFragmentDestino = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destino);

        autocompleteFragmentDestino.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                destino = place;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_desconocido),
                        Toast.LENGTH_LONG).show();
            }
        });



        horaET = (EditText) findViewById(R.id.et_hora);
        fechaET = (EditText) findViewById(R.id.et_fecha);

        horaET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.setTimeListener(NuevoViaje.this);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

        fechaET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setDateListener(NuevoViaje.this);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        // cargando los vehiculos desde el server
        loadData();


    }

    public void loadData() {
        cargarVehiculos();
        //restDataLoaded = true;
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_no_internet), Toast.LENGTH_LONG).show();
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        hora = hourOfDay;
        minuto = minute;
        String min = minuto == 0 ? "00" : Integer.toString(minuto);
        horaET.setText(Integer.toString(hora) + ":" + min);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        anio = year;
        mes = month;
        dia = day;
        Date date = new Date(anio - 1900, mes, dia);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String reportDate = df.format(date);
        fechaET.setText(reportDate);
    }

    public void showNumberPicker(View v)
    {
        EditText asientos = (EditText) findViewById(R.id.et_asientos);
        if (asientos.getText().equals(""))
            asientos.setText(Integer.toString(1));
        TextView as = (TextView) findViewById(R.id.tv_asientos);
        as.setVisibility(View.VISIBLE);
        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.number_picker_dialog, null);
        d.setTitle("Asientos disponibles");
        //d.setMessage("Message");
        d.setView(dialogView);
        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.dialog_number_picker);
        numberPicker.setMaxValue(8);
        numberPicker.setMinValue(1);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                Log.d(TAG, "onValueChange: ");
                EditText asientos = (EditText) findViewById(R.id.et_asientos);
                asientos.setText(Integer.toString(i1));
            }
        });
        d.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                /*Log.d(TAG, "onClick: " + numberPicker.getValue());
                EditText asientos = (EditText) findViewById(R.id.et_asientos);
                asientos.setText(Integer.toString(i));*/
            }
        });

        AlertDialog alertDialog = d.create();
        alertDialog.show();
    }


    void lanzarPlacePicker() {
        try {
            PlacePicker.IntentBuilder intentBuilder =
                    new PlacePicker.IntentBuilder();
            if (lastPlace == null) {
                double radiusDegrees = 0.25;
                GPSHelper gps = new GPSHelper(NuevoViaje.this);
                gps.getMyLocation();
                LatLng center = new LatLng(gps.getLatitude(), gps.getLongitude());
                LatLng northEast = new LatLng(center.latitude + radiusDegrees, center.longitude + radiusDegrees);
                LatLng southWest = new LatLng(center.latitude - radiusDegrees, center.longitude - radiusDegrees);
                LatLngBounds bounds = LatLngBounds.builder()
                        .include(northEast)
                        .include(southWest)
                        .build();
                if (gps.getLongitude() == 0 && gps.getLongitude() == 0)
                    intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
                else
                    intentBuilder.setLatLngBounds(bounds);
            } else {
                // usar la ultima seleccion
                double radiusDegrees = 0.10;

                LatLng center = new LatLng(lastPlace.getLatLng().latitude, lastPlace.getLatLng().longitude);
                LatLng northEast = new LatLng(center.latitude + radiusDegrees, center.longitude + radiusDegrees);
                LatLng southWest = new LatLng(center.latitude - radiusDegrees, center.longitude - radiusDegrees);
                LatLngBounds bounds = LatLngBounds.builder()
                        .include(northEast)
                        .include(southWest)
                        .build();
                intentBuilder.setLatLngBounds(bounds);
            }

            Intent intent = intentBuilder.build(NuevoViaje.this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }



    public void agregarEscala(View vButton) {
        final int id = idDinamico++;
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.layout_nuevo_punto, null);
        v.setId(id);

        EditText et = (EditText) v.findViewById(R.id.et_punto);
        et.setHint("Click para elegir punto");


        ImageButton btn = (ImageButton) v.findViewById(R.id.btn_eliminar_punto);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewButton) {
                ViewGroup base = (ViewGroup) findViewById(R.id.layout_escalas);
                View removed = findViewById(id);
                base.removeView(removed);
                puntosIntermedios.remove(id);
            }
        });

        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Paco", Toast.LENGTH_LONG).show();
                lastEditTextUsed = (EditText)v;
                lasEditTextUsedIsEscala = true;
                lanzarPlacePicker();
            }
        });

        // insert into main view
        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.layout_escalas);
        insertPoint.addView(v, puntosIntermedios.size() + 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        puntosIntermedios.put(id, null);
    }



    public void agregarRecoger(View boton) {
        final int id = idDinamico++;
        LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.layout_nuevo_punto, null);
        v.setId(id);

        ImageButton btn = (ImageButton) v.findViewById(R.id.btn_eliminar_punto);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewButton) {
                ViewGroup base = (ViewGroup) findViewById(R.id.layout_pick_up_points);
                View removed = base.findViewById(id);
                base.removeView(removed);
                puntosRecogida.remove(id);
                //Toast.makeText(getApplicationContext(), "Paco", Toast.LENGTH_LONG).show();
            }
        });

        EditText et = (EditText) v.findViewById(R.id.et_punto);
        et.setHint("Click para elegir punto");
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastEditTextUsed = (EditText)v;
                lasEditTextUsedIsEscala = false;
                //Toast.makeText(getApplicationContext(), "Paco", Toast.LENGTH_LONG).show();
                lanzarPlacePicker();
            }
        });

        // insert into main view
        ViewGroup insertPoint = (ViewGroup) findViewById(R.id.layout_pick_up_points);
        insertPoint.addView(v, puntosRecogida.size() + 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        puntosRecogida.put(id, null);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);
            lastPlace = place;
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            String attributions = (String) place.getAttributions();
            if (attributions == null) {
                attributions = "";
            }

            /*mName.setText(name);
            mAddress.setText(address);
            mAttributions.setText(Html.fromHtml(attributions));*/
            //LinearLayout ll = (LinearLayout)((ViewGroup)lastEditTextUsed.getParent()).getParent();
            //int id1 = lastEditTextUsed.getId();
            int id2 = ((ViewGroup) lastEditTextUsed.getParent()).getId();
            //int id3 = ((LinearLayout) ((ViewGroup)lastEditTextUsed.getParent()).getParent()).getId();
            if (lasEditTextUsedIsEscala) {
                puntosIntermedios.put(id2, place);
            } else {
                puntosRecogida.put(id2, place);
            }

            lastEditTextUsed.setText(address);
            lastEditTextUsed = null;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void crearViaje(View v) {

        if (!restDataLoaded) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_no_rest_conection), Toast.LENGTH_LONG).show();
            loadData();
            return;
        }

        // Creando el JSON
        JSONObject root = new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray puntos_recoger = new JSONArray();
        JSONArray puntos_intermedios = new JSONArray();

        try {
            String nombre = ((EditText)findViewById(R.id.nombre_viaje)).getText().toString();
            if (nombre.equals("")) {
                /*Toast.makeText(getApplicationContext(), getString(R.string.error_no_nombre), Toast.LENGTH_LONG).show();
                findViewById(R.id.nombre_viaje).requestFocus();*/
                ((TextView)findViewById(R.id.nombre_viaje)).setError(getString(R.string.error_field_required));
                return;
            }
            data.put("nombre", nombre);

            if (origen == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_no_origen), Toast.LENGTH_LONG).show();
                return;
            }
            data.put("origen_lat", origen.getLatLng().latitude);
            data.put("origen_lon", origen.getLatLng().longitude);
            data.put("origen", origen.getAddress());

            if (destino == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_no_destino), Toast.LENGTH_LONG).show();
                return;
            }
            data.put("destino_lat", destino.getLatLng().latitude);
            data.put("destino_lon", destino.getLatLng().longitude);
            data.put("destino", destino.getAddress());

            String fecha = ((EditText)findViewById(R.id.et_fecha)).getText().toString().replace('/','-');
            if (!fecha.matches("^\\d{2}-\\d{2}-\\d{4}$")) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_no_fecha), Toast.LENGTH_LONG).show();
                return;
            }
            data.put("fecha", fecha);

            String hora = ((EditText)findViewById(R.id.et_hora)).getText().toString();
            if (hora.equals("")) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_no_hora), Toast.LENGTH_LONG).show();
                return;
            }
            data.put("hora", hora);

            data.put("tiempo_estimado", null);
            data.put("tiempo_flexibilidad", null);

            String asientosString = ((EditText)findViewById(R.id.et_asientos)).getText().toString();
            int asientos = 0;
            if (asientosString.equals("") || asientosString.equals("0")) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_no_asientos), Toast.LENGTH_LONG).show();
                return;
            } else {
                asientos = Integer.parseInt(asientosString);
            }
            data.put("asientos", asientos);

            String precioString = ((EditText)findViewById(R.id.et_precio)).getText().toString();
            double precio = 0.0;
            if (!precioString.matches("^([+-]?(\\d+\\.)?\\d+)$")) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_no_precio), Toast.LENGTH_LONG).show();
                return;
            } else {
                precio = Double.parseDouble(precioString);
            }
            data.put("precio", precio);


            data.put("id_carro", vehiculoSeleccionado != null? vehiculoSeleccionado.auto_id : null);

            // puntos recoger
            int i = 0;
            for (Map.Entry<Integer, Place> entry : puntosRecogida.entrySet()) {
                JSONObject p = new JSONObject();
                p.put("lat", entry.getValue().getLatLng().latitude);
                p.put("lon", entry.getValue().getLatLng().longitude);
                puntos_recoger.put(i, p);
                i++;
            }

            // puntos intermedios
            i = 0;
            for (Map.Entry<Integer, Place> entry : puntosIntermedios.entrySet()) {
                JSONObject p = new JSONObject();
                p.put("lat", entry.getValue().getLatLng().latitude);
                p.put("lon", entry.getValue().getLatLng().longitude);
                p.put("nombre", entry.getValue().getName());
                puntos_intermedios.put(i, p);
                i++;
            }

            root.put("datos", data);
            root.put("puntos_recoger", puntos_recoger);
            root.put("puntos_intermedios", puntos_intermedios);

            Log.e(TAG, root.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestParams params = new RequestParams();
        params.put("", root.toString());
        params.setHttpEntityIsRepeatable(true);
        params.setUseJsonStreamer(false);

        RestClient.post("viajes", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                // Volver a la actividad anterior
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                Toast.makeText(getApplicationContext(), array.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStart() {
                //Toast.makeText(getApplicationContext(), "Hola4", Toast.LENGTH_LONG).show();
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
/*
        RestClient.get("viajes", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                Toast.makeText(getApplicationContext(), "Hola3", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStart() {
                //Toast.makeText(getApplicationContext(), "Hola4", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRetry(int retryNo) {
                Toast.makeText(getApplicationContext(), "Hola5", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, java.lang.Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), errorResponse.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
            }
        });
*/

    }


    public void cargarVehiculos() {
        RestClient.get("vehiculos", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ArrayList<Vehiculo> result = new ArrayList<>();
                try {
                    JSONArray vehiculos = response.getJSONArray("vehiculos");
                    for (int i = 0; i < vehiculos.length(); i++) {
                        JSONObject vehiculo = vehiculos.getJSONObject(i);
                        Vehiculo v = new Vehiculo();
                        v.auto_id = vehiculo.getInt("auto_id");
                        v.marca = vehiculo.getString("marca");
                        v.sub_marca = vehiculo.getString("sub_marca");
                        v.modelo = vehiculo.getInt("modelo");
                        v.placa = vehiculo.getString("placa");
                        v.color = vehiculo.getString("color");
                        result.add(v);
                    }


                    Spinner spinner = (Spinner) findViewById(R.id.spinner_vehiculo);
                    ArrayAdapter adapter = new ArrayAdapter(NuevoViaje.this, android.R.layout.simple_spinner_dropdown_item, result);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            vehiculoSeleccionado = (Vehiculo) parent.getItemAtPosition(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            vehiculoSeleccionado = (Vehiculo) parent.getItemAtPosition(0);
                        }
                    });

                    //Toast.makeText(getApplicationContext(), "restDataLoaded", Toast.LENGTH_LONG).show();
                    restDataLoaded = true;

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Error intentando interpretar respuesta del servido", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                Toast.makeText(getApplicationContext(), "Hola3", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStart() {
                //Toast.makeText(getApplicationContext(), "Hola4", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRetry(int retryNo) {
                Toast.makeText(getApplicationContext(), "Hola5", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, java.lang.Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), errorResponse.toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
            }
        });

    }
}
