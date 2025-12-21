package ao.co.isptec.aplm.projetoanuncioloc.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ao.co.isptec.aplm.projetoanuncioloc.MainActivity;
import ao.co.isptec.aplm.projetoanuncioloc.R;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "anuncios_channel";
    private static final String CHANNEL_NAME = "Anúncios Localizados";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Novo FCM Token: " + token);
        // Salva o token no servidor
        saveTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "Mensagem FCM recebida - From: " + message.getFrom());
        Log.d(TAG, "Notification: " + message.getNotification());
        Log.d(TAG, "Data: " + message.getData());

        // IMPORTANTE: Quando o app está em BACKGROUND, o FCM exibe automaticamente a notificação
        // Se criarmos outra manualmente aqui, teremos DUPLICAÇÃO!
        // Só criar notificação manual quando:
        // 1. App está em FOREGROUND (onMessageReceived só é chamado em foreground)
        // 2. OU quando vem apenas dados (sem notification payload)

        java.util.Map<String, String> data = message.getData();
        String type = data == null ? null : data.get("type");

        // Ignore heartbeat messages completely (no UI, no internal notifications)
        if ("heartbeat".equals(type)
                || (message.getNotification() != null && "heartbeat".equals(message.getNotification().getBody()))) {
            Log.d(TAG, "Recebido heartbeat - ignorando");
            return;
        }

        // If this is an 'anuncio' (either via data or notification+data), show notification and refresh count
        boolean isAnuncio = "anuncio".equals(type) || (data != null && data.containsKey("anuncioId"));

        if (isAnuncio) {
            String title = data != null && data.get("title") != null ? data.get("title") : (message.getNotification() != null ? message.getNotification().getTitle() : null);
            String body = data != null && data.get("body") != null ? data.get("body") : (message.getNotification() != null ? message.getNotification().getBody() : null);
            Log.d(TAG, "Anúncio recebido - Title: " + title + ", Body: " + body);

            boolean appForeground = isAppInForeground();

            // Garantir NOTIFICAÇÃO EXTERNA (sistema) também quando a app está em foreground.
            // Para evitar duplicação no background (quando o payload já contém notification), só forçamos a exibição manual
            // quando a app está em foreground ou quando a mensagem é data-only (message.getNotification()==null).
            if (title != null && body != null) {
                if (appForeground || message.getNotification() == null) {
                    showNotification(title, body);
                    Log.d(TAG, "Notificação externa exibida (sistema) para anúncio: " + title);
                } else {
                    Log.d(TAG, "FCM com payload de notification recebido em background — a exibição será feita pelo sistema");
                }
            }

            // Se a app estiver em foreground, também atualiza a UI internamente
            if (appForeground) {
                Log.d(TAG, "App em foreground — atualizando UI internamente e emitindo broadcast interno");
                try {
                    SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    long userId = prefs.getLong("userId", -1L);
                    if (userId != -1L) {
                        RetrofitClient.getApiService(this).getContagemNotificacoes(userId).enqueue(new retrofit2.Callback<Integer>() {
                            @Override
                            public void onResponse(retrofit2.Call<Integer> call, retrofit2.Response<Integer> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    int contagem = response.body();
                                    prefs.edit().putInt("notificacaoCount", contagem).apply();
                                    Intent i = new Intent("ao.co.isptec.aplm.NOTIF_COUNT_UPDATED");
                                    i.putExtra("count", contagem);
                                    sendBroadcast(i);
                                    // Notificar componentes internos sobre o novo anúncio para recarregar a lista
                                    Intent j = new Intent("ao.co.isptec.aplm.NEW_ANUNCIO_INTERNAL");
                                    j.putExtra("title", title);
                                    j.putExtra("body", body);
                                    sendBroadcast(j);
                                    Log.d(TAG, "Contagem de notificações atualizada para: " + contagem);
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<Integer> call, Throwable t) {
                                Log.e(TAG, "Falha ao atualizar contagem de notificações: " + t.getMessage());
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao tentar atualizar contagem de notificações: " + e.getMessage());
                }
            } else {
                // App em background/fechado → Atualiza contagem de notificações do backend e notifica a UI (assim que possível)
                try {
                    SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    long userId = prefs.getLong("userId", -1L);
                    if (userId != -1L) {
                        RetrofitClient.getApiService(this).getContagemNotificacoes(userId).enqueue(new retrofit2.Callback<Integer>() {
                            @Override
                            public void onResponse(retrofit2.Call<Integer> call, retrofit2.Response<Integer> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    int contagem = response.body();
                                    prefs.edit().putInt("notificacaoCount", contagem).apply();
                                    Intent i = new Intent("ao.co.isptec.aplm.NOTIF_COUNT_UPDATED");
                                    i.putExtra("count", contagem);
                                    sendBroadcast(i);
                                    Log.d(TAG, "Contagem de notificações atualizada para: " + contagem);
                                }
                            }

                            @Override
                            public void onFailure(retrofit2.Call<Integer> call, Throwable t) {
                                Log.e(TAG, "Falha ao atualizar contagem de notificações: " + t.getMessage());
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao tentar atualizar contagem de notificações: " + e.getMessage());
                }
            }

            return;
        }

        // Fallback: comportamento anterior (notification payload in foreground, or data-only with title/body)
        if (message.getNotification() != null) {
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();
            Log.d(TAG, "Exibindo notificação manualmente (app em foreground) - Title: " + title + ", Body: " + body);
            showNotification(title, body);
        } else if (data != null && !data.isEmpty()) {
            String title = data.get("title");
            String body = data.get("body");
            if (title != null && body != null) {
                Log.d(TAG, "Exibindo notificação de dados - Title: " + title + ", Body: " + body);
                showNotification(title, body);
            }
        }
    }

    private boolean isAppInForeground() {
        try {
            android.app.ActivityManager activityManager = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            java.util.List<android.app.ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) return false;
            final String packageName = getPackageName();
            for (android.app.ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(packageName)) {
                    return appProcess.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                            appProcess.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar estado da app: " + e.getMessage());
        }
        return false;
    }

    private void saveTokenToServer(String token) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        // Sempre salva localmente para permitir desregisto no logout, mesmo sem login
        prefs.edit().putString("fcmToken", token).apply();
        Log.d(TAG, "Token FCM salvo localmente");

        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            Log.d(TAG, "UserId não encontrado - envio ao servidor será tentado após login");
            prefs.edit().putBoolean("pendingFcmRegistration", true).apply();
            return;
        }

        // Cria o Map esperado pelo backend
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("token", token);
        String deviceInfo = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        body.put("deviceInfo", deviceInfo);

        Call<Void> call = RetrofitClient.getApiService(this).updateFcmToken(userId, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Token FCM salvo no servidor com sucesso");
                } else {
                    Log.e(TAG, "Erro ao salvar token: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Falha na rede ao salvar token: " + t.getMessage());
                prefs.edit().putBoolean("pendingFcmRegistration", true).apply();
            }
        });
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Cria canal de notificação com importância alta (aparece mesmo com app fechado)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificações de anúncios localizados");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cria notificação do sistema (tipo Facebook) - aparece mesmo com app fechado
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.logoapp)  // Ícone do app
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridade alta
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Som, vibração e luz
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visível mesmo na tela de bloqueio
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body)); // Texto expandido

        // Gera ID único para cada notificação (evita sobreposição)
        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());

        Log.d(TAG, "Notificação do sistema exibida: " + title);
    }
}

