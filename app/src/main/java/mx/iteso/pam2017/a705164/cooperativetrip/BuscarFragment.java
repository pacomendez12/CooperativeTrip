package mx.iteso.pam2017.a705164.cooperativetrip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BuscarFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BuscarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BuscarFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "BuscarFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private View view;
    private PlaceAutocompleteFragment autocompleteFragmentOrigen;
    private PlaceAutocompleteFragment autocompleteFragmentDestino;
    private EditText fechaET;
    private Button btnBuscar;
    private Place origen;
    private Place destino;

    LinearLayout mViajesView;
    LinearLayout mProgressView;

    private int anio;
    private int mes;
    private int dia;

    public BuscarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BuscarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BuscarFragment newInstance(String param1, String param2) {
        BuscarFragment fragment = new BuscarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return view = inflater.inflate(R.layout.fragment_buscar, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity)getActivity()).setBarTitle("Buscar viaje");

        mViajesView = (LinearLayout) view.findViewById(R.id.ll_buscar_view);
        mProgressView = (LinearLayout) view.findViewById(R.id.buscar_progress);

        autocompleteFragmentOrigen = (PlaceAutocompleteFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_origen_buscar);

        if (autocompleteFragmentOrigen == null) {
            autocompleteFragmentOrigen = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_origen_buscar);
        }

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
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destino_buscar);

        if (autocompleteFragmentDestino == null) {
            autocompleteFragmentDestino = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_destino_buscar);
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
                Toast.makeText(view.getContext(), getResources().getString(R.string.error_desconocido),
                        Toast.LENGTH_LONG).show();
            }
        });

        fechaET = (EditText) view.findViewById(R.id.et_buscar_fecha);
        fechaET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment newFragment = new DatePickerFragment();
                newFragment.setDateListener(BuscarFragment.this);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        btnBuscar = (Button) view.findViewById(R.id.btn_buscar_viaje);
        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarViaje();
            }
        });



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
        if (context instanceof OnFragmentInteractionListener) {
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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        anio = year;
        mes = month;
        dia = dayOfMonth;
        Date date = new Date(anio - 1900, mes, dia);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String reportDate = df.format(date);
        fechaET.setText(reportDate);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public void onDestroyView() {
        super.onDestroyView();

        Activity activity = getActivity();
        if (activity != null) {
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


    public void buscarViaje() {
        // Limpiar los viajes actuales
        LinearLayout contenedor = (LinearLayout)view.findViewById(R.id.ll_buscar_content);
        contenedor.removeAllViews();

        if (origen == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Elige un punto de origen", Toast.LENGTH_LONG).show();
            return;
        }

        if (destino == null) {
            Toast.makeText(getActivity().getApplicationContext(), "Elige un punto de destino", Toast.LENGTH_LONG).show();
            return;
        }

        String fecha = fechaET.getText().toString();
        if (fecha.equals("")) {
            Toast.makeText(getActivity().getApplicationContext(), "Elige una fecha para tu viaje", Toast.LENGTH_LONG).show();
            return;
        }

        fecha = fecha.replace('/','-');
        String lugarOrigen = origen.getName().toString();
        String lugarDestino = destino.getName().toString();

        String path = "viajes/buscar/" + lugarOrigen + "/" + lugarDestino + "/" + fecha;



        RestClient.get(path, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (response == null)
                    return;
                //Toast.makeText(getActivity().getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                try {
                    int estado = response.getInt("estado");
                    if (estado == 1) {
                        JSONArray viajes = response.getJSONArray("mensaje");
                        int len = viajes.length();
                        for(int i = 0; i < len; i++) {
                                agregarViajeView(viajes.getJSONObject(i), i);
                        }
                    } else {
                        // no hay viajes disponibles
                        Toast.makeText(getActivity().getApplicationContext(), "No hay viajes disponibles con los parámetros que especificaste", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                if (array != null)
                    Toast.makeText(getActivity().getApplicationContext(), "Hola3", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStart() {
                //Toast.makeText(getActivity().getApplicationContext(), "Hola4", Toast.LENGTH_LONG).show();
                // show progress bar
                showProgress(true);
            }

            @Override
            public void onFinish() {
                showProgress(false);
            }

            @Override
            public void onRetry(int retryNo) {
                //Toast.makeText(getActivity().getApplicationContext(), "Hola5", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, java.lang.Throwable throwable, JSONObject errorResponse) {
                if (errorResponse != null) {
                    Toast.makeText(getActivity().getApplicationContext(), errorResponse.toString(), Toast.LENGTH_LONG).show();
                    showNoInternet();
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

    /*public void agregarViajeView(JSONObject viaje, int i) {
        //final int id = idDinamico++;
        LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.layout_viaje, null);
        //v.setId(id);

        TextView nombre = (TextView) v.findViewById(R.id.tv_nombre_content);
        TextView origen = (TextView) v.findViewById(R.id.tv_origen_content);
        TextView destino = (TextView) v.findViewById(R.id.tv_destino_content);
        TextView fecha = (TextView) v.findViewById(R.id.tv_fecha_content);
        TextView hora = (TextView) v.findViewById(R.id.tv_hora_content);

        try {
            nombre.setText(viaje.getString("nombre"));
            origen.setText(viaje.getString("origen"));
            destino.setText(viaje.getString("destino"));
            fecha.setText(viaje.getString("fecha").replace('-', '/'));
            hora.setText(viaje.getString("hora"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), "Viaje presionado", Toast.LENGTH_LONG).show();
            }
        });

        // insert into main view
        ViewGroup insertPoint = (ViewGroup) view.findViewById(R.id.ll_buscar_content);
        insertPoint.addView(v, i, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    }*/


    public void agregarViajeView(JSONObject viajes, int i) throws JSONException {
        final JSONObject data = viajes.getJSONObject("datos");
        final JSONArray puntos_recoger;
        if (!viajes.isNull("puntos_recoger"))
            puntos_recoger = viajes.getJSONArray("puntos_recoger");
        else
            puntos_recoger = null;

        final JSONArray puntos_intermedios;
        if (!viajes.isNull("puntos_intermedios"))
            puntos_intermedios = viajes.getJSONArray("puntos_intermedios");
        else
            puntos_intermedios = null;
        final JSONObject usuario = viajes.getJSONObject("usuario");
        final JSONObject vehiculo = viajes.getJSONObject("vehiculo");


        if (data == null) return;
        //final int id = idDinamico++;
        LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.layout_viaje, null);
        //v.setId(id);

        TextView nombre = (TextView) v.findViewById(R.id.tv_nombre_content);
        TextView origen = (TextView) v.findViewById(R.id.tv_origen_content);
        TextView destino = (TextView) v.findViewById(R.id.tv_destino_content);
        TextView fecha = (TextView) v.findViewById(R.id.tv_fecha_content);
        TextView hora = (TextView) v.findViewById(R.id.tv_hora_content);

        try {
            nombre.setText(data.getString("nombre"));
            origen.setText(data.getString("origen"));
            destino.setText(data.getString("destino"));
            fecha.setText(data.getString("fecha").replace('-', '/'));
            hora.setText(data.getString("hora"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vista) {
                // Creando el objeto viaje
                Viaje v = null;
                try {
                    String [] arrayPuntosRecoger = new String[puntos_recoger != null? puntos_recoger.length() : 0];
                    for (int i = 0; i < arrayPuntosRecoger.length; i++) {
                        JSONObject obj = puntos_recoger.getJSONObject(i);
                        arrayPuntosRecoger[i] = obj.getString("lat") + "|" + obj.getString("lon");
                    }

                    String [] arrayPuntosIntermedios = new String[puntos_intermedios != null? puntos_intermedios.length() : 0];
                    for (int i = 0; i < arrayPuntosIntermedios.length; i++) {
                        JSONObject obj = puntos_intermedios.getJSONObject(i);
                        arrayPuntosIntermedios[i] = obj.getString("nombre") + "|" + obj.getString("lat") + "|" + obj.getString("lon");
                    }

                    String [] datosUsuario = new String[5];
                    datosUsuario[0] = usuario.getString("id_usuario");
                    datosUsuario[1] = usuario.getString("correo");
                    datosUsuario[2] = usuario.getString("telefono");
                    datosUsuario[3] = usuario.getString("nombre");
                    datosUsuario[4] = usuario.getString("apellido");

                    String vehiculoStr = vehiculo.getString("sub_marca") + " - " + vehiculo.getString("placa");
                    v = new Viaje(data.getString("nombre"), data.getString("origen"), data.getString("origen_lat"),
                            data.getString("origen_lon"), data.getString("destino"), data.getString("destino_lat"),
                            data.getString("destino_lon"), data.getString("fecha").replace('-', '/'),
                            data.getString("hora"), vehiculoStr, data.getDouble("precio"),
                            data.getInt("asientos"), data.getInt("asientos_libres"), data.getInt("id_viaje"),
                            arrayPuntosRecoger, arrayPuntosIntermedios, datosUsuario);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (v == null) return;

                //Toast.makeText(getActivity().getApplicationContext(), "Viaje presionado", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity().getApplicationContext(), MostrarViajeActivity.class);
                intent.putExtra("viajeTag", v);
                startActivity(intent);
            }
        });

        // insert into main view
        ViewGroup insertPoint = (ViewGroup) view.findViewById(R.id.ll_buscar_content);
        insertPoint.addView(v, i, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mViajesView.setVisibility(show ? View.GONE : View.VISIBLE);
            mViajesView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mViajesView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mViajesView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
