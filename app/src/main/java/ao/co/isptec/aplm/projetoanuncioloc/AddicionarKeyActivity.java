package ao.co.isptec.aplm.projetoanuncioloc;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AddicionarKeyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chaves);

        // Botão X (fechar)
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        // Card: Adicionar a chave existente
        findViewById(R.id.card_existing_key).setOnClickListener(v -> {
            // TODO: abrir tela de adicionar valor em chave existente
            // startActivity(new Intent(this, AddValueToExistingKeyActivity.class));
            finish();
        });

        // Card: Criar nova chave
        findViewById(R.id.card_new_key).setOnClickListener(v -> {
            // TODO: abrir tela de criar nova chave
            // startActivity(new Intent(this, CreateNewKeyActivity.class));
            finish();
        });

    }
}
