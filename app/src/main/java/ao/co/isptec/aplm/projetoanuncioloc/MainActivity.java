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
    private ImageView btnProfile, btnNotification; // <-- adicionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar cards
        cardLocais = findViewById(R.id.cardLocais);
        cardAnuncios = findViewById(R.id.cardAnuncios);

        // Inicializar tabs
        tabCriados = findViewById(R.id.tabCriados);
        tabGuardados = findViewById(R.id.tabGuardados);

        // Inicializar perfil e notificações
        btnProfile = findViewById(R.id.btnProfile);
        btnNotification = findViewById(R.id.btnNotification);

        // Clique no card Locais -> abre tela AdicionarLocalActivity
        cardLocais.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdicionarLocalActivity.class);
            startActivity(intent);
        });

        // Clique no card Anúncios -> abre tela AdicionarAnunciosActivity
        cardAnuncios.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdicionarAnunciosActivity.class);
            startActivity(intent);
        });

        // Tabs
        tabCriados.setOnClickListener(v -> {
            tabCriados.setBackgroundColor(getResources().getColor(R.color.verde_principal));
            tabCriados.setTextColor(getResources().getColor(R.color.white));
            tabGuardados.setBackgroundColor(getResources().getColor(R.color.white));
            tabGuardados.setTextColor(getResources().getColor(R.color.verde_principal));
            // Atualizar RecyclerView se necessário
        });

        tabGuardados.setOnClickListener(v -> {
            tabGuardados.setBackgroundColor(getResources().getColor(R.color.verde_principal));
            tabGuardados.setTextColor(getResources().getColor(R.color.white));
            tabCriados.setBackgroundColor(getResources().getColor(R.color.white));
            tabCriados.setTextColor(getResources().getColor(R.color.verde_principal));
            // Atualizar RecyclerView se necessário
        });

        // Clique no botão Perfil -> abre PerfilActivity
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PerfilActivity.class);
            startActivity(intent);
        });

        // Clique no botão Notificações -> abre NotificacoesActivity
        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificacoesActivity.class);
            startActivity(intent);
        });
    }
}
