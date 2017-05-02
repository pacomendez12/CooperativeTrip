package mx.iteso.pam2017.a705164.cooperativetrip;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.MapFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener,
        NuevoViaje.OnFragmentInteractionListener,
        BuscarFragment.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        /*navigationView.removeHeaderView(findViewById(R.id.menu_header_linear_layout));
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);*/


        android.app.FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, new MainFragment(), "MAIN_ACTIVITY_FRAGMENT")
                .commit();


        // usuario

        TextView tv_nombre =  (TextView) headerView.findViewById(R.id.usuario_nombre);
        TextView tv_correo = (TextView) headerView.findViewById(R.id.correo);
        Usuario usuario = AuthenticationManager.getUsuario();
        tv_nombre.setText(usuario.nombre + " " + usuario.apellido);
        tv_correo.setText(usuario.correo);
        //getSupportActionBar().setTitle("paco");
    }

    public void setBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.app.FragmentManager fragmentManager = getFragmentManager();


        if (id == R.id.nav_mis_viajes) {
            Fragment currentFragment = (Fragment) fragmentManager.findFragmentById(R.layout.fragment_main);
            if (currentFragment == null || !currentFragment.isVisible()) {
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, new MainFragment(), "MAIN_ACTIVITY_FRAGMENT")
                        .commit();
            }
        } else if (id == R.id.nav_buscar_viajes) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new BuscarFragment(), "BUSCAR_FRAGMENT")
                    .commit();
        } else if (id == R.id.nav_nuevo_viaje) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, new NuevoViaje(), "NUEVO_VIAJE_FRAGMENT")
                    .commit();

        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            Bundle b = new Bundle();
            b.putBoolean("logout", true);
            intent.putExtras(b);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_ayuda) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // do nothing
    }
}
