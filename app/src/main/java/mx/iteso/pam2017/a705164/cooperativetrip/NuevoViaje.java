package mx.iteso.pam2017.a705164.cooperativetrip;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_viaje);

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
        et.setHint("Click para cambiar");


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
                Toast.makeText(getApplicationContext(), "Paco", Toast.LENGTH_LONG).show();
            }
        });

        EditText et = (EditText) v.findViewById(R.id.et_punto);
        et.setHint("Click para cambiar");
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
        //Toast.makeText(getApplicationContext(), "Hola1", Toast.LENGTH_LONG).show();
        RestClient.get("viajes", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
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
