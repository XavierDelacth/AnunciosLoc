package ao.co.isptec.aplm.projetoanuncioloc;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class AlterarSenhaActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnConfirmPassword;
    private ImageButton btnClosePassword, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.ativity_mudar_senha);

        // Views
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnConfirmPassword = findViewById(R.id.btnConfirmPassword);
        btnClosePassword = findViewById(R.id.btnClosePassword);
        btnBack = findViewById(R.id.btnBack);

        // Botões de fechar
        btnClosePassword.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());

        // Confirmar alteração
        btnConfirmPassword.setOnClickListener(v -> alterarSenha());

        // Back press moderno (Android 14+)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void alterarSenha() {
        String atual = etCurrentPassword.getText().toString().trim();
        String nova = etNewPassword.getText().toString().trim();
        String confirmar = etConfirmPassword.getText().toString().trim();

        if (atual.isEmpty() || nova.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nova.equals(confirmar)) {
            etConfirmPassword.setError("As senhas não coincidem!");
            etConfirmPassword.requestFocus();
            return;
        }

        if (nova.length() < 6) {
            etNewPassword.setError("A nova senha deve ter pelo menos 6 caracteres");
            etNewPassword.requestFocus();
            return;
        }

        // AQUI VAI A TUA LÓGICA REAL (Firebase, API, etc.)
        // Exemplo com Firebase Auth:
        // FirebaseAuth.getInstance().getCurrentUser().updatePassword(nova)...

        Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_LONG).show();
        finish(); // volta ao perfil
    }
}
