package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddicionarKeyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chaves);

        // Botão X (fechar)
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());



        // Card: Criar nova chave
        findViewById(R.id.card_new_key).setOnClickListener(v -> {
            // Abre tela de criar nova chave
            Intent intent = new Intent(this, CriarNovaChaveActivity.class);
            startActivityForResult(intent, 2);  // REQUEST_CODE = 2
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String novaChave = data.getStringExtra("nova_chave");
            String valor = data.getStringExtra("valor_adicionado");

            if (requestCode == 1 && valor != null) {
                Toast.makeText(this, "Valor adicionado: " + valor, Toast.LENGTH_SHORT).show();
            } else if (requestCode == 2 && novaChave != null) {
                Toast.makeText(this, "Chave criada: " + novaChave, Toast.LENGTH_SHORT).show();
            }

            // Retorna para AdicionarAnunciosActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("nova_chave", novaChave);
            resultIntent.putExtra("valor_adicionado", valor);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}
