package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class AddValorChaveExistenteActivity extends AppCompatActivity {

    private Spinner spinnerChaves;
    private android.widget.EditText etNovoValor;
    private Button btnAdicionar;
    private TextView btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existente_key); // vamos criar este layout já

         // ← CORRETO
        btnAdicionar = findViewById(R.id.btn_add_value);    // ← CORRETO
        btnClose = findViewById(R.id.btn_close);

        // Preenche spinner com nomes das chaves existentes
        ArrayList<String> nomesChaves = new ArrayList<>();
        for (ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey key : PerfilActivity.allKeysStatic) {
            nomesChaves.add(key.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nomesChaves);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChaves.setAdapter(adapter);

        btnClose.setOnClickListener(v -> finish());

        btnAdicionar.setOnClickListener(v -> {
            String chaveSelecionada = spinnerChaves.getSelectedItem().toString();
            String novoValor = etNovoValor.getText().toString().trim();

            if (novoValor.isEmpty()) {
                Toast.makeText(this, "Digite um valor", Toast.LENGTH_SHORT).show();
                return;
            }

            // Adiciona ao ProfileKey correto
            for (ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey key : PerfilActivity.allKeysStatic) {
                if (key.getName().equals(chaveSelecionada)) {
                    if (!key.getAvailableValues().contains(novoValor)) {
                        key.getAvailableValues().add(novoValor);
                    }
                    if (!key.getSelectedValues().contains(novoValor)) {
                        key.getSelectedValues().add(novoValor);
                    }
                    break;
                }
            }

            // Atualiza mapa global
            PerfilActivity.mySelectedKeysStatic.computeIfAbsent(chaveSelecionada, k -> new ArrayList<>()).add(novoValor);

            Toast.makeText(this, "Valor adicionado!", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        });
    }
}
