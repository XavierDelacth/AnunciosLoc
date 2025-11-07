package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class AdicionarGPS extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gps);

        // Views
        ImageView btnFechar = findViewById(R.id.btnFechar);
        Button btnCancelar = findViewById(R.id.btnCancelar);
        Button btnAdicionar = findViewById(R.id.btnAdicionar);
        EditText etNomeLocal = findViewById(R.id.etNomeLocal);
        Button btnGps = findViewById(R.id.btnGps);
        Button btnWifi = findViewById(R.id.btnWifi);

        // Fechar com X ou Cancelar
        btnFechar.setOnClickListener(v -> finish());
        btnCancelar.setOnClickListener(v -> finish());

        // Botão Adicionar
        btnAdicionar.setOnClickListener(v -> {
            String nome = etNomeLocal.getText().toString().trim();
            if (nome.isEmpty()) {
                etNomeLocal.setError("Digite o nome do local");
                etNomeLocal.requestFocus();
                return;
            }
            Toast.makeText(this, "Local '" + nome + "' adicionado!", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Alternar GPS / WIFI
        btnGps.setOnClickListener(v -> {
            btnGps.setBackgroundTintList(getColorStateList(R.color.verde_principal));
            btnWifi.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        });

        btnWifi.setOnClickListener(v -> {
            btnWifi.setBackgroundTintList(getColorStateList(R.color.verde_principal));
            btnGps.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        });

        // === Clique nos botões GPS / WIFI ===
        btnGps.setOnClickListener(v -> {
            // Já está no GPS → não faz nada
        });

        btnWifi.setOnClickListener(v -> {
            // Abre o modal WIFI como nova Activity
            startActivity(new Intent(AdicionarGPS.this, AdicionarWIFI.class));
            finish(); // fecha o GPS para não ficar na pilha
        });

        // === CORREÇÃO OFICIAL DO onBackPressed (AndroidX) ===
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // fecha o modal instantaneamente
            }
        });
}}
