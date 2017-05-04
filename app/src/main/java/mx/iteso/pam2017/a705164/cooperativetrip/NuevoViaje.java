package mx.iteso.pam2017.a705164.cooperativetrip;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


public class NuevoViaje extends Fragment implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener, OnConnectionFailedListener  {
    final private String TAG =  "NuevoViaje";
    EditText horaET;
    EditText fechaET;
    EditText asientosET;

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

    private PlaceAutocompleteFragment autocompleteFragmentOrigen;
    private PlaceAutocompleteFragment autocompleteFragmentDestino;

    View myView;
    private OnFragmentInteractionListener mListener;

    public NuevoViaje(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainFragment.OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.activity_nuevo_viaje, container, false);
        return myView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setContentView(R.layout.activity_nuevo_viaje);
        restDataLoaded = false;

        ((MainActivity)getActivity()).setBarTitle("Nuevo viaje");

        autocompleteFragmentOrigen = (PlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_origen);

        if (autocompleteFragmentOrigen == null) {
            autocompleteFragmentOrigen = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_origen);
        }

        puntosIntermedios = new HashMap<>();
        puntosRecogida = new HashMap<>();

        autocompleteFragmentOrigen.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                origen = place;
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        autocompleteFragmentDestino = (PlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destino);

        if (autocompleteFragmentDestino == null) {
            autocompleteFragmentDestino = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destino);
        }

        autocompleteFragmentDestino.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                destino = place;
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
                Toast.makeText(myView.getContext(), getResources().getString(R.string.error_desconocido),
                        Toast.LENGTH_LONG).show();
            }
        });


        horaET = (EditText) myView.findViewById(R.id.et_hora);
        fechaET = (EditText) myView.findViewById(R.id.et_fecha);

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

        asientosET = (EditText) myView.findViewById(R.id.et_asientos);
        asientosET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumberPicker(v);
            }
        });

        final Button guardarViaje = (Button) myView.findViewById(R.id.btn_guardar);
        guardarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearViaje(v);
            }
        });

        Button agregarEscataBtn = (Button)myView.findViewById(R.id.btn_agregar_escala);
        agregarEscataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarEscala(v);
            }
        });

        Button agregarRecogerBtn = (Button) myView.findViewById(R.id.btn_agregar_recoger);
        agregarRecogerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarRecoger(v);
            }
        });

        if (!Util.isConnected(getActivity())) {
            showNoInternet();
        }
        // cargando los vehiculos desde el server
        loadData();
    }

    public void onDestroyView() {
        super.onDestroyView();
        Activity activity = getActivity();
        if(activity != null) {
            try {
                android.app.FragmentManager fm = activity.getFragmentManager();

                FragmentTransaction ft = fm.beginTransaction();
                if (!autocompleteFragmentOrigen.isDetached())
                    ft.remove(autocompleteFragmentOrigen);
                if (!autocompleteFragmentDestino.isDetached())
                    ft.remove(autocompleteFragmentDestino);
                ft.commit();
            } catch (IllegalStateException e) {}
        }
    }

    public void loadData() {
        cargarVehiculos();
        //restDataLoaded = true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(myView.getContext(), getResources().getString(R.string.error_no_internet), Toast.LENGTH_LONG).show();
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
        EditText asientos = (EditText) myView.findViewById(R.id.et_asientos);
        if (asientos.getText().equals(""))
            asientos.setText(Integer.toString(1));
        TextView as = (TextView) myView.findViewById(R.id.tv_asientos);
        as.setVisibility(View.VISIBLE);
        final AlertDialog.Builder d = new AlertDialog.Builder(this.getActivity());
        LayoutInflater inflater = this.getActivity().getLayoutInflater();
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
                EditText asientos = (EditText) myView.findViewById(R.id.et_asientos);
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
                GPSHelper gps = new GPSHelper(NuevoViaje.this.getActivity());
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

            Intent intent = intentBuilder.build(NuevoViaje.this.getActivity());
            startActivityForResult(intent, PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }



    public void agregarEscala(View vButton) {
        final int id = idDinamico++;
        LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.layout_nuevo_punto, null);
        v.setId(id);

        EditText et = (EditText) v.findViewById(R.id.et_punto);
        et.setHint("Click para elegir punto");


        ImageButton btn = (ImageButton) v.findViewById(R.id.btn_eliminar_punto);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewButton) {
                ViewGroup base = (ViewGroup) myView.findViewById(R.id.layout_escalas);
                View removed = myView.findViewById(id);
                base.removeView(removed);
                puntosIntermedios.remove(id);
            }
        });

        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity().getApplicationContext(), "Paco", Toast.LENGTH_LONG).show();
                lastEditTextUsed = (EditText)v;
                lasEditTextUsedIsEscala = true;
                lanzarPlacePicker();
            }
        });

        // insert into main view
        ViewGroup insertPoint = (ViewGroup) myView.findViewById(R.id.layout_escalas);
        insertPoint.addView(v, puntosIntermedios.size() + 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        puntosIntermedios.put(id, null);
    }



    public void agregarRecoger(View boton) {
        final int id = idDinamico++;
        LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.layout_nuevo_punto, null);
        v.setId(id);

        ImageButton btn = (ImageButton) v.findViewById(R.id.btn_eliminar_punto);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewButton) {
                ViewGroup base = (ViewGroup) myView.findViewById(R.id.layout_pick_up_points);
                View removed = base.findViewById(id);
                base.removeView(removed);
                puntosRecogida.remove(id);
                //Toast.makeText(getActivity().getApplicationContext(), "Paco", Toast.LENGTH_LONG).show();
            }
        });

        EditText et = (EditText) v.findViewById(R.id.et_punto);
        et.setHint("Click para elegir punto");
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastEditTextUsed = (EditText)v;
                lasEditTextUsedIsEscala = false;
                //Toast.makeText(getActivity().getApplicationContext(), "Paco", Toast.LENGTH_LONG).show();
                lanzarPlacePicker();
            }
        });

        // insert into main view
        ViewGroup insertPoint = (ViewGroup) myView.findViewById(R.id.layout_pick_up_points);
        insertPoint.addView(v, puntosRecogida.size() + 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        puntosRecogida.put(id, null);
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this.getActivity(), data);
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
            Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_no_rest_conection), Toast.LENGTH_LONG).show();
            loadData();
            return;
        }

        // Creando el JSON
        JSONObject root = new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray puntos_recoger = new JSONArray();
        JSONArray puntos_intermedios = new JSONArray();

        try {
            String nombre = ((EditText)myView.findViewById(R.id.nombre_viaje)).getText().toString();
            if (nombre.equals("")) {
                /*Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_no_nombre), Toast.LENGTH_LONG).show();
                findViewById(R.id.nombre_viaje).requestFocus();*/
                ((TextView)myView.findViewById(R.id.nombre_viaje)).setError(getString(R.string.error_field_required));
                return;
            }
            data.put("nombre", nombre);

            if (origen == null) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_no_origen), Toast.LENGTH_LONG).show();
                return;
            }
            data.put("origen_lat", origen.getLatLng().latitude);
            data.put("origen_lon", origen.getLatLng().longitude);
            data.put("origen", origen.getAddress());

            if (destino == null) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_no_destino), Toast.LENGTH_LONG).show();
                return;
            }
            data.put("destino_lat", destino.getLatLng().latitude);
            data.put("destino_lon", destino.getLatLng().longitude);
            data.put("destino", destino.getAddress());

            String fecha = ((EditText)myView.findViewById(R.id.et_fecha)).getText().toString().replace('/','-');
            if (!fecha.matches("^\\d{2}-\\d{2}-\\d{4}$")) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_no_fecha), Toast.LENGTH_LONG).show();
                return;
            }
            data.put("fecha", fecha);

            String hora = ((EditText)myView.findViewById(R.id.et_hora)).getText().toString();
            if (hora.equals("")) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_no_hora), Toast.LENGTH_LONG).show();
                return;
            }
            data.put("hora", hora);

            data.put("tiempo_estimado", null);
            data.put("tiempo_flexibilidad", null);

            String asientosString = ((EditText)myView.findViewById(R.id.et_asientos)).getText().toString();
            int asientos = 0;
            if (asientosString.equals("") || asientosString.equals("0")) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_no_asientos), Toast.LENGTH_LONG).show();
                return;
            } else {
                asientos = Integer.parseInt(asientosString);
            }
            data.put("asientos", asientos);

            String precioString = ((EditText)myView.findViewById(R.id.et_precio)).getText().toString();
            double precio = 0.0;
            if (!precioString.matches("^([+-]?(\\d+\\.)?\\d+)$")) {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.error_no_precio), Toast.LENGTH_LONG).show();
                return;
            } else {
                precio = Double.parseDouble(precioString);
            }
            data.put("precio", precio);


            data.put("id_carro", vehiculoSeleccionado != null? vehiculoSeleccionado.auto_id : null);

            // puntos recoger
            int i = 0;
            for (Map.Entry<Integer, Place> entry : puntosRecogida.entrySet()) {
                if (entry.getValue() != null) {
                    JSONObject p = new JSONObject();
                    p.put("lat", entry.getValue().getLatLng().latitude);
                    p.put("lon", entry.getValue().getLatLng().longitude);
                    puntos_recoger.put(i, p);
                }
                i++;
            }

            // puntos intermedios
            i = 0;
            for (Map.Entry<Integer, Place> entry : puntosIntermedios.entrySet()) {
                if (entry.getValue() != null) {
                    JSONObject p = new JSONObject();
                    p.put("lat", entry.getValue().getLatLng().latitude);
                    p.put("lon", entry.getValue().getLatLng().longitude);
                    p.put("nombre", entry.getValue().getName());
                    puntos_intermedios.put(i, p);
                }
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
                Toast.makeText(getActivity().getApplicationContext(), "Viaje creado con éxito", Toast.LENGTH_LONG).show();
                // Volver a la actividad anterior
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        android.app.FragmentManager fragmentManager =  getActivity().getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame, new MainFragment(), "MAIN_ACTIVITY_FRAGMENT")
                                .commit();
                    }
                }, 1000);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                Toast.makeText(getActivity().getApplicationContext(), array.toString(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(getActivity().getApplicationContext(), "Error interno del servidor, por favor reportelo", Toast.LENGTH_LONG).show();
                Log.e(TAG, errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getActivity().getApplicationContext(), "Error interno del servidor, por favor reportelo", Toast.LENGTH_LONG).show();
                Log.e(TAG, responseString);
            }
        });
    }


    public void cargarVehiculos() {
        RestClient.get("vehiculos", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (response == null)
                    return;
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


                    Spinner spinner = (Spinner) myView.findViewById(R.id.spinner_vehiculo);
                    ArrayAdapter adapter = new ArrayAdapter(NuevoViaje.this.getActivity(), android.R.layout.simple_spinner_dropdown_item, result);
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

                    //Toast.makeText(getActivity().getApplicationContext(), "restDataLoaded", Toast.LENGTH_LONG).show();
                    restDataLoaded = true;

                } catch (JSONException e) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Error intentando interpretar respuesta del servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                /*if (array != null)
                    Toast.makeText(getActivity().getApplicationContext(), "Hola3", Toast.LENGTH_LONG).show();*/
            }

            @Override
            public void onStart() {
                //Toast.makeText(getActivity().getApplicationContext(), "Hola4", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRetry(int retryNo) {
                //Toast.makeText(getActivity().getApplicationContext(), "Hola5", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, java.lang.Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    Toast.makeText(getActivity().getApplicationContext(), "No existe vehiculos registrados", Toast.LENGTH_LONG).show();
                    //showNoInternet();
                } else {
                    showNoInternet();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (responseString != null) {
                    Toast.makeText(getActivity().getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                    showNoInternet();
                } else {
                    showNoInternet();
                }
            }
        });

    }

    public void showNoInternet() {
        /*CoordinatorLayout layout = (CoordinatorLayout) myView.findViewById(R.id.coordinatorLayoutMain);
        Snackbar snackbar = Snackbar.make(layout, "No hay conexión a internet, es necesaria una conexión", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();*/
        Toast.makeText(getActivity().getApplicationContext(), "No hay conexión a internet, es necesaria una conexión", Toast.LENGTH_LONG).show();
    }
}
