package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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

        // Inicializar views
        cardLocais = findViewById(R.id.cardLocais);
        cardAnuncios = findViewById(R.id.cardAnuncios);
        tabCriados = findViewById(R.id.tabCriados);
        tabGuardados = findViewById(R.id.tabGuardados);
        btnProfile = findViewById(R.id.btnProfile);
        btnNotification = findViewById(R.id.btnNotification);

        // Clique nos cards
        cardLocais.setOnClickListener(v -> startActivity(new Intent(this, AdicionarLocalActivity.class)));
        cardAnuncios.setOnClickListener(v -> startActivity(new Intent(this, AdicionarAnunciosActivity.class)));

        // Clique no Perfil
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, PerfilActivity.class)));

        // Clique nas Notificações
        btnNotification.setOnClickListener(v -> startActivity(new Intent(this, NotificacoesActivity.class)));

        // === CLIQUE NAS TABS ===
        tabCriados.setOnClickListener(v -> {
            selectTab(true);
            // Aqui podes carregar anúncios criados
        });

        tabGuardados.setOnClickListener(v -> {
            selectTab(false);
            // Abre a tela LocalGuardadoActivity
            startActivity(new Intent(MainActivity.this, LocalGuardadoActivity.class));
        });

        // Inicia com "Criados" selecionado
        selectTab(true);
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