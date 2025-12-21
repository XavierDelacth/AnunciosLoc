package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class App extends Application {
    private static final String TAG = "AnunciosLocApp";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
        } catch (Exception e) {
            Log.w(TAG, "Firebase init falhou: " + e.getMessage());
        }

        // Ao iniciar a app, se tivermos userId e token pendente, tentar registrar token no backend
        try {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            long userId = prefs.getLong("userId", -1L);
            boolean pending = prefs.getBoolean("pendingFcmRegistration", false);
            String existing = prefs.getString("fcmToken", null);
            if (userId != -1L && (pending || existing == null)) {
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        if (token != null) {
                            prefs.edit().putString("fcmToken", token).putBoolean("pendingFcmRegistration", false).apply();
                            java.util.Map<String, String> body = new java.util.HashMap<>();
                            body.put("token", token);
                            body.put("deviceInfo", android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
                            RetrofitClient.getApiService(this).updateFcmToken(userId, body).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Log.d(TAG, "Token FCM registrado no backend (startup)");
                                    } else {
                                        Log.e(TAG, "Falha ao registrar token no startup: " + response.code());
                                        prefs.edit().putBoolean("pendingFcmRegistration", true).apply();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Log.e(TAG, "Erro de rede ao registrar token no startup: " + t.getMessage());
                                    prefs.edit().putBoolean("pendingFcmRegistration", true).apply();
                                }
                            });
                        }
                    } else {
                        Log.w(TAG, "Não foi possível obter token FCM no startup: " + task.getException());
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao tentar registrar token no startup: " + e.getMessage());
        }
    }
}