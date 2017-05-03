package mx.iteso.pam2017.a705164.cooperativetrip;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by pacomendez on 5/3/17.
 */


public class Viaje implements Parcelable{
    public String nombre;
    public String origen;
    public String lat_origen;
    public String lon_origen;
    public String destino;
    public String lat_destino;
    public String lon_destino;
    public String fecha;
    public String hora;
    public String vehiculo;
    public double precio;
    public int asientos;
    public int asientosLibres;
    public int idViaje;
    public String [] escalas;
    public String [] puntosRecoger;
    public String [] datosUsuario;

    public Viaje(String nombre, String origen, String lat_origen, String lon_origen, String destino,
                 String lat_destino, String lon_destino, String fecha, String hora, String vehiculo,
                 double precio, int asientos, int asientosLibres, int idViaje, String [] escalas, String[] puntosRecoger,
                 String[] datosUsuario) {
        this.nombre = nombre;
        this.origen = origen;
        this.lat_origen = lat_origen;
        this.lon_origen = lon_origen;
        this.destino = destino;
        this.lat_destino = lat_destino;
        this.lon_destino = lon_destino;
        this.fecha = fecha;
        this.hora = hora;
        this.vehiculo = vehiculo;
        this.precio = precio;
        this.asientos = asientos;
        this.idViaje = idViaje;
        this.asientosLibres = asientosLibres;
        this.escalas = escalas;
        this.puntosRecoger = puntosRecoger;
        this.datosUsuario = datosUsuario;
    }

    public Viaje(Parcel in) {
        String [] data = new String[14];
        in.readStringArray(data);

        this.nombre = data[0];
        this.origen = data[1];
        this.lat_origen = data[2];
        this.lon_origen = data[3];
        this.destino = data[4];
        this.lat_destino = data[5];
        this.lon_destino = data[6];
        this.fecha = data[7];
        this.hora = data[8];
        this.vehiculo = data[9];
        this.precio = Double.parseDouble(data[10]);
        this.asientos = Integer.parseInt(data[11]);
        this.asientosLibres = Integer.parseInt(data[12]);
        this.idViaje = Integer.parseInt(data[13]);

        Object [] o = in.readArray(null);
        escalas = new String[o.length];
        for (int i = 0; i < escalas.length; i++) {
            escalas[i] = (String)o[i];
        }

        o = in.readArray(null);
        puntosRecoger = new String[o.length];
        for (int i = 0; i < puntosRecoger.length; i++) {
            puntosRecoger[i] = (String)o[i];
        }

        o = in.readArray(null);
        datosUsuario = new String[o.length];
        for (int i = 0; i < datosUsuario.length; i++) {
            datosUsuario[i] = (String)o[i];
        }
    }

    public static final Parcelable.Creator<Viaje> CREATOR = new Parcelable.Creator<Viaje>() {

        @Override
        public Viaje createFromParcel(Parcel source) {
            return new Viaje(source);
        }

        @Override
        public Viaje[] newArray(int size) {
            return new Viaje[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                this.nombre,
            this.origen,
                this.lat_origen,
                this.lon_origen,
            this.destino,
                this.lat_destino,
                this.lon_destino,
            this.fecha,
            this.hora,
            this.vehiculo,
            String.valueOf(this.precio),
            String.valueOf(this.asientos),
            String.valueOf(this.asientosLibres),
                String.valueOf(this.idViaje)
        });

        dest.writeArray(escalas);
        dest.writeArray(puntosRecoger);
        dest.writeArray(datosUsuario);
    }
}
