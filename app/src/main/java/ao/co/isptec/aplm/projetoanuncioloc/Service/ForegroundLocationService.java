package ao.co.isptec.aplm.projetoanuncioloc.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.LocationUpdateRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ForegroundLocationService extends Service {

    private static final String TAG = "FGLocationService";
    public static final String CHANNEL_ID = "location_channel";
    private static final int NOTIF_ID = 4321;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (result == null) return;
                var loc = result.getLastLocation();
                if (loc == null) return;
                double lat = loc.getLatitude();
                double lng = loc.getLongitude();
                Log.d(TAG, "Localização capturada: " + lat + ", " + lng);
                sendLocationToServer(lat, lng);
            }
        };
    }

    private void sendLocationToServer(double lat, double lng) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) return;

        List<String> wifiIds = new ArrayList<>();
        LocationUpdateRequest request = new LocationUpdateRequest(userId, lat, lng, wifiIds);

        Log.d(TAG, "Sending location to server: userId=" + userId + " lat=" + lat + " lng=" + lng);

        Call<Void> call = RetrofitClient.getApiService(this).updateLocation(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Localização enviada: " + lat + ", " + lng + " (HTTP " + response.code() + ")");
                } else {
                    String err = "";
                    try {
                        if (response.errorBody() != null) err = response.errorBody().string();
                    } catch (Exception ex) {
                        err = "(error reading body: " + ex.getMessage() + ")";
                    }
                    Log.e(TAG, "Falha ao enviar localização - HTTP " + response.code() + " err=" + err);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Falha ao enviar localização: " + t.getMessage(), t);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Stronger check: on Android S+ we need both the FOREGROUND_SERVICE_LOCATION permission AND a location permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean hasFgsPerm = checkSelfPermission("android.permission.FOREGROUND_SERVICE_LOCATION") == PERMISSION_GRANTED;
            boolean hasLocationPerm = checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED || checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED;
            if (!hasFgsPerm || !hasLocationPerm) {
                Log.e(TAG, "Cannot start foreground service: missing FOREGROUND_SERVICE_LOCATION or location permission (fg=" + hasFgsPerm + ", loc=" + hasLocationPerm + ")");
                // Ensure we call startForeground to satisfy the OS timing requirement, then stop the service and notify UI to request permissions
                createNotificationChannel();
                Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("AnúnciosLoc — Localização")
                        .setContentText("Permissões de localização ausentes. Abra a aplicação para conceder.")
                        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .build();
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        startForeground(NOTIF_ID, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
                    } else {
                        startForeground(NOTIF_ID, notif);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao chamar startForeground em missing-perm path: " + e.getMessage(), e);
                }
                try {
                    Intent b = new Intent("ao.co.isptec.aplm.FGS_START_FAILED");
                    b.putExtra("reason", "missing_permissions");
                    sendBroadcast(b);
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to broadcast FGS_START_FAILED: " + ex.getMessage());
                }
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        createNotificationChannel();

        Intent i = new Intent(this, ao.co.isptec.aplm.projetoanuncioloc.MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("AnúnciosLoc — Localização")
                .setContentText("Enviando localização em segundo plano")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        // IMPORTANT: call startForeground immediately to satisfy the OS requirement
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Usa a sobrecarga com serviceType para marcar como FGS de localização
                startForeground(NOTIF_ID, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(NOTIF_ID, notif);
            }
            Log.d(TAG, "startForeground called early to satisfy OS requirement");
        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException when calling startForeground early: " + se.getMessage(), se);
            try {
                Intent b = new Intent("ao.co.isptec.aplm.FGS_START_FAILED");
                b.putExtra("reason", "security_exception");
                sendBroadcast(b);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to broadcast FGS_START_FAILED after early SecurityException: " + ex.getMessage());
            }
            stopSelf();
            return START_NOT_STICKY;
        } catch (RuntimeException re) {
            Log.e(TAG, "RuntimeException when calling startForeground early: " + re.getMessage(), re);
            stopSelf();
            return START_NOT_STICKY;
        }

        try {
            // Extra check: consult AppOps to detect device-level denial of FGS for location
            try {
                android.app.AppOpsManager aom = (android.app.AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                if (aom != null) {
                    String op;
                    try {
                        java.lang.reflect.Field f = android.app.AppOpsManager.class.getField("OPSTR_FOREGROUND_SERVICE_LOCATION");
                        op = (String) f.get(null);
                    } catch (Throwable e) {
                        op = "android:foreground_service_location";
                    }
                    int mode = aom.checkOpNoThrow(op, android.os.Process.myUid(), getPackageName());
                    if (mode != android.app.AppOpsManager.MODE_ALLOWED) {
                        Log.w(TAG, "AppOps forbids starting FGS (mode=" + mode + ") — aborting start");
                        try {
                            Intent b = new Intent("ao.co.isptec.aplm.FGS_START_FAILED");
                            b.putExtra("reason", "appops_denied");
                            sendBroadcast(b);
                        } catch (Exception ex) {
                            Log.e(TAG, "Failed to broadcast FGS_START_FAILED after AppOps check: " + ex.getMessage());
                        }
                        // We already called startForeground to satisfy the system; now stop and exit
                        stopForeground(true);
                        stopSelf();
                        return START_NOT_STICKY;
                    }
                }
            } catch (Throwable t) {
                Log.d(TAG, "AppOps check not available or failed: " + t.getMessage());
                Log.d(TAG, "AppOps unavailable — proceeding with best-effort FGS (permissions present)");
                // Can't verify AppOps — continue running (best-effort) but notify UI
                try {
                    Intent b = new Intent("ao.co.isptec.aplm.FGS_START_FAILED");
                    b.putExtra("reason", "appops_unavailable");
                    sendBroadcast(b);
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to broadcast FGS_START_FAILED after AppOps exception: " + ex.getMessage());
                }
                // Proceed with location updates (we already have called startForeground)
            }
        } catch (Exception ex) {
            Log.e(TAG, "Unexpected error during AppOps check: " + ex.getMessage(), ex);
            // If something unexpected happened, stop the service cleanly
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        // Marca que o serviço FGS está em execução (usado pela UI para evitar duplicados)
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("fgLocationServiceRunning", true).apply();

        startLocationUpdates();
        return START_STICKY;
    }

    private void startLocationUpdates() {
        if (checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            Log.e(TAG, "Sem permissão de localização");
            stopSelf();
            return;
        }
        LocationRequest req = LocationRequest.create();
        req.setInterval(2 * 60 * 1000); // 2 minutos
        req.setFastestInterval(60 * 1000); // 1 minuto
        req.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        Log.d(TAG, "Requesting location updates: interval=" + req.getInterval() + " fastest=" + req.getFastestInterval());
        fusedLocationClient.requestLocationUpdates(req, locationCallback, null);
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        // Limpa marcação de execução do FGS
        try {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("fgLocationServiceRunning", false).apply();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar flag fgLocationServiceRunning: " + e.getMessage());
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Localização em segundo plano", NotificationManager.IMPORTANCE_LOW);
            NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (mgr != null) mgr.createNotificationChannel(channel);
        }
    }
}
