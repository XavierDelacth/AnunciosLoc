package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class AdicionarWIFI extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wifi);

        ImageView btnFecharWiFi = findViewById(R.id.btnFecharWiFi);
        Button btnCancelarWiFi = findViewById(R.id.btnCancelarWiFi);
        Button btnAdicionarWiFi = findViewById(R.id.btnAdicionarWiFi);
        EditText etNomeLocalWiFi = findViewById(R.id.etNomeLocalWiFi);
        EditText etSSID = findViewById(R.id.etSSID);
        Button btnGps = findViewById(R.id.btnGps);

        Button btnGpsToggle = findViewById(R.id.btnGpsToggle);
        Button btnWifiToggle = findViewById(R.id.btnWifiToggle);;

        // Adicionar WIFI
        btnAdicionarWiFi.setOnClickListener(v -> {
            String nome = etNomeLocalWiFi.getText().toString().trim();
            String ssid = etSSID.getText().toString().trim();

            if (nome.isEmpty() || ssid.isEmpty()) {
                if (nome.isEmpty()) etNomeLocalWiFi.setError("Nome obrigatório");
                if (ssid.isEmpty()) etSSID.setError("SSID obrigatório");
                return;
            }

            Toast.makeText(this, "Local WiFi '" + nome + "' (SSID: " + ssid + ") adicionado!", Toast.LENGTH_LONG).show();
            finish();
        });

        btnGpsToggle.setOnClickListener(v -> {
            startActivity(new Intent(AdicionarWIFI.this, AdicionarGPS.class));
            finish();
        });

        btnWifiToggle.setOnClickListener(v -> {
            // Já está no WIFI → não faz nada
        });



        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

}
