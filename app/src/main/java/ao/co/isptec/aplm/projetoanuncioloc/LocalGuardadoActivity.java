package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class LocalGuardadoActivity extends AppCompatActivity {

    private ImageView btnProfile, btnNotification, ivClear;
    private TextView tvLocation, tabCriados, tabGuardados;
    private CardView cardTabs, cardLocais, cardAnuncios;
    private EditText etSearch;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardado);

        initViews();
        setupClickListeners();
        setupTabs();
        setupSearch();
        selectTab(false);

        // Back gesture + botão voltar
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
        btnProfile = findViewById(R.id.btnProfile);
        btnNotification = findViewById(R.id.btnNotification);
        tvLocation = findViewById(R.id.tvLocation);
        tabCriados = findViewById(R.id.tabCriados);
        tabGuardados = findViewById(R.id.tabGuardados);
        cardTabs = findViewById(R.id.cardTabs);
        cardLocais = findViewById(R.id.cardLocais);
        cardAnuncios = findViewById(R.id.cardAnuncios);
        etSearch = findViewById(R.id.etSearch);
        ivClear = findViewById(R.id.ivClear);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupClickListeners() {
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, PerfilActivity.class)));
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificacoesActivity.class)));
        cardLocais.setOnClickListener(v -> startActivity(new Intent(this, AdicionarLocalActivity.class)));
        cardAnuncios.setOnClickListener(v -> startActivity(new Intent(this, AdicionarAnunciosActivity.class)));
        ivClear.setOnClickListener(v -> etSearch.setText(""));

        // Volta para "Criados"
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
            @Override public void afterTextChanged(Editable s) {
                ivClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });
    }
}