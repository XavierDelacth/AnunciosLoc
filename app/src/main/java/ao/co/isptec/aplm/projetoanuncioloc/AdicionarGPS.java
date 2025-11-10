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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class AdicionarGPS extends AppCompatActivity {

    private FusedLocationProviderClient clienteLocalizacao;
    private static final int REQ_LOCALIZACAO = 100;

    // Views
    private EditText etNomeLocal, etLatitude, etLongitude, etRaio;
    private Button btnAdicionar, btnCancelar, btnGps, btnWifi;
    private ImageView btnFechar;
    private Switch switchMapear;
    private TextView tvAvisoLocalizacao; // ← NOVO: o aviso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gps);

        // === Views ===
        etNomeLocal = findViewById(R.id.etNomeLocal);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etRaio = findViewById(R.id.etRaio);
        btnAdicionar = findViewById(R.id.btnAdicionar);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnFechar = findViewById(R.id.btnFechar);
        btnGps = findViewById(R.id.btnGps);
        btnWifi = findViewById(R.id.btnWifi);
        switchMapear = findViewById(R.id.switchMapear);
        tvAvisoLocalizacao = findViewById(R.id.tvAvisoLocalizacao); // ← NOVO

        clienteLocalizacao = LocationServices.getFusedLocationProviderClient(this);

        // === Bloquear edição dos campos de coordenadas ===
        etLatitude.setEnabled(false);
        etLongitude.setEnabled(false);
        etRaio.setEnabled(false);
        etRaio.setText("50"); // raio fixo ou o que quiseres

        // === Switch "Mapear localização" agora controla se usa GPS ou não ===
        switchMapear.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvAvisoLocalizacao.setText("As coordenadas são da sua localização atual");
                obterLocalizacaoAtual();
            } else {
                tvAvisoLocalizacao.setText("Localização desativada");
                etLatitude.setText("");
                etLongitude.setText("");
            }
            etRaio.setEnabled(isChecked);
        });

        // === Botões ===
        btnFechar.setOnClickListener(v -> finish());
        btnCancelar.setOnClickListener(v -> finish());

        btnWifi.setOnClickListener(v -> {
            startActivity(new Intent(this, AdicionarWIFI.class));
            finish();
        });

        btnGps.setOnClickListener(v -> {
            btnGps.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.verde_principal));
            btnWifi.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray));
        });

        btnAdicionar.setOnClickListener(v -> {
            String nome = etNomeLocal.getText().toString().trim();
            if (nome.isEmpty()) {
                etNomeLocal.setError("Digite o nome do local");
                return;
            }
            if (switchMapear.isChecked() && (etLatitude.getText().toString().isEmpty())) {
                Toast.makeText(this, "Aguarde a detecção da localização...", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Local GPS adicionado com sucesso!", Toast.LENGTH_LONG).show();
            finish();
        });

        // Back button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // Ativa GPS automaticamente ao abrir
        switchMapear.setChecked(true);
    }

    private void obterLocalizacaoAtual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCALIZACAO);
            return;
        }

        tvAvisoLocalizacao.setText("A detectar a sua localização...");

        clienteLocalizacao.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                etLatitude.setText(String.format("%.6f", lat));
                etLongitude.setText(String.format("%.6f", lng));

                tvAvisoLocalizacao.setText("Localização detectada com sucesso!");
            } else {
                tvAvisoLocalizacao.setText("Não foi possível obter localização. Tente novamente.");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCALIZACAO && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacaoAtual();
        } else {
            tvAvisoLocalizacao.setText("Permissão de localização negada");
            switchMapear.setChecked(false);
        }
    }
}