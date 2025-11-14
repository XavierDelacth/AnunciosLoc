package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
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
        setContentView(R.layout.activity_notificacao);  // Layout da tela de notificações

        // Inicializa views (IDs do teu XML)
        recyclerView = findViewById(R.id.recyclerNotifications);
        btnClearNotifications = findViewById(R.id.btnClearNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Inicializa a lista ANTES de adicionar itens
        listaAnuncios = new ArrayList<>();

        // DADOS DE TESTE (usando o novo construtor da Anuncio, com datas atuais: 12/11/2025)
        // Exemplo 1: Pizza (promoção rápida, local do PDF)
        Anuncio anuncio1 = new Anuncio(
                "Pizza 50% OFF",  // titulo
                "Largo da Independência - Só hoje! Venha experimentar a melhor pizza de Luanda com ingredientes frescos. Entrega grátis no local.",  // descricao
                "Largo da Independência",  // local
                "",  // imagem (vazio por agora; podes adicionar URI depois)
                "12/11/2025",  // dataInicio (hoje!)
                "12/11/2025",  // dataFim
                "18:00",  // horaInicio
                "23:00",  // horaFim
                "Whitelist",  // tipoRestricao (só para perfis permitidos)
                "Centralizado"  // modoEntrega (via 4G/WiFi)
        );
        anuncio1.addChave("Idade", Arrays.asList("18-24 anos", "25-35 anos"));  // Restrição: Jovens (F4 do PDF)
        listaAnuncios.add(anuncio1);

        // Exemplo 2: Terreno (imóvel, como no cenário de Alice no PDF)
        Anuncio anuncio2 = new Anuncio(
                "Terreno no Sun City",
                "500m² com vista mar. Ideal para construção residencial. Preço negociável: 150.000 Kz. Contato via app.",
                "Belas Shopping",
                "",  // imagem
                "13/11/2025",
                "30/11/2025",
                "09:00",
                "17:00",
                "Blacklist",  // tipoRestricao (exclui certos perfis)
                "Descentralizado"  // modoEntrega (via WiFi Direct para próximos)
        );
        anuncio2.addChave("Interesses", Arrays.asList("Imóveis", "Investimentos"));  // Restrição: Interessados em imóveis
        listaAnuncios.add(anuncio2);

        // Exemplo 3: iPhone (eletrônicos, descrição longa)
        Anuncio anuncio3 = new Anuncio(
                "iPhone 15 Pro 256GB",
                "Novo, lacrado, com nota fiscal. Cor: Titânio Preto. Inclui carregador original, cabo USB-C, fones AirPods Pro 2ª geração grátis. " +
                        "Aceito troca por iPhone 14 Pro Max + volta. Entrega em mão em Luanda ou envio via EMS com tracking.",
                "Ginásio do Camama I",  // local do PDF
                "content://media/external/images/media/123",  // imagem (URI mockado)
                "12/11/2025",
                "20/11/2025",
                "10:00",
                "20:00",
                "Whitelist",
                "Centralizado"
        );
        anuncio3.addChave("Gênero", Arrays.asList("Masculino", "Feminino"));  // Sem restrição de gênero
        anuncio3.addChave("Interesses", Arrays.asList("Tecnologia"));  // Só para fãs de tech
        listaAnuncios.add(anuncio3);

        // Exemplo 4: Evento Cultural (para testar interesses)
        Anuncio anuncio4 = new Anuncio(
                "Concerto de Semba Gratuito",
                "Noite de música ao vivo com artistas locais. Traga a família e desfrute de cultura angolana autêntica.",
                "Largo da Independência",
                "",
                "15/11/2025",
                "15/11/2025",
                "19:00",
                "22:00",
                "Whitelist",
                "Descentralizado"
        );
        anuncio4.addChave("Interesses", Arrays.asList("Música", "Arte"));
        listaAnuncios.add(anuncio4);

        // Exemplo 5: Oferta de Ginásio (local do PDF)
        Anuncio anuncio5 = new Anuncio(
                "Aula de Fitness Grátis",
                "Primeira aula experimental no nosso ginásio moderno. Equipamentos novos e instrutores certificados.",
                "Ginásio do Camama I",
                "",
                "14/11/2025",
                "14/11/2025",
                "07:00",
                "09:00",
                "Blacklist",
                "Centralizado"
        );
        anuncio5.addChave("Idade", Arrays.asList("36-45 anos", "46+ anos"));  // Exclui jovens (Blacklist implícito)
        listaAnuncios.add(anuncio5);

        // Configura o Adapter e RecyclerView
        adapter = new AnuncioAdapter(this, listaAnuncios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Inicializa visibilidade: Mostra lista se houver itens, senão "vazio"
        if (listaAnuncios.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            btnClearNotifications.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            btnClearNotifications.setVisibility(View.VISIBLE);
        }

        // BOTÃO VOLTAR (seta à esquerda - usa o teu btnClose do XML)
        findViewById(R.id.btnClose).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // LIMPAR NOTIFICAÇÕES (esvazia lista e atualiza UI)
        btnClearNotifications.setOnClickListener(v -> {
            listaAnuncios.clear();
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            btnClearNotifications.setVisibility(View.GONE);
            Toast.makeText(this, "Notificações limpas!", Toast.LENGTH_SHORT).show();  // ← ADICIONADO: Feedback
        });
    }
}