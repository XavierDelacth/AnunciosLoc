package ao.co.isptec.aplm.projetoanuncioloc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class AdicionarGPS extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mapa;
    private FusedLocationProviderClient clienteLocalizacao;
    private static final int REQ_LOCALIZACAO = 100;

    private EditText etLatitude, etLongitude, etNomeLocal;
    private Button btnAdicionar, btnCancelar, btnGps, btnWifi;
    private ImageView btnFechar;
    private Switch switchMapear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gps);

        // === Inicializar Views ===
        etNomeLocal = findViewById(R.id.etNomeLocal);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        btnAdicionar = findViewById(R.id.btnAdicionar);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnFechar = findViewById(R.id.btnFechar);
        btnGps = findViewById(R.id.btnGps);
        btnWifi = findViewById(R.id.btnWifi);
        switchMapear = findViewById(R.id.switchMapear);

        clienteLocalizacao = LocationServices.getFusedLocationProviderClient(this);

        // === Configuração do Mapa ===
        SupportMapFragment mapaFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentoMapa);
        if (mapaFragment != null) {
            mapaFragment.getMapAsync(this);
        }

        // === Botões ===
        btnFechar.setOnClickListener(v -> finish());
        btnCancelar.setOnClickListener(v -> finish());

        btnWifi.setOnClickListener(v -> {
            startActivity(new Intent(this, AdicionarWIFI.class));
            finish();
        });

        btnGps.setOnClickListener(v -> {
            btnGps.setBackgroundTintList(getColorStateList(R.color.verde_principal));
            btnWifi.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        });

        btnAdicionar.setOnClickListener(v -> {
            String nome = etNomeLocal.getText().toString().trim();
            if (nome.isEmpty()) {
                etNomeLocal.setError("Digite o nome do local");
                return;
            }
            Toast.makeText(this, "Local salvo com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    // === MAPA ===
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.getUiSettings().setZoomControlsEnabled(true);
        mapa.getUiSettings().setMyLocationButtonEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacao();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCALIZACAO);
        }

        mapa.setOnMapClickListener(latLng -> {
            mapa.clear();
            mapa.addMarker(new MarkerOptions().position(latLng).title("Selecionado"));
            etLatitude.setText(String.format("%.6f", latLng.latitude));
            etLongitude.setText(String.format("%.6f", latLng.longitude));
        });
    }

    private void ativarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        mapa.setMyLocationEnabled(true);
        clienteLocalizacao.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng atual = new LatLng(location.getLatitude(), location.getLongitude());
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(atual, 15));
                etLatitude.setText(String.format("%.6f", atual.latitude));
                etLongitude.setText(String.format("%.6f", atual.longitude));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCALIZACAO &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacao();
        }
    }
}
