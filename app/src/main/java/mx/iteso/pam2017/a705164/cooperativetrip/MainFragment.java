package mx.iteso.pam2017.a705164.cooperativetrip;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends android.app.Fragment {
    public static final String TAG = "MainFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;
    private Button btnNuevoViaje;


    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
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
        return view = inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnNuevoViaje = (Button)view.findViewById(R.id.btn_nuevo_viaje);
        btnNuevoViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, new NuevoViaje(), "NUEVO_VIAJE_FRAGMENT")
                        .commit();
            }
        });

        ((MainActivity)getActivity()).setBarTitle("Mis viajes");

        // obteniendo los viajes

        RestClient.get("viajes", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //Toast.makeText(getActivity().getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();

                try {
                    int estado = response.getInt("estado");
                    if (estado == 1) {
                        JSONArray viajes = response.getJSONArray("mensaje");
                        int len = viajes.length();
                        for(int i = 0; i < len; i++) {
                            //JSONObject viaje = viajes.getJSONObject(i).getJSONObject("datos");
                            //if (viaje != null) {
                                agregarViajeView(viajes.getJSONObject(i), i);
                            //}
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray array) {
                Toast.makeText(getActivity().getApplicationContext(), "Hola3", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getActivity().getApplicationContext(), errorResponse.toString(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No hay conexion con el servidor, revise tu conexion", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (responseString != null) {
                    Toast.makeText(getActivity().getApplicationContext(), responseString, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "No hay conexion con el servidor, revise tu conexion", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

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
        ViewGroup insertPoint = (ViewGroup) view.findViewById(R.id.ll_content);
        insertPoint.addView(v, i, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
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


}
