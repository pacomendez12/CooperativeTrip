package mx.iteso.pam2017.a705164.cooperativetrip;

/**
 * Created by pacomendez on 4/30/17.
 */

public class Vehiculo {
    public int auto_id;
    public String marca;
    public String sub_marca;
    public int modelo;
    public String placa;
    public String color;

    @Override
    public String toString() {
        return sub_marca + " - "+ placa;
    }
}
