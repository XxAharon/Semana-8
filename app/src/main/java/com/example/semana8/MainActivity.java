package com.example.semana8;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.MapView;

import org.jspecify.annotations.NonNull;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private int cantidadPermisosDenegados = 0;
    private Button button;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean ubicacion = result.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false);
                boolean locacion = result.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (ubicacion && locacion) {
                    Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
                } else {
                    cantidadPermisosDenegados ++;
                    if (cantidadPermisosDenegados >= 2) {
                        DialogAlertConcederPermisosEnConfiguracion();
                    } else {
                        DialogAlertSolicitarPermisos();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.ButtonBuscarUbicacion);
        mapView = findViewById(R.id.map);
        VerificarPermisos();
        button.setOnClickListener(v -> {
            VerificarPermisos();
        });
    }

    private void VerificarPermisos() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
            } else {
                SolicitarPermisos();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error desconocido", Toast.LENGTH_SHORT).show();
        }
    }

    private void SolicitarPermisos() {
        if(cantidadPermisosDenegados >= 2){
            DialogAlertConcederPermisosEnConfiguracion();
        }else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void DialogAlertSolicitarPermisos() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Permisos Denegados")
                .setMessage("La ubicacion y locacion son necesarios para el funcionamineto de la app.")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    SolicitarPermisos();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void DialogAlertConcederPermisosEnConfiguracion() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Permisos no concedidos")
                .setMessage("Para utilizar funciones se redirigira a la configuracion para otorgar permisos.")
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