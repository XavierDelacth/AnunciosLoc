package ao.co.isptec.aplm.projetoanuncioloc;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;

public class CriarNovaChaveActivity extends AppCompatActivity {

    private EditText etNomeChave, etValores;
    private Button btnCriar;
    private TextView btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_key);

        btnCriar = findViewById(R.id.btn_add_key);
        btnClose = findViewById(R.id.btn_close);

        btnClose.setOnClickListener(v -> finish());

        btnCriar.setOnClickListener(v -> {
            String nome = etNomeChave.getText().toString().trim();
            String valoresStr = etValores.getText().toString().trim();

            if (nome.isEmpty() || valoresStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> valores = new ArrayList<>(Arrays.asList(valoresStr.split(",")));
            for (int i = 0; i < valores.size(); i++) {
                valores.set(i, valores.get(i).trim());
            }

            // Criar nova chave
            ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey nova = new ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey(nome);
            nova.setAvailableValues(valores);
            nova.setSelectedValues(new ArrayList<>(valores));

            PerfilActivity.allKeysStatic.add(nova);
            PerfilActivity.mySelectedKeysStatic.put(nome, new ArrayList<>(valores));

            Toast.makeText(this, "Chave criada com sucesso!", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        });
    }
}
