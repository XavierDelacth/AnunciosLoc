package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import ao.co.isptec.aplm.projetoanuncioloc.Model.LoginRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Model.User;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvTabRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvTabRegister = findViewById(R.id.tv_tab_register);

        tvTabRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // CHAMA API
            LoginRequest request = new LoginRequest(username, password);
            Call<User> call = RetrofitClient.getApiService(this).login(request);

            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        String jwt = user.getSessionId();
                        Long userId = user.getId();
                        String username = user.getUsername();

                        // SALVA NO SHARED PREFERENCES
                        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                        prefs.edit()
                                .putLong("userId", userId)
                                .putString("jwt", jwt)
                                .putString("username", username)
                                .apply();

                        Toast.makeText(LoginActivity.this, "Login bem-sucedido!", Toast.LENGTH_LONG).show();

                        // VAI PARA MAIN
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("username", user.getUsername());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Usuário ou senha incorretos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}