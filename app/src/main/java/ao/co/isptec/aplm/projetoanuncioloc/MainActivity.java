package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private CardView cardLocais, cardAnuncios;
    private TextView tabCriados, tabGuardados;
    private ImageView btnProfile, btnNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        setupTabs();
        selectTab(true);

        // Compatível com back gesture (Android 13+)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Se veio de Guardados → fica em Criados
                if (getIntent().getBooleanExtra("fromGuardados", false)) {
                    selectTab(true);
                    getIntent().removeExtra("fromGuardados");
                } else {
                    // Sai do app
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
    }

    private void setupClickListeners() {
        // Card Locais - Abre tela de gerir locais
        cardLocais.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarLocalActivity.class)));

        // Card Anúncios - Agora abre DIRETAMENTE a tela de criar novo anúncio
        cardAnuncios.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarAnunciosActivity.class)));

        // Perfil
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));

        // Notificações
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
}