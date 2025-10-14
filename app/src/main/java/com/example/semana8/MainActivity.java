package com.example.semana8;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap googleMap;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private int cantidadPermisosDenegados = 0;
    private Button button;
    Bundle mapViewBundle = null;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private Location lastKnownLocation = null;
    private boolean mapaCargado = false;

    //Callback de permisos
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean ubicacionFina = Boolean.TRUE.equals(result.get(android.Manifest.permission.ACCESS_FINE_LOCATION));
                boolean ubicacionGruesa = Boolean.TRUE.equals(result.get(android.Manifest.permission.ACCESS_COARSE_LOCATION));

                if (ubicacionFina && ubicacionGruesa) {
                    Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
                    if (mapaCargado) {
                        CargarUbicacion();
                    }
                } else {
                    cantidadPermisosDenegados ++;
                    if (cantidadPermisosDenegados >= 2) {
                        DialogAlertConcederPermisosEnConfiguracion();
                    } else {
                        DialogAlertSolicitarPermisos();
                    }
                }
            });
    //Callback de permisos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.ButtonBuscarUbicacion);
        mapView = findViewById(R.id.map);

        VerificarPermisos();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        button.setOnClickListener(v -> {
            if(mapaCargado){
                CargarUbicacion();
            }else{
                Toast.makeText(this, "El mapa no ha cargado.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Mapa
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mapaCargado = true;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            CargarUbicacion();
        }
    }

    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onStop() { super.onStop(); mapView.onStop(); }
    @Override public void onPause() { mapView.onPause(); super.onPause(); }
    @Override public void onDestroy() { mapView.onDestroy(); super.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mapViewBundle == null) {
            mapViewBundle = new Bundle();
        }

        mapView.onSaveInstanceState(mapViewBundle);
        outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        super.onSaveInstanceState(outState);
    }
    //Mapa

    //Ubicacion

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if(locationResult == null) return;
            Location location = locationResult.getLastLocation();
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                MostrarUbicacionEnMapa(lat, lon);
            }
        }
    };

    private void MostrarUbicacionEnMapa(double lat, double lon) {
        // Almacena la última ubicación conocida
        if (lastKnownLocation == null) {
            lastKnownLocation = new Location("");
        }
        lastKnownLocation.setLatitude(lat);
        lastKnownLocation.setLongitude(lon);

        // Mueve la cámara y marca la ubicación
        ActualizarMapaConUbicacion();
    }

    private void ActualizarMapaConUbicacion() {
        if (googleMap != null && lastKnownLocation != null) {
            LatLng ubicacion = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(ubicacion).title("Tu ubicación actual"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 17));
        }
    }

    private void CargarUbicacion() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            try {
                googleMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                Toast.makeText(this, "Permisos insuficientes para MyLocationEnabled.", Toast.LENGTH_SHORT).show();
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    MostrarUbicacionEnMapa(location.getLatitude(), location.getLongitude());
                } else {
                    solicitarUnaActualizacionDeUbicacion();
                }
            });

        } else {
            SolicitarPermisos();
        }
    }

    private void solicitarUnaActualizacionDeUbicacion() {
        if (googleMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(1000)
                    .setNumUpdates(1);

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }
    //Ubicacion

    //Permisos
    private void VerificarPermisos() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                SolicitarPermisos();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error desconocido al verificar permisos", Toast.LENGTH_SHORT).show();
        }
    }

    private void SolicitarPermisos() {
        if(cantidadPermisosDenegados >= 2){
            DialogAlertConcederPermisosEnConfiguracion();
        }else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET
            });
        }
    }
    //Permisos

    //Dialogs
    private void DialogAlertSolicitarPermisos() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Permisos Denegados")
                .setMessage("La ubicación es necesaria para el funcionamiento de la app.")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    SolicitarPermisos();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void DialogAlertConcederPermisosEnConfiguracion() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Permisos no concedidos")
                .setMessage("Para utilizar funciones se le redirigirá a la configuración para otorgar permisos manualmente.")
                .setPositiveButton("Ir a ajustes", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
        Toast.makeText(this, "Permisos no concedidos", Toast.LENGTH_SHORT).show();
    }
}