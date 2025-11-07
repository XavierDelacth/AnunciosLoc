package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AdicionarLocalActivity extends AppCompatActivity {

    private EditText etSearch;
    private Button btnAddLocal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_local);  // teu layout com a lista

        btnAddLocal = findViewById(R.id.btnAddLocal);

        // CLIQUE NO BOTÃO ADICIONAR → abre o modal como Dialog
        btnAddLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdicionarLocalActivity.this, AdicionarGPS.class);
                startActivity(intent);

            }
        });
    }

    // Método para adicionar Local (pode abrir outra tela ou salvar)
    private void adicionarLocal() {
        String nomeLocal = etSearch.getText().toString().trim();

        if (nomeLocal.isEmpty()) {
            etSearch.setError("Digite o nome do local");
            etSearch.requestFocus();
            return;
        }

        // Aqui você pode salvar o local em banco ou enviar para outra Activity
        // Exemplo: abrir uma Activity de confirmação
        Intent intent = new Intent(AdicionarLocalActivity.this, ConfirmarLocalActivity.class);
        intent.putExtra("nomeLocal", nomeLocal);
        startActivity(intent);

        // Se quiser apenas limpar o campo
        // etSearch.setText("");
    }
}
