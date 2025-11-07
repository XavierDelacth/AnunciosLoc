package ao.co.isptec.aplm.projetoanuncioloc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocalGuardadoActivity extends AppCompatActivity {

    private CardView cardLocais, cardAnuncios, cardTabs;
    private TextView tabCriados, tabGuardados, tvLocation;
    private ImageView ivClear;
    private EditText etSearch;
    private RecyclerView recyclerView;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardado);

        initViews();
        setupClickListeners();
        setupTabs();
        setupSearch();
        selectTab(false); // Começa na aba "Guardados"

        // ========================
        // 📍 LOCALIZAÇÃO ATUAL
        // ========================
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        obterLocalizacaoAtual();

        // ========================
        // Botão voltar / Back Gesture
        // ========================
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(LocalGuardadoActivity.this, MainActivity.class);
                intent.putExtra("fromGuardados", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initViews() {
        cardLocais = findViewById(R.id.cardLocais);
        cardAnuncios = findViewById(R.id.cardAnuncios);
        cardTabs = findViewById(R.id.cardTabs);

        tabCriados = findViewById(R.id.tabCriados);
        tabGuardados = findViewById(R.id.tabGuardados);

        tvLocation = findViewById(R.id.tvLocation);

        etSearch = findViewById(R.id.etSearch);
        ivClear = findViewById(R.id.ivClear);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupClickListeners() {
        cardLocais.setOnClickListener(v -> startActivity(new Intent(this, AdicionarLocalActivity.class)));
        cardAnuncios.setOnClickListener(v -> startActivity(new Intent(this, AdicionarAnunciosActivity.class)));

        ivClear.setOnClickListener(v -> etSearch.setText(""));

        // Tab "Criados" volta para MainActivity mantendo estado
        tabCriados.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fromGuardados", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void setupTabs() {
        tabGuardados.setOnClickListener(v -> selectTab(false));
    }

    private void selectTab(boolean isCriados) {
        if (isCriados) {
            tabCriados.setBackgroundColor(getColor(R.color.verde_principal));
            tabCriados.setTextColor(getColor(R.color.white));
            tabGuardados.setBackgroundColor(getColor(R.color.white));
            tabGuardados.setTextColor(getColor(R.color.verde_principal));
        } else {
            tabCriados.setBackgroundColor(getColor(R.color.white));
            tabCriados.setTextColor(getColor(R.color.verde_principal));
            tabGuardados.setBackgroundColor(getColor(R.color.verde_principal));
            tabGuardados.setTextColor(getColor(R.color.white));
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                ivClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                // TODO: filtrar RecyclerView aqui
            }
        });
    }

    // ============================
    // 📍 LOCALIZAÇÃO ATUAL
    // ============================
    private void obterLocalizacaoAtual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(LocalGuardadoActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(),
                                location.getLongitude(),
                                1);

                        if (addresses != null && !addresses.isEmpty()) {
                            String cidade = addresses.get(0).getLocality();
                            String pais = addresses.get(0).getCountryName();

                            String texto = "";
                            if (cidade != null) texto += " " + cidade;
                            if (pais != null) texto += ", " + pais;

                            tvLocation.setText(texto.trim());
                        }
                    } catch (IOException e) {
                        tvLocation.setText("Erro ao obter localização.");
                    }
                } else {
                    tvLocation.setText("Localização não disponível.");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obterLocalizacaoAtual();
            } else {
                tvLocation.setText("Permissão de localização negada.");
            }
        }
    }
}