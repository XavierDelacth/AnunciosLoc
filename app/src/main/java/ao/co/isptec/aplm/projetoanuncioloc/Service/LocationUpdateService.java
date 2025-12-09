// LocationUpdateService.java corrigido completo
// Adiciona verificação de permissão para localização + WiFi

package ao.co.isptec.aplm.projetoanuncioloc.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.LocationUpdateRequest;  
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class LocationUpdateService extends Service {

    private static final String TAG = "LocationService";
    private static final long UPDATE_INTERVAL = 5 * 60 * 1000; // 5 min

    private FusedLocationProviderClient fusedLocationClient;
    private WifiManager wifiManager;
    private Handler handler;
    private Runnable updateRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        handler = new Handler(Looper.getMainLooper());

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateLocation();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(updateRunnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateLocation() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            Log.e(TAG, "UserId não encontrado - Atualização ignorada");
            return;
        }

        // Verifica permissão para localização (resolve o lint warning)
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            Log.e(TAG, "Permissão ACCESS_FINE_LOCATION ausente - Localização ignorada");
            return;
        }

        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                List<String> wifiIds = getVisibleWifiSsids();

                LocationUpdateRequest request = new LocationUpdateRequest(userId, lat, lng, wifiIds);
                Call<Void> call = RetrofitClient.getApiService(this).updateLocation(request);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Localização atualizada com sucesso");
                        } else {
                            Log.e(TAG, "Erro ao atualizar localização: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e(TAG, "Falha na rede: " + t.getMessage());
                    }
                });
            } else {
                Log.e(TAG, "Localização GPS nula");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Erro ao pegar localização: " + e.getMessage()));
    }

    private List<String> getVisibleWifiSsids() {
        List<String> ssids = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            Log.e(TAG, "Permissão ACCESS_FINE_LOCATION ausente - WiFi scan ignorado");
            return ssids;
        }

        if (!wifiManager.isWifiEnabled()) {
            Log.d(TAG, "WiFi desligado - Scan ignorado");
            return ssids;
        }

        boolean scanStarted = wifiManager.startScan();
        if (!scanStarted) {
            Log.e(TAG, "Falha ao iniciar WiFi scan");
            return ssids;
        }

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupção no sleep: " + e.getMessage());
        }

        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult result : scanResults) {
            if (result.SSID != null && !result.SSID.isEmpty()) {
                ssids.add(result.SSID);
            }
        }

        Log.d(TAG, "WiFi SSIDs encontrados: " + ssids.size());
        return ssids;
    }
}