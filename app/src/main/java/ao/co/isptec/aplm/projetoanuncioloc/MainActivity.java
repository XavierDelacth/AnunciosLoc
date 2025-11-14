package ao.co.isptec.aplm.projetoanuncioloc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import ao.co.isptec.aplm.projetoanuncioloc.Adapters.MainAnuncioAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private CardView cardLocais, cardAnuncios;
    private TextView tabCriados, tabGuardados, tvLocation, tvEmptyAnuncios;
    private ImageView btnProfile, btnNotification;
    private RecyclerView rvAnunciosMain;
    private MainAnuncioAdapter adapter;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        setupTabs();
        selectTab(true);

        // Inicializa provedor de localização
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Tenta obter localização
        obterLocalizacaoAtual();

        setupListaAnuncios();

        // Compatível com back gesture (Android 13+)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getIntent().getBooleanExtra("fromGuardados", false)) {
                    selectTab(true);
                    getIntent().removeExtra("fromGuardados");
                } else {
                    finish();
                }
            }
        });
    }

    private void initViews() {
        cardLocais = findViewById(R.id.cardLocais);
        cardAnuncios = findViewById(R.id.cardAnuncios);
        tabCriados = findViewById(R.id.tabCriados);
        tabGuardados = findViewById(R.id.tabGuardados);
        btnProfile = findViewById(R.id.btnProfile);
        btnNotification = findViewById(R.id.btnNotification);
        tvLocation = findViewById(R.id.tvLocation);
        tvEmptyAnuncios = findViewById(R.id.tvEmptyAnuncios);
        rvAnunciosMain = findViewById(R.id.recyclerView);
    }

    private void setupClickListeners() {
        cardLocais.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarLocalActivity.class)));

        cardAnuncios.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarAnunciosActivity.class)));

        cardLocais.setOnClickListener(v -> startActivity(new Intent(this, AdicionarLocalActivity.class)));

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));

        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificacoesActivity.class)));
    }

    private void setupTabs() {
        tabCriados.setOnClickListener(v -> selectTab(true));

        tabGuardados.setOnClickListener(v -> {
            selectTab(false);
            Intent intent = new Intent(this, LocalGuardadoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
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

    private void obterLocalizacaoAtual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(this, location -> {
            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            1
                    );

                    if (addresses != null && !addresses.isEmpty()) {
                        String cidade = addresses.get(0).getLocality();
                        String pais = addresses.get(0).getCountryName();

                        String texto = "";
                        if (cidade != null) texto += " " + cidade;
                        if (pais != null) texto += ", " + pais;

                        tvLocation.setText(texto);
                    }

                } catch (IOException e) {
                    tvLocation.setText("Erro ao obter localização.");
                }
            } else {
                tvLocation.setText("Localização não disponível.");
            }
        });
    }

    private void setupListaAnuncios() {
        // Simula anúncios recebidos (baseado no PDF)
        Anuncio anuncio1 = new Anuncio(
                "Apartamento T2 para Arrendar - Vista Mar!",
                "Excelente T2 mobilado no coração da cidade, perto do Largo da Independência. 2 quartos, cozinha equipada. Contacte via app!",
                "Largo da Independência",
                null,
                "13/11/2025", "15/11/2025",
                "09:00", "18:00",
                "Whitelist",
                "Centralizado"
        );
        anuncio1.addChave("Idade", Arrays.asList("18-30", "30-50"));

        Anuncio anuncio2 = new Anuncio(
                "Ginásio Camama I - Aulas Grátis Hoje!",
                "Venha experimentar aulas de fitness no Ginásio do Camama I. Horário especial para novos membros.",
                "Ginásio do Camama I",
                null,
                "13/11/2025", "13/11/2025",
                "14:00", "20:00",
                "Nenhuma",
                "Descentralizado"
        );
        anuncio2.addChave("Gênero", Arrays.asList("Feminino", "Masculino"));
        anuncio2.addChave("Interesse", Arrays.asList("Fitness", "Yoga"));

        listaAnuncios.add(anuncio1);
        listaAnuncios.add(anuncio2);

        // Setup RecyclerView
        rvAnunciosMain.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAnuncioAdapter(this, listaAnuncios);
        rvAnunciosMain.setAdapter(adapter);

        // Atualiza visibilidade
        atualizarVisibilidade();
    }

    private void atualizarVisibilidade() {
        if (listaAnuncios.isEmpty()) {
            rvAnunciosMain.setVisibility(View.GONE);
            tvEmptyAnuncios.setVisibility(View.VISIBLE);
            tvEmptyAnuncios.setText("Nenhum anúncio");
        } else {
            rvAnunciosMain.setVisibility(View.VISIBLE);
            tvEmptyAnuncios.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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