package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdicionarKeyActivity extends AppCompatActivity {

    private CardView cardExistingKey, cardNewKey;
    private TextView btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chaves);

        cardExistingKey = findViewById(R.id.card_existing_key);
        cardNewKey = findViewById(R.id.card_new_key);
        btnClose = findViewById(R.id.btn_close);

        btnClose.setOnClickListener(v -> finish());

        cardExistingKey.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddValorChaveExistenteActivity.class);
            startActivityForResult(intent, 100);
        });

        cardNewKey.setOnClickListener(v -> {
            Intent intent = new Intent(this, CriarNovaChaveActivity.class);
            startActivityForResult(intent, 101);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish(); // volta para PerfilActivity e atualiza lista
        }
    }
}