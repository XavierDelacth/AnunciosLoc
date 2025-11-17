package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import ao.co.isptec.aplm.projetoanuncioloc.Adapters.NotificacaoAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Notificacao;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificacoesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificacaoAdapter adapter;
    private List<Notificacao> listaNotificacoes;
    private Button btnClearNotifications;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacao);

        // Só precisamos de um TextView ou nada (o Toast já mostra)
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        if (tvEmpty != null) tvEmpty.setText("Carregando...");

        // CHAMA O GET COM TOAST
        carregarNotificacoesComToast();
    }

    private void carregarNotificacoesComToast() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);

        // LOG 1: Verifica se userId está correto
        Log.d("NOTIF_DEBUG", "userId do SharedPreferences: " + userId);

        if (userId == -1L) {
            Toast.makeText(this, "Erro: userId não encontrado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Call<List<Notificacao>> call = RetrofitClient.getApiService(this).getNotificacoes(userId);

        // LOG 2: URL exata que está sendo chamada
        Log.d("NOTIF_DEBUG", "URL chamada: " + call.request().url().toString());

        call.enqueue(new Callback<List<Notificacao>>() {
            @Override
            public void onResponse(Call<List<Notificacao>> call, Response<List<Notificacao>> response) {
                // LOG 3: Status e corpo
                Log.d("NOTIF_DEBUG", "Status: " + response.code());
                Log.d("NOTIF_DEBUG", "Body: " + (response.body() != null ? response.body().toString() : "null"));

                int count = response.body() != null ? response.body().size() : 0;
                String msg = count > 0
                        ? "Você tem " + count + " notificação" + (count > 1 ? "s" : "")
                        : "Nenhuma notificação (ou erro)";

                Toast.makeText(NotificacoesActivity.this, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<List<Notificacao>> call, Throwable t) {
                Log.e("NOTIF_DEBUG", "Erro: " + t.getMessage());
                Toast.makeText(NotificacoesActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void limparNotificacoes() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) return;

        Call<Void> call = RetrofitClient.getApiService(this).limparNotificacoes(userId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    listaNotificacoes.clear();
                    adapter.notifyDataSetChanged();
                    atualizarVisibilidade();
                    Toast.makeText(NotificacoesActivity.this, "Notificações limpas!", Toast.LENGTH_SHORT).show();
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
            btnClearNotifications.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            btnClearNotifications.setVisibility(View.VISIBLE);
        }
    }
}