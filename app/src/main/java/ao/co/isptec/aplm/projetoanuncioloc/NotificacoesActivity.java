package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.AnuncioAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;

public class NotificacoesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AnuncioAdapter adapter;
    private List<Anuncio> listaAnuncios;
    private Button btnClearNotifications;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacao);  // <--- TEU LAYOUT

        // IDs QUE JÁ EXISTEM NO TEU XML
        recyclerView = findViewById(R.id.recyclerNotifications);
        btnClearNotifications = findViewById(R.id.btnClearNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);

        // DADOS DE TESTE
        listaAnuncios = new ArrayList<>();
        listaAnuncios.add(new Anuncio("Pizza 50% OFF", "Largo da Independência - Só hoje!", false));
        listaAnuncios.add(new Anuncio("Terreno no Sun City", "500m² com vista mar", false));
        listaAnuncios.add(new Anuncio("iPhone 15 Pro", "Novo, lacrado", false));
        listaAnuncios.add(new Anuncio("Alugo T3 Talatona", "Mobilado, piscina", false));
        listaAnuncios.add(new Anuncio("Alugo T3 Talatona", "Mobilado, piscina", false));

        adapter = new AnuncioAdapter(this, listaAnuncios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // BOTÃO VOLTAR (seta à esquerda) - USA O TEU btnClose
        findViewById(R.id.btnClose).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // BOTÃO PERFIL (à direita)
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            startActivity(new Intent(this, PerfilActivity.class));
        });

        // LIMPAR NOTIFICAÇÕES
        btnClearNotifications.setOnClickListener(v -> {
            listaAnuncios.clear();
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            btnClearNotifications.setVisibility(View.GONE);
        });
    }
}