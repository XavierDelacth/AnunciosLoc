package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class LocalGuardadoActivity extends AppCompatActivity {

    // Toolbar
    private ImageView btnProfile, btnNotification;

    // Localização
    private TextView tvLocation;

    // Tabs
    private TextView tabCriados, tabGuardados;
    private CardView cardTabs;

    // Pesquisa
    private SearchView searchView;

    // Lista
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardado); // Certifica-te que o XML se chama activity_local_guardado.xml

        initViews();
        setupClickListeners();
        setupTabs();
        setupSearch();
        setupRecyclerView();
    }

    private void initViews() {
        btnProfile = findViewById(R.id.btnProfile);
        btnNotification = findViewById(R.id.btnNotification);

        tvLocation = findViewById(R.id.tvLocation);

        tabCriados = findViewById(R.id.tabCriados);
        tabGuardados = findViewById(R.id.tabGuardados);
        cardTabs = findViewById(R.id.cardTabs);

        searchView = findViewById(R.id.searchView);

        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupClickListeners() {
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, PerfilActivity.class)));

        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificacoesActivity.class)));

        findViewById(R.id.cardLocais).setOnClickListener(v -> { /* abrir Locais */ });
        findViewById(R.id.cardAnuncios).setOnClickListener(v -> { /* abrir Anúncios */ });
    }

    private void setupTabs() {
        tabCriados.setOnClickListener(v -> selectTab(true));
        tabGuardados.setOnClickListener(v -> selectTab(false));
        selectTab(true);
    }

    private void selectTab(boolean isCriados) {
        if (isCriados) {
            tabCriados.setBackgroundColor(getColor(R.color.white));
            tabCriados.setTextColor(getColor(R.color.verde_principal));
            tabGuardados.setBackgroundColor(getColor(R.color.verde_principal));
            tabGuardados.setTextColor(getColor(R.color.white));
        } else {
            tabCriados.setBackgroundColor(getColor(R.color.verde_principal));
            tabCriados.setTextColor(getColor(R.color.white));
            tabGuardados.setBackgroundColor(getColor(R.color.white));
            tabGuardados.setTextColor(getColor(R.color.verde_principal));
        }
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { performSearch(query); return true; }
            @Override public boolean onQueryTextChange(String newText) { filterList(newText); return true; }
        });
    }

    private void performSearch(String query) { /* implementar */ }
    private void filterList(String text) { /* implementar */ }

    private void setupRecyclerView() {
        // recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // recyclerView.setAdapter(...);
    }
}