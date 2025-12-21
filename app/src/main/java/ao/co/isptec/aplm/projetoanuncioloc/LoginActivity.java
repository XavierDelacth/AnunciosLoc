package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import ao.co.isptec.aplm.projetoanuncioloc.Request.LoginRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Model.User;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.gson.Gson;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvTabRegister;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvTabRegister = findViewById(R.id.tv_tab_register);
        loadingDialog = new LoadingDialog(this);

        // Diagnostics: confirm button binding and state, force enabled/clickable in case it's disabled by layout or theme
        Log.d("LoginActivity", "onCreate: btnLogin=" + btnLogin);
        if (btnLogin != null) {
            Log.d("LoginActivity", "btn state - visible=" + btnLogin.getVisibility() + " enabled=" + btnLogin.isEnabled() + " clickable=" + btnLogin.isClickable());
            btnLogin.setClickable(true);
            btnLogin.setEnabled(true);
            btnLogin.setFocusable(true);
            btnLogin.setFocusableInTouchMode(true);
            // Diagnostics: log touch events on the button to detect interception by other views
            btnLogin.setOnTouchListener((v, e) -> {
                Log.d("LoginActivity", "btnLogin onTouch: action=" + e.getAction() + " x=" + e.getX() + " y=" + e.getY());
                // If ACTION_UP, run the login action directly to avoid relying on onClick propagation
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("LoginActivity", "btnLogin onTouch ACTION_UP -> invoking login action");
                    // Prevent double-invocation and UI glitches
                    v.setEnabled(false);
                    try {
                        performLoginAction();
                    } catch (Exception ex) {
                        Log.e("LoginActivity", "performLoginAction failed from touch: " + ex.getMessage(), ex);
                        v.setEnabled(true);
                    }
                    return true; // consume touch
                }
                return false; // allow normal handling for other actions
            });
            // Try to ensure the button is not covered by another view
            try {
                btnLogin.bringToFront();
            } catch (Exception ex) {
                Log.w("LoginActivity", "bringToFront failed: " + ex.getMessage());
            }
        } else {
            Log.e("LoginActivity", "btnLogin is null after findViewById");
        }

        // Se já existe sessão (jwt + userId) no SharedPreferences, considera o utilizador como logado
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long savedUserId = prefs.getLong("userId", -1L);
        String savedJwt = prefs.getString("jwt", null);
        if (savedJwt != null && savedUserId != -1L) {
            // Redireciona diretamente para MainActivity — se o token não for válido, as chamadas em Main tratarão 401 e farão logout.
            // Inicia o ForegroundLocationService caso a sessão já exista
            try {
                Intent svc = new Intent(LoginActivity.this, ao.co.isptec.aplm.projetoanuncioloc.Service.ForegroundLocationService.class);
                runOnUiThread(() -> {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            startForegroundService(svc);
                        } else {
                            startService(svc);
                        }
                        Log.d("LoginActivity", "ForegroundLocationService iniciada (sessão existente)");
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Erro ao iniciar ForegroundLocationService (sessão existente, ui thread): " + e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                Log.e("LoginActivity", "Erro ao preparar start do ForegroundLocationService (sessão existente): " + e.getMessage(), e);
            }

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        tvTabRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        // Centraliza a ação de login num método para ser chamada tanto pelo onClick quanto pelo touch
        btnLogin.setOnClickListener(v -> performLoginAction());

        // ... rest of onCreate continues
    }

    // Executa a ação de login (extraída para reuso e testes)
    private void performLoginAction() {
        Log.d("LoginActivity", "performLoginAction invoked");
        Toast.makeText(LoginActivity.this, "Entrando...", Toast.LENGTH_SHORT).show();

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            if (btnLogin != null) btnLogin.setEnabled(true);
            return;
        }

        // MOSTRA O LOADING
        loadingDialog.setMessage("A autenticar...");
        loadingDialog.show();
        if (btnLogin != null) btnLogin.setEnabled(false);

        // CHAMA API
        LoginRequest request = new LoginRequest(username, password);
        Call<User> call = RetrofitClient.getApiService(this).login(request);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                loadingDialog.dismiss();
                if (btnLogin != null) btnLogin.setEnabled(true);
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
                    Log.d("LoginActivity", "JWT salvo: " + prefs.getString("jwt", "Nenhum"));

                    // Inicia o ForegroundLocationService automaticamente após login (começa a enviar localização em segundo plano)
                    try {
                        Intent svc = new Intent(LoginActivity.this, ao.co.isptec.aplm.projetoanuncioloc.Service.ForegroundLocationService.class);
                        // Garantir chamada no thread principal
                        runOnUiThread(() -> {
                            try {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    startForegroundService(svc);
                                } else {
                                    startService(svc);
                                }
                                Log.d("LoginActivity", "ForegroundLocationService iniciada após login");
                            } catch (Exception e) {
                                Log.e("LoginActivity", "Erro ao iniciar ForegroundLocationService (ui thread): " + e.getMessage(), e);
                            }
                        });
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Erro ao preparar start do ForegroundLocationService: " + e.getMessage(), e);
                    }

                    // BUSCA OS PERFIS DO UTILIZADOR NO SERVIDOR e salva localmente
                    RetrofitClient.getApiService(LoginActivity.this).getUserPerfis(userId)
                            .enqueue(new Callback<Map<String, String>>() {
                                @Override
                                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> resp) {
                                    if (resp.isSuccessful() && resp.body() != null) {
                                        Map<String, String> serverMap = resp.body();
                                        // converte Map<String, String(comma-separated)> para Map<String, List<String>>
                                        Map<String, List<String>> selections = new HashMap<>();
                                        for (Map.Entry<String, String> e : serverMap.entrySet()) {
                                            String k = e.getKey();
                                            String v = e.getValue();
                                            if (v == null || v.isEmpty()) continue;
                                            String[] parts = v.split(",");
                                            List<String> vals = new ArrayList<>();
                                            for (String p : parts) {
                                                String trimmed = p.trim();
                                                if (!trimmed.isEmpty()) vals.add(trimmed);
                                            }
                                            if (!vals.isEmpty()) selections.put(k, vals);
                                        }
                                        // grava em SharedPreferences no mesmo formato usado por PerfilActivity
                                        SharedPreferences selPrefs = getSharedPreferences("my_profile_selections_" + userId, MODE_PRIVATE);
                                        Gson g = new Gson();
                                        String json = g.toJson(selections);
                                        selPrefs.edit().putString("selections", json).apply();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                                    // não impedimos o login; apenas logamos
                                }
                            });

                    Toast.makeText(LoginActivity.this, "Login bem-sucedido!", Toast.LENGTH_LONG).show();

                    // Prepara intent para MainActivity, mas espera até tentarmos registar o token FCM (timeout curto)
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", user.getUsername());

                    // Tentativa robusta de registo do token FCM antes de prosseguir
                    final java.util.concurrent.atomic.AtomicBoolean started = new java.util.concurrent.atomic.AtomicBoolean(false);

                    Runnable proceed = () -> {
                        if (started.compareAndSet(false, true)) {
                            startActivity(intent);
                            finish();
                        }
                    };

                    // Timeout: não bloqueia o utilizador mais do que 5s
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(proceed, 5000);

                    try {
                        if (com.google.firebase.FirebaseApp.getApps(LoginActivity.this).isEmpty()) {
                            com.google.firebase.FirebaseApp.initializeApp(LoginActivity.this);
                        }

                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String token = task.getResult();
                                if (token != null) {
                                    prefs.edit().putString("fcmToken", token).apply();
                                    java.util.Map<String, String> body = new java.util.HashMap<>();
                                    body.put("token", token);
                                    body.put("deviceInfo", android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);

                                    // Tenta enviar ao backend e só prossegue ao fim (ou com o timeout)
                                    RetrofitClient.getApiService(LoginActivity.this).updateFcmToken(userId, body)
                                            .enqueue(new Callback<Void>() {
                                                @Override
                                                public void onResponse(Call<Void> call, Response<Void> response) {
                                                    if (response.isSuccessful()) {
                                                        Log.d("LoginActivity", "Token FCM registrado no backend após login");
                                                        prefs.edit().putBoolean("pendingFcmRegistration", false).apply();
                                                    } else {
                                                        Log.e("LoginActivity", "Falha ao registrar token no backend: " + response.code());
                                                        prefs.edit().putBoolean("pendingFcmRegistration", true).apply();
                                                    }
                                                    // Prossegue (se ainda não prosseguiu via timeout)
                                                    proceed.run();
                                                }

                                                @Override
                                                public void onFailure(Call<Void> call, Throwable t) {
                                                    Log.e("LoginActivity", "Erro de rede ao registrar token: " + t.getMessage());
                                                    prefs.edit().putBoolean("pendingFcmRegistration", true).apply();
                                                    proceed.run();
                                                }
                                            });
                                } else {
                                    Log.e("LoginActivity", "Token FCM nulo");
                                    prefs.edit().putBoolean("pendingFcmRegistration", true).apply();
                                    proceed.run();
                                }
                            } else {
                                Log.e("LoginActivity", "Não foi possível obter token FCM: " + task.getException());
                                prefs.edit().putBoolean("pendingFcmRegistration", true).apply();
                                proceed.run();
                            }
                        });
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Firebase não disponível após login: " + e.getMessage(), e);
                        prefs.edit().putBoolean("pendingFcmRegistration", true).apply();
                        proceed.run();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Usuário ou senha incorretos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                loadingDialog.dismiss();
                if (btnLogin != null) btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }
}