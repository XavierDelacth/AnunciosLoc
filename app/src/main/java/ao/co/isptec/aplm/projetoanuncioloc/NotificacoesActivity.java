package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ao.co.isptec.aplm.projetoanuncioloc.Adapters.NotificacaoAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Notificacao;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificacoesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificacaoAdapter adapter;
    private List<Notificacao> listaNotificacoes = new ArrayList<>();
    private Button btnClearNotifications;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacao);

        recyclerView = findViewById(R.id.recyclerNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnClearNotifications = findViewById(R.id.btnClearNotifications);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnClearNotifications.setOnClickListener(v -> limparNotificacoes());

        carregarNotificacoes();
    }

    private void carregarNotificacoes() {
        tvEmpty.setText("Carregando...");
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            Toast.makeText(this, "Erro: usuário não encontrado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RetrofitClient.getApiService(this).getNotificacoes(userId).enqueue(new Callback<List<Notificacao>>() {
            @Override
            public void onResponse(Call<List<Notificacao>> call, Response<List<Notificacao>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaNotificacoes = response.body();

                    adapter = new NotificacaoAdapter(NotificacoesActivity.this, listaNotificacoes);
                    recyclerView.setAdapter(adapter);

                    Toast.makeText(NotificacoesActivity.this,
                            "Você tem " + listaNotificacoes.size() + " notificação" +
                                    (listaNotificacoes.size() > 1 ? "s" : ""), Toast.LENGTH_LONG).show();

                    atualizarVisibilidade();
                } else {
                    tvEmpty.setText("Nenhuma notificação encontrada");
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Notificacao>> call, Throwable t) {
                tvEmpty.setText("Sem conexão");
                tvEmpty.setVisibility(View.VISIBLE);
                Toast.makeText(NotificacoesActivity.this, "Erro de rede", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void limparNotificacoes() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);

        RetrofitClient.getApiService(this).limparNotificacoes(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    listaNotificacoes.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(NotificacoesActivity.this, "Limpo!", Toast.LENGTH_SHORT).show();
                    atualizarVisibilidade();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(NotificacoesActivity.this, "Erro ao limpar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarVisibilidade() {
        if (listaNotificacoes.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Nenhuma notificação");
            btnClearNotifications.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            btnClearNotifications.setVisibility(View.VISIBLE);
        }
    }
}