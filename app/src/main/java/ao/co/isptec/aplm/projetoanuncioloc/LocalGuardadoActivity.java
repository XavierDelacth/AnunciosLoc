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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.AnuncioAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;

public class LocalGuardadoActivity extends AppCompatActivity {

    private CardView cardLocais, cardAnuncios, cardTabs;
    private TextView tabCriados, tabGuardados, tvLocation, tvEmptyGuardados;
    private ImageView ivClear, btnProfile, btnNotification;
    private EditText etSearch;
    private RecyclerView recyclerView;
    private AnuncioAdapter adapter;
    private List<Anuncio> listaAnunciosGuardados;

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

        // Inicializa lista de anúncios guardados
        setupListaAnunciosGuardados();

        // Localização atual
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        obterLocalizacaoAtual();

        // Botão voltar / Back Gesture
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
        tvEmptyGuardados = findViewById(R.id.tvEmptyGuardados);

        etSearch = findViewById(R.id.etSearch);
        ivClear = findViewById(R.id.ivClear);
        recyclerView = findViewById(R.id.recyclerView);

        btnProfile = findViewById(R.id.btnProfile);
        btnNotification = findViewById(R.id.btnNotification);
    }

    private void setupClickListeners() {
        cardLocais.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarLocalActivity.class)));

        cardAnuncios.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarAnunciosActivity.class)));

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));

        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificacoesActivity.class)));

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
                filtrarAnuncios(s.toString());
            }
        });
    }

    private void setupListaAnunciosGuardados() {
        listaAnunciosGuardados = new ArrayList<>();

        // ANÚNCIOS SIMULADOS (todos com salvo = true)

        // Anúncio 1: Pizza
        Anuncio anuncio1 = new Anuncio(
                "Pizza 50% OFF",
                "Largo da Independência - Só hoje! Venha experimentar a melhor pizza de Luanda com ingredientes frescos. Entrega grátis no local.",
                true, // SALVO
                "Largo da Independência",
                "",
                "12/11/2025",
                "12/11/2025",
                "18:00",
                "23:00",
                "Whitelist",
                "Centralizado"
        );
        anuncio1.addChave("Idade", Arrays.asList("18-24", "25-34"));
        listaAnunciosGuardados.add(anuncio1);

        // Anúncio 2: Terreno
        Anuncio anuncio2 = new Anuncio(
                "Terreno no Sun City",
                "500m² com vista mar. Ideal para construção residencial. Preço negociável: 150.000 Kz.",
                true, // SALVO
                "Belas Shopping",
                "",
                "13/11/2025",
                "30/11/2025",
                "09:00",
                "17:00",
                "Blacklist",
                "Descentralizado"
        );
        anuncio2.addChave("Interesses", Arrays.asList("Imóveis", "Investimentos"));
        listaAnunciosGuardados.add(anuncio2);

        // Anúncio 3: iPhone
        Anuncio anuncio3 = new Anuncio(
                "iPhone 15 Pro 256GB",
                "Novo, lacrado, com nota fiscal. Cor: Titânio Preto. Inclui carregador original, cabo USB-C, fones AirPods Pro 2ª geração grátis.",
                true, // SALVO
                "Ginásio do Camama I",
                "",
                "12/11/2025",
                "20/11/2025",
                "10:00",
                "20:00",
                "Whitelist",
                "Centralizado"
        );
        anuncio3.addChave("Gênero", Arrays.asList("Masculino", "Feminino"));
        anuncio3.addChave("Interesses", Arrays.asList("Tecnologia"));
        listaAnunciosGuardados.add(anuncio3);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnuncioAdapter(this, listaAnunciosGuardados);
        recyclerView.setAdapter(adapter);

        // Atualiza visibilidade
        atualizarVisibilidade();
    }

    private void filtrarAnuncios(String query) {
        if (adapter == null) return;

        List<Anuncio> listaFiltrada = new ArrayList<>();

        if (query.isEmpty()) {
            listaFiltrada.addAll(listaAnunciosGuardados);
        } else {
            String queryLower = query.toLowerCase();
            for (Anuncio anuncio : listaAnunciosGuardados) {
                if (anuncio.titulo.toLowerCase().contains(queryLower) ||
                        anuncio.descricao.toLowerCase().contains(queryLower) ||
                        anuncio.local.toLowerCase().contains(queryLower)) {
                    listaFiltrada.add(anuncio);
                }
            }
        }

        // Atualiza adapter com lista filtrada
        adapter = new AnuncioAdapter(this, listaFiltrada);
        recyclerView.setAdapter(adapter);

        // Atualiza visibilidade
        if (listaFiltrada.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyGuardados.setVisibility(View.VISIBLE);
            if (!query.isEmpty()) {
                tvEmptyGuardados.setText("Nenhum anúncio encontrado");
            } else {
                tvEmptyGuardados.setText("Nenhum anúncio guardado");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyGuardados.setVisibility(View.GONE);
        }
    }

    private void atualizarVisibilidade() {
        if (listaAnunciosGuardados.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyGuardados.setVisibility(View.VISIBLE);
            tvEmptyGuardados.setText("Nenhum anúncio guardado");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyGuardados.setVisibility(View.GONE);
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
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
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