package ao.co.isptec.aplm.projetoanuncioloc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.MainAnuncioAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse;
import ao.co.isptec.aplm.projetoanuncioloc.Service.LocationUpdateService;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_EDITAR_ANUNCIO = 1001;
    private CardView cardLocais, cardAnuncios;
    private TextView tabCriados, tabGuardados, tvLocation, tvEmptyAnuncios;
    private ImageView btnProfile, btnNotification;
    private RecyclerView rvAnunciosMain;
    private MainAnuncioAdapter adapter;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TextView badgeNotificacao;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int BACKGROUND_LOCATION_PERMISSION_CODE = 102;
    private static final int FOREGROUND_SERVICE_LOCATION_PERMISSION_CODE = 300;

    // Receiver para atualizações da contagem de notificações vindas do serviço FCM
    private final android.content.BroadcastReceiver notifCountReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int count = intent.getIntExtra("count", -1);
            if (count >= 0) {
                atualizarBadgeNotificacao(count);
            }
        }
    };

    // Receiver para falha ao iniciar FGS — instrui a UI a pedir permissões
    private final android.content.BroadcastReceiver fgsFailReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String reason = intent.getStringExtra("reason");
            Log.w("MainActivity", "Recebido FGS_START_FAILED: " + reason);

            // Mostra um diálogo com opções: abrir definições ou tentar novamente
            runOnUiThread(() -> {
                try {
                    new android.app.AlertDialog.Builder(MainActivity.this)
                            .setTitle("Impossível ativar localização em 2º plano")
                            .setMessage("O sistema impediu que a app inicie localização em segundo plano (" + reason + "). Deseja abrir as definições da app para ajustar permissões ou tentar novamente?")
                            .setPositiveButton("Abrir definições", (d, w) -> openAppSettings())
                            .setNeutralButton("Tentar novamente", (d, w) -> {
                                // Tenta reativar o fluxo de verificação/início do FGS
                                // Nota: não resetamos a flag de verificação — garantimos que o fluxo de verificação só será mostrado uma vez
                                // Curto delay para garantir que a UI settle
                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> startForegroundLocationIfNeeded(), 300);
                            })
                            .setNegativeButton("Fechar", null)
                            .show();
                } catch (Exception e) {
                    // Fallback minimal: Toast
                    Toast.makeText(MainActivity.this, "Não foi possível ativar localização em 2º plano: verifique permissões", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private boolean gpsAtivado = false;
    private static final int REQUEST_ENABLE_GPS = 1002;
    private LocationCallback locationCallback;
    private Handler locationTimeoutHandler;

    private static final int LOCATION_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1);
        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Explicação opcional para o utilizador
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this,
                            "A app precisa da localização para mostrar anúncios próximos",
                            Toast.LENGTH_LONG).show();
                }

                // Evita pedir permissões repetidamente em loops (ex.: o utilizador escolheu "só desta vez")
                long lastPrompt = prefs.getLong("lastLocationPrompt", 0L);
                long now = System.currentTimeMillis();
                if (now - lastPrompt < 10_000L) {
                    // Já pedimos há menos de 10s — não pedir de novo imediatamente
                    Log.d("MainActivity", "Já pedimos permissão de localização recentemente; não pedir novamente agora");
                    // Apenas mostra um aviso curto e deixa a UI continuar
                    Toast.makeText(this, "Conceda permissão de localização nas configurações, se necessário", Toast.LENGTH_SHORT).show();
                    // Inicia o fluxo mesmo sem permissão (lista ficará sem localização)
                    setupListaAnuncios();
                } else {
                    // Regista a hora em que pedimos e solicita a permissão ao sistema
                    prefs.edit().putLong("lastLocationPrompt", now).apply();
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST);
                }

            } else {
                // Já tem permissão → inicia o serviço
                iniciarLocationService();
            }
        } else {
            // Android antigo → inicia direto
            iniciarLocationService();
        }

        initViews();
        setupClickListeners();
        setupTabs();
        selectTab(true);

        // Regista receiver para atualizações de contagem de notificações
        android.content.IntentFilter notifFilter = new android.content.IntentFilter("ao.co.isptec.aplm.NOTIF_COUNT_UPDATED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // A partir do Android 13 (Tiramisu) é exigido especificar se o receiver é exportado ou não
            registerReceiver(notifCountReceiver, notifFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(notifCountReceiver, notifFilter);
        }

        // Inicializa provedor de localização
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializa o handler para timeout
        locationTimeoutHandler = new Handler(Looper.getMainLooper());

        // Verifica estado inicial do GPS
        gpsAtivado = isGPSEnabled();

        if (!gpsAtivado) {
            mostrarDialogGPS();
        } else {
            // Tenta obter localização
            obterLocalizacaoAtual();
        }

        carregarAnuncios();
        carregarContagemNotificacoes();

        // Garantir canal de notificação e permissões (Android 13+)
        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 200);
            }
        }

        // Se ainda não temos permissão de background location (Android Q+), pedir ao utilizador de forma educada
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Permissão de Localização em Segundo Plano")
                        .setMessage("Para enviar a sua localização mesmo quando a app está em segundo plano (ex.: para mostrar anúncios próximos continuamente), permita a localização em segundo plano.")
                        .setPositiveButton("Permitir", (dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION, "android.permission.FOREGROUND_SERVICE_LOCATION"}, BACKGROUND_LOCATION_PERMISSION_CODE))
                        .setNegativeButton("Não", (dialog, which) -> Log.d("MainActivity", "Background location negada pelo utilizador"))
                        .show();
                // Marca que já mostramos a verificação do sistema/permissão para não repetir (one-shot)
                try {
                    getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("fgs_verification_shown", true).apply();
                } catch (Exception ex) { /* ignore */ }
            } else {
                // Se já tiver, confirma que temos a permissão específica de FGS location (Android S+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checkSelfPermission("android.permission.FOREGROUND_SERVICE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{"android.permission.FOREGROUND_SERVICE_LOCATION"}, FOREGROUND_SERVICE_LOCATION_PERMISSION_CODE);
                } else {
                    startForegroundLocationIfNeeded();
                }
            }
        } else {
            // Em versões antigas, garante que o serviço foreground pode ser iniciado (não obrigatório)
            startForegroundLocationIfNeeded();
        }

        // Se anteriormente falhou o registro do token FCM, tenta novamente aqui (após login estar estabelecido)
        boolean pending = prefs.getBoolean("pendingFcmRegistration", false);
        long currentUserId = prefs.getLong("userId", -1L);
        if (pending && currentUserId != -1L) {
            try {
                if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                    com.google.firebase.FirebaseApp.initializeApp(this);
                }

                com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        if (token != null) {
                            prefs.edit().putString("fcmToken", token).putBoolean("pendingFcmRegistration", false).apply();
                            java.util.Map<String, String> body = new java.util.HashMap<>();
                            body.put("token", token);
                            body.put("deviceInfo", android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL);
                            RetrofitClient.getApiService(this).updateFcmToken(currentUserId, body).enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Log.d("MainActivity", "Retry: token FCM registrado no backend");
                                    } else {
                                        Log.e("MainActivity", "Retry: falha ao registrar token: " + response.code());
                                        prefs.edit().putBoolean("pendingFcmRegistration", true).apply();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Log.e("MainActivity", "Retry: erro de rede ao registrar token: " + t.getMessage());
                                    prefs.edit().putBoolean("pendingFcmRegistration", true).apply();
                                }
                            });
                        }
                    } else {
                        Log.e("MainActivity", "Retry: não foi possível obter token FCM: " + task.getException());
                    }
                });
            } catch (Exception e) {
                Log.e("MainActivity", "Retry: Firebase não disponível no MainActivity: " + e.getMessage(), e);
            }
        }

        // Regista receiver para falhas ao iniciar FGS (ex.: falta de permissão específica do SO)
        try {
            android.content.IntentFilter f = new android.content.IntentFilter("ao.co.isptec.aplm.FGS_START_FAILED");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(fgsFailReceiver, f, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(fgsFailReceiver, f);
            }
        } catch (Exception ex) {
            Log.e("MainActivity", "Erro ao registar receiver FGS_START_FAILED: " + ex.getMessage());
        }

        // Verifica se as notificações estão ativas nas configurações do sistema
        boolean notificationsEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        if (!notificationsEnabled) {
            Log.d("MainActivity", "Notificações do sistema estão desativadas para esta app");
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Notificações desativadas")
                    .setMessage("As notificações estão desativadas nas configurações do dispositivo. Deseja abrir as definições da app para ativá-las?")
                    .setPositiveButton("Abrir definições", (d, w) -> {
                        try {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                            intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                            startActivity(intent);
                        } catch (Exception ex) {
                            try {
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            } catch (Exception ex2) {
                                Log.e("MainActivity", "Failed to open notification settings: " + ex2.getMessage());
                            }
                        }
                    })
                    .setNegativeButton("Fechar", null)
                    .show();
        }

        // Compatível com back gesture (Android 13+)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getIntent().getBooleanExtra("fromGuardados", false)) {
                    selectTab(true);
                    getIntent().removeExtra("fromGuardados");
                } else {
                    finish();
                }
            }
        });

        startService(new Intent(this, LocationUpdateService.class));
    }

    private void initViews() {
        cardLocais = findViewById(R.id.cardLocais);
        cardAnuncios = findViewById(R.id.cardAnuncios);
        tabCriados = findViewById(R.id.tabCriados);
        tabGuardados = findViewById(R.id.tabGuardados);
        btnProfile = findViewById(R.id.btnProfile);
        btnNotification = findViewById(R.id.btnNotification);
        tvLocation = findViewById(R.id.tvLocation);
        tvEmptyAnuncios = findViewById(R.id.tvEmptyAnuncios);
        rvAnunciosMain = findViewById(R.id.recyclerView);
        badgeNotificacao = findViewById(R.id.badgeNotificacao);
    }

    private void setupClickListeners() {
        cardLocais.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarLocalActivity.class)));

        /*cardAnuncios.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarAnunciosActivity.class)));
    */
        cardAnuncios.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdicionarAnunciosActivity.class);
            startActivityForResult(intent, AdicionarAnunciosActivity.REQUEST_CODE_EDIT);
        });

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));

        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificacoesActivity.class)));
    }

    private void setupTabs() {
        tabCriados.setOnClickListener(v -> selectTab(true));

        tabGuardados.setOnClickListener(v -> {
            selectTab(false);
            Intent intent = new Intent(this, AnuncioGuardadoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }

    private void selectTab(boolean isCriados) {
        if (isCriados) {
            tabCriados.setBackgroundColor(getColor(R.color.verde_principal));
            tabCriados.setTextColor(getColor(R.color.white));
            tabGuardados.setBackgroundColor(getColor(R.color.white));
            tabGuardados.setTextColor(getColor(R.color.verde_principal));
        } else {
            tabCriados.setBackgroundColor(getColor(R.color.white));
            tabCriados.setTextColor(getColor(R.color.verde_principal));
            tabGuardados.setBackgroundColor(getColor(R.color.verde_principal));
            tabGuardados.setTextColor(getColor(R.color.white));
        }
    }

    private void obterLocalizacaoAtual() {
        Log.d("MAPS_DEBUG", "Iniciando obtenção de localização...");

        // Mostra estado de carregamento
        runOnUiThread(() -> { if (tvLocation != null) tvLocation.setText("Obtendo localização..."); });

        // Verifica se já tem permissão
        if (temPermissaoLocalizacao()) {
            Log.d("MAPS_DEBUG", "Permissões já concedidas, obtendo localização...");
            obterLocalizacaoComPermissao();
        } else {
            // Solicita permissões
            Log.d("MAPS_DEBUG", "Solicitando permissões de localização...");
        }
    }

    // Garante que o canal de notificação necessário exista (usado por FCM)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("anuncios_channel", "Anúncios Localizados", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notificações de anúncios localizados");
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @SuppressLint("MissingPermission")
    private void obterLocalizacaoComPermissao() {
        // Primeiro tenta obter a última localização conhecida (mais rápido)
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(this, location -> {
            if (location != null && location.getTime() > System.currentTimeMillis() - 2 * 60 * 1000) {
                // Localização recente (menos de 2 minutos)
                Log.d("MAPS_DEBUG", "Localização recente obtida: " + location.getLatitude() + ", " + location.getLongitude());
                converterCoordenadasParaEndereco(location);
            } else {
                Log.d("MAPS_DEBUG", "Última localização não disponível ou muito antiga, solicitando atualizações...");
                solicitarAtualizacoesDeLocalizacao();
            }
        });

        task.addOnFailureListener(this, e -> {
            Log.e("MAPS_DEBUG", "Erro ao obter última localização: " + e.getMessage());
            solicitarAtualizacoesDeLocalizacao();
        });
    }

    @SuppressLint("MissingPermission")
    private void solicitarAtualizacoesDeLocalizacao() {
        Log.d("MAPS_DEBUG", "Solicitando atualizações de localização...");

        if (!temPermissaoLocalizacao()) {
            Log.e("MAPS_DEBUG", "Permissões não concedidas para atualizações");
            return;
        }

        try {
            // Remove callbacks anteriores para evitar múltiplas solicitações
            if (locationCallback != null) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }

            // Configuração para obter localização rápida
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000); // 10 segundos
            locationRequest.setFastestInterval(5000); // 5 segundos
            locationRequest.setNumUpdates(1); // Apenas uma atualização
            locationRequest.setMaxWaitTime(15000); // Timeout de 15 segundos

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            Log.d("MAPS_DEBUG", "Localização obtida via atualizações: " +
                                    location.getLatitude() + ", " + location.getLongitude());
                            converterCoordenadasParaEndereco(location);
                        } else {
                            Log.e("MAPS_DEBUG", "Localização nula nas atualizações");
                            runOnUiThread(() -> tvLocation.setText("Localização não disponível"));
                        }
                    } else {
                        Log.e("MAPS_DEBUG", "LocationResult é nulo");
                        runOnUiThread(() -> tvLocation.setText("Localização não disponível"));
                    }

                    // Para as atualizações após receber uma localização
                    fusedLocationProviderClient.removeLocationUpdates(this);

                    // Remove o timeout
                    if (locationTimeoutHandler != null) {
                        locationTimeoutHandler.removeCallbacksAndMessages(null);
                    }
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );

            // Timeout: Se após 15 segundos não obtiver localização, mostra mensagem
            if (locationTimeoutHandler != null) {
                locationTimeoutHandler.postDelayed(() -> {
                    runOnUiThread(() -> {
                        String textoAtual = (tvLocation != null ? tvLocation.getText().toString() : "");
                        if (textoAtual.equals("Obtendo localização...")) {
                            if (tvLocation != null) tvLocation.setText("Localização não disponível");
                            Log.d("MAPS_DEBUG", "Timeout na obtenção de localização");

                            // Para as atualizações
                            if (locationCallback != null && fusedLocationProviderClient != null) {
                                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            }
                        }
                    });
                }, 15000);
            }

        } catch (SecurityException e) {
            Log.e("MAPS_DEBUG", "Erro de segurança: " + e.getMessage());
            runOnUiThread(() -> tvLocation.setText("Erro de permissão"));
        } catch (Exception e) {
            Log.e("MAPS_DEBUG", "Erro inesperado: " + e.getMessage());
            runOnUiThread(() -> tvLocation.setText("Erro ao obter localização"));
        }
    }

    private void setupListaAnuncios() {


        // Setup RecyclerView
        rvAnunciosMain.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAnuncioAdapter(this, listaAnuncios, true, null); // ← MUDAR AQUI
        rvAnunciosMain.setAdapter(adapter);

        atualizarVisibilidade();
    }

    private void atualizarVisibilidade() {
        if (listaAnuncios.isEmpty()) {
            rvAnunciosMain.setVisibility(View.GONE);
            tvEmptyAnuncios.setVisibility(View.VISIBLE);
            tvEmptyAnuncios.setText("Nenhum anúncio");
        } else {
            rvAnunciosMain.setVisibility(View.VISIBLE);
            tvEmptyAnuncios.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            if (requestCode == LOCATION_PERMISSION_REQUEST) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permissão concedida → inicia o serviço
                    Toast.makeText(this, "Permissão de localização concedida", Toast.LENGTH_SHORT).show();
                    try {
                        iniciarLocationService();
                    } catch (Exception e) {
                        Log.e("MAPS_DEBUG", "Erro ao iniciar LocationService após permissão: " + e.getMessage(), e);
                    }
                } else {
                    // Permissão negada
                    Toast.makeText(this, "Sem permissão de localização, os anúncios próximos não funcionarão",
                            Toast.LENGTH_LONG).show();
                    // Opcional: desabilita botões ou features que usam localização
                }
            }

            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MAPS_DEBUG", "Permissão concedida, obtendo localização...");

                    // Pequeno delay para garantir que as permissões foram processadas
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (temPermissaoLocalizacao()) {
                            obterLocalizacaoComPermissao();
                        }
                    }, 500);

                } else {
                    // Permissão negada
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // Usuário negou, mas não marcou "nunca mais perguntar"
                        if (tvLocation != null) tvLocation.setText("Localização necessária para mostrar anúncios próximos");

                        new android.app.AlertDialog.Builder(this)
                                .setTitle("Permissão de Localização")
                                .setMessage("O app precisa da localização para mostrar anúncios relevantes da sua área. Deseja permitir?")
                                .setPositiveButton("Sim", (dialog, which) -> obterLocalizacaoAtual())
                                .setNegativeButton("Não", (dialog, which) -> {
                                    if (tvLocation != null) tvLocation.setText("Localização não permitida");
                                    setupListaAnuncios();
                                })
                                .show();
                    } else {
                        // Usuário marcou "nunca mais perguntar"
                        if (tvLocation != null) tvLocation.setText("Ative a localização nas configurações");

                        new android.app.AlertDialog.Builder(this)
                                .setTitle("Localização Necessária")
                                .setMessage("Para usar todos os recursos do app, ative a localização nas configurações do dispositivo.")
                                .setPositiveButton("Configurações", (dialog, which) -> {
                                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                })
                                .setNegativeButton("Cancelar", (dialog, which) -> {
                                    if (tvLocation != null) tvLocation.setText("Localização desativada");
                                    setupListaAnuncios();
                                })
                                .show();
                    }
                }
            }

            // RESULTADO DO PEDIDO DE PERMISSÃO DE NOTIFICAÇÕES (REQUEST CODE: 200)
            if (requestCode == 200) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Permissão de notificação concedida");
                    Toast.makeText(this, "Notificações ativadas", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("MainActivity", "Permissão de notificação negada");
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                        // Usuário negou, mas permitimos mostrar a explicação e tentar de novo
                        new android.app.AlertDialog.Builder(this)
                                .setTitle("Permissão de Notificação")
                                .setMessage("Para receber alertas e notificações, permita notificações. Deseja permitir agora?")
                                .setPositiveButton("Permitir", (dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 200))
                                .setNegativeButton("Não", null)
                                .show();
                    } else {
                        // O sistema pode ter negado permanentemente ("Não perguntar novamente") ou desabilitado nas configs
                        Toast.makeText(this, "Ative as notificações nas configurações do app para receber alertas", Toast.LENGTH_LONG).show();
                        // Opcional: abrir as configurações do app
                        // Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        // intent.setData(Uri.parse("package:" + getPackageName()));
                        // startActivity(intent);
                    }
                }
            }

            // RESULTADO DO PEDIDO DE PERMISSÃO DE BACKGROUND LOCATION (REQUEST_CODE: BACKGROUND_LOCATION_PERMISSION_CODE)
            if (requestCode == BACKGROUND_LOCATION_PERMISSION_CODE) {
                boolean backgroundGranted = false;
                // Find the specific permission result for ACCESS_BACKGROUND_LOCATION if present
                for (int i = 0; i < permissions.length; i++) {
                    if (Manifest.permission.ACCESS_BACKGROUND_LOCATION.equals(permissions[i])) {
                        backgroundGranted = (i < grantResults.length && grantResults[i] == PackageManager.PERMISSION_GRANTED);
                        break;
                    }
                }

                if (!backgroundGranted && grantResults.length == 1 && permissions.length == 1 && Manifest.permission.ACCESS_BACKGROUND_LOCATION.equals(permissions[0])) {
                    backgroundGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                }

                if (backgroundGranted) {
                    Log.d("MainActivity", "Permissão de background location concedida");
                    // Marca que já mostramos/gerimos a verificação da permissão para não repetir a UI de orientação
                    try { getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("fgs_verification_shown", true).apply(); } catch (Exception ex) { /* ignore */ }
                    // Pequeno atraso para garantir que o sistema atualizou os estados de permissão antes de iniciar o serviço
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try {
                            startForegroundLocationIfNeeded();
                            try { getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("fgs_auto_attempted", true).apply(); } catch (Exception ex) { /* ignore */ }
                        } catch (Exception ex) {
                            Log.e("MainActivity", "Erro ao iniciar FGS após permissão de background: " + ex.getMessage(), ex);
                            Toast.makeText(MainActivity.this, "Erro ao iniciar localização em background", Toast.LENGTH_LONG).show();
                        }
                    }, 300);
                } else {
                    Log.d("MainActivity", "Permissão de background location negada");
                }
            }

            // RESULTADO DO PEDIDO PARA FOREGROUND_SERVICE_LOCATION (REQUEST_CODE: FOREGROUND_SERVICE_LOCATION_PERMISSION_CODE)
            if (requestCode == FOREGROUND_SERVICE_LOCATION_PERMISSION_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainActivity", "Permissão FOREGROUND_SERVICE_LOCATION concedida");
                    try { getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("fgs_verification_shown", true).apply(); } catch (Exception ex) { /* ignore */ }
                    try { getSharedPreferences("app_prefs", MODE_PRIVATE).edit().putBoolean("fgs_auto_attempted", true).apply(); } catch (Exception ex) { /* ignore */ }
                    startForegroundLocationIfNeeded();
                } else {
                    Log.d("MainActivity", "Permissão FOREGROUND_SERVICE_LOCATION negada");

                    // Se o utilizador negou permanentemente (não podemos pedir novamente), sugere abrir definições
                    String perm = "android.permission.FOREGROUND_SERVICE_LOCATION";
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                        new android.app.AlertDialog.Builder(this)
                                .setTitle("Permissão necessária")
                                .setMessage("A permissão para iniciar serviço em foreground de localização foi negada permanentemente. Deseja abrir as definições da app para permitir?")
                                .setPositiveButton("Abrir definições", (d, w) -> openAppSettings())
                                .setNegativeButton("Cancelar", null)
                                .show();
                    } else {
                        // Usuário negou, mas ainda podemos pedir novamente com explicação
                        new android.app.AlertDialog.Builder(this)
                                .setTitle("Permissão necessária")
                                .setMessage("Para enviar a sua localização em segundo plano, a app precisa desta permissão. Deseja tentar novamente?")
                                .setPositiveButton("Tentar", (d, w) -> requestForegroundServiceLocationPermissionWithRationale())
                                .setNegativeButton("Cancelar", null)
                                .show();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("MainActivity", "Erro em onRequestPermissionsResult: " + ex.getMessage(), ex);
        }
    }

    // Abre a página de definições da app para que o utilizador possa ajustar permissões manualmente
    private void openAppSettings() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao abrir definições da app: " + e.getMessage(), e);
            Toast.makeText(this, "Não foi possível abrir definições da app", Toast.LENGTH_SHORT).show();
        }
    }

    // Pede a permissão FOREGROUND_SERVICE_LOCATION com explicação se necessário
    private void requestForegroundServiceLocationPermissionWithRationale() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return; // only relevant on S+

        String perm = "android.permission.FOREGROUND_SERVICE_LOCATION";
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Permissão de Foreground Service Location")
                    .setMessage("Para enviar a sua localização em segundo plano, a app precisa da permissão específica para serviço foreground de localização. Deseja permitir agora?")
                    .setPositiveButton("Permitir", (d, w) -> ActivityCompat.requestPermissions(MainActivity.this, new String[]{perm}, FOREGROUND_SERVICE_LOCATION_PERMISSION_CODE))
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            // Pode ter sido negada permanentemente — convidar o utilizador a abrir definições
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Permissão necessária")
                    .setMessage("A permissão de serviço em foreground para localização parece estar negada permanentemente. Abra as definições da app para concedê-la?")
                    .setPositiveButton("Abrir definições", (d, w) -> openAppSettings())
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
    }

    private void converterCoordenadasParaEndereco(Location location) {
        if (location == null) {
            Log.e("MAPS_DEBUG", "Localização é nula");
            runOnUiThread(() -> tvLocation.setText("Localização não disponível"));
            return;
        }

        Log.d("MAPS_DEBUG", "Convertendo coordenadas: " + location.getLatitude() + ", " + location.getLongitude());

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1
                );

                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);

                        // Tenta obter atributos mais específicos, com vários fallbacks
                        String localidade = address.getLocality();
                        String subadmin = address.getSubAdminArea();
                        String admin = address.getAdminArea();
                        String feature = address.getFeatureName();
                        String enderecoLinha = address.getAddressLine(0);

                        Log.d("MAPS_DEBUG", "Geocoder returned address: " + address.toString());

                        String resolved = null;
                        if (localidade != null && !localidade.isEmpty()) resolved = localidade;
                        else if (subadmin != null && !subadmin.isEmpty()) resolved = subadmin;
                        else if (admin != null && !admin.isEmpty()) resolved = admin;
                        else if (feature != null && !feature.isEmpty()) resolved = feature;
                        else if (enderecoLinha != null && !enderecoLinha.isEmpty()) resolved = enderecoLinha;

                        if (resolved != null) {
                            tvLocation.setText(resolved);
                            Log.d("MAPS_DEBUG", "Localidade definida (fallbacks aplicados): " + resolved);
                        } else {
                            tvLocation.setText("Localização não disponível");
                            Log.d("MAPS_DEBUG", "Usando fallback: Localização não disponível");
                        }
                    } else {
                        tvLocation.setText("Localização não disponível");
                        Log.d("MAPS_DEBUG", "Nenhum endereço encontrado");
                    }
                });

            } catch (IOException e) {
                Log.e("MAPS_DEBUG", "Erro Geocoder: " + e.getMessage());
                runOnUiThread(() -> tvLocation.setText("Localização não disponível"));
            } catch (Exception e) {
                Log.e("MAPS_DEBUG", "Erro inesperado: " + e.getMessage());
                runOnUiThread(() -> tvLocation.setText("Localização não disponível"));
            }
        }).start();
    }



    private boolean isGPSEnabled() {
        try {
            android.location.LocationManager locationManager =
                    (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.e("MAPS_DEBUG", "Erro ao verificar GPS: " + e.getMessage());
            return false;
        }
    }

    private void mostrarDialogGPS() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("GPS Desativado")
                .setMessage("Para uma melhor experiência, ative o GPS para ver anúncios na sua localização atual.")
                .setPositiveButton("Ativar GPS", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, REQUEST_ENABLE_GPS);
                })
                .setNegativeButton("Continuar Sem GPS", (dialog, which) -> {
                    tvLocation.setText("GPS desativado");
                    setupListaAnuncios();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDITAR_ANUNCIO && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("anuncio_editado")) {
                Anuncio anuncioEditado = data.getParcelableExtra("anuncio_editado");
                int position = data.getIntExtra("position", -1);

                if (anuncioEditado != null && position != -1 && position < listaAnuncios.size()) {
                    // Atualiza o anúncio na lista
                    listaAnuncios.set(position, anuncioEditado);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Anúncio atualizado!", Toast.LENGTH_SHORT).show();
                } else {
                    // Se algo deu errado, recarrega toda a lista
                    carregarAnuncios();
                }
            }
        }

        if (requestCode == REQUEST_ENABLE_GPS) {
            // Usuário voltou das configurações de GPS
            if (isGPSEnabled()) {
                Log.d("MAPS_DEBUG", "GPS ativado pelo usuário, obtendo localização...");
                gpsAtivado = true;
                // Pequeno delay para garantir que o GPS está realmente ativo
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    obterLocalizacaoAtual();
                }, 1000);
            } else {
                Log.d("MAPS_DEBUG", "Usuário não ativou o GPS");
                tvLocation.setText("GPS desativado");
            }
        }

        if (requestCode == AdicionarAnunciosActivity.REQUEST_CODE_EDIT  // Ou o código que usas para adicionar
                && resultCode == RESULT_OK
                && data != null
                && data.getBooleanExtra("anuncio_criado", false)) {

            Log.d("MainActivity", "Novo anúncio criado – atualizando lista");
            Long userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getLong("userId", -1);
            if (userId != -1) {
                carregarAnuncios(); // Reaproveita o teu método existente para recarregar a lista
                Toast.makeText(this, "Lista atualizada!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Verifica se o GPS foi ativado enquanto a app estava em segundo plano
        boolean gpsAgoraAtivo = isGPSEnabled();
        if (!gpsAtivado && gpsAgoraAtivo) {
            Log.d("MAPS_DEBUG", "GPS ativado durante onResume, obtendo localização...");
            gpsAtivado = true;
            obterLocalizacaoAtual();
        } else if (gpsAtivado && !gpsAgoraAtivo) {
            Log.d("MAPS_DEBUG", "GPS desativado durante onResume");
            gpsAtivado = false;
            tvLocation.setText("GPS desativado");
        }

        // Verifica se precisamos tentar obter localização novamente
        verificarEstadoLocalizacao();
        carregarContagemNotificacoes();

        // Garantir que enquanto a app está em foreground usamos o serviço leve (handler-based)
        try {
            startService(new Intent(this, LocationUpdateService.class));
            stopForegroundLocationService();
        } catch (Exception e) {
            Log.e("MainActivity", "Erro no onResume ao gerir serviços de localização: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Para as atualizações de localização quando a activity não está visível
        try {
            if (locationCallback != null) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }
            if (locationTimeoutHandler != null) {
                locationTimeoutHandler.removeCallbacksAndMessages(null);
            }

            // Quando a app vai para background, parar o serviço leve e, se tivermos permissão, iniciar o foreground service
            stopLocationUpdateService();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Também pedir permissão de FGS em S+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checkSelfPermission("android.permission.FOREGROUND_SERVICE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                    // Não temos a permissão exigida — pede ao utilizador de forma adequada
                    Log.d("MainActivity", "FOREGROUND_SERVICE_LOCATION ausente — solicitando permissão ou orientando o utilizador");
                    // Tenta pedir com razão / abre definições se for caso de 'não perguntar novamente'
                    requestForegroundServiceLocationPermissionWithRationale();
                } else {
                    startForegroundLocationIfNeeded();
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Erro no onPause ao gerir serviços de localização: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpa recursos relacionados com localização
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        if (locationTimeoutHandler != null) {
            locationTimeoutHandler.removeCallbacksAndMessages(null);
        }
        // Unregister receivers
        try {
            unregisterReceiver(notifCountReceiver);
        } catch (IllegalArgumentException e) {
            // Já estava registado ou removido
        }
        try {
            unregisterReceiver(fgsFailReceiver);
        } catch (IllegalArgumentException e) {
            // Já estava registado ou removido
        }
    }

    private void verificarEstadoLocalizacao() {
        String textoAtual = tvLocation.getText().toString();

        // Se a localização atual não foi obtida ou está com mensagem de erro, tenta novamente
        if (textoAtual.equals("GPS desativado") ||
                textoAtual.equals("Localização não disponível") ||
                textoAtual.equals("Erro ao obter localização") ||
                textoAtual.equals("Erro de permissão") ||
                textoAtual.equals("Obtendo localização...") ||
                textoAtual.equals("Localização atual")) {

            if (isGPSEnabled() && temPermissaoLocalizacao()) {
                Log.d("MAPS_DEBUG", "Tentando obter localização novamente em onResume...");
                obterLocalizacaoAtual();
            }
        }
    }

    private boolean temPermissaoLocalizacao() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void carregarAnuncios() {
        Long userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getLong("userId", -1);
        if (userId == -1) return;

        // Inicializa o adapter com o listener de ações - MUDAR false para true
        rvAnunciosMain.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAnuncioAdapter(this, listaAnuncios, true, new MainAnuncioAdapter.OnActionClickListener() { // ← MUDAR AQUI
            @Override
            public void onEditClick(Anuncio anuncio, int position) {
                Log.d("MAIN_EDIT", "=== EDITANDO ANÚNCIO ===");
                Log.d("MAIN_EDIT", "Título: " + anuncio.titulo);
                Log.d("MAIN_EDIT", "ID: " + anuncio.id);
                Log.d("MAIN_EDIT", "Posição: " + position);

                Intent intent = new Intent(MainActivity.this, AdicionarAnunciosActivity.class);

                // Use letras minúsculas para consistência
                intent.putExtra("ANUNCIO_PARA_EDITAR", anuncio);  // Parcelable!
                intent.putExtra("POSICAO", position);
                startActivityForResult(intent, REQUEST_CODE_EDITAR_ANUNCIO);
            }

            @Override
            public void onDeleteClick(Anuncio anuncio, int position) {
                // CHAMA O MÉTODO PARA ELIMINAR O ANÚNCIO
                eliminarAnuncio(anuncio, position);
            }

            @Override
            public void onSaveClick(Anuncio anuncio, int position) {
                // Implementar salvar/guardar se necessário
                Toast.makeText(MainActivity.this, "Guardar: " + anuncio.titulo, Toast.LENGTH_SHORT).show();
            }
        });
        rvAnunciosMain.setAdapter(adapter);

        // Resto do código para carregar anúncios...
        Call<List<AnuncioResponse>> call = RetrofitClient.getApiService(this).getMeusAnuncios(userId);
        call.enqueue(new Callback<List<AnuncioResponse>>() {
            @Override
            public void onResponse(Call<List<AnuncioResponse>> call, Response<List<AnuncioResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaAnuncios.clear();

                    for (AnuncioResponse responseItem : response.body()) {
                        Anuncio anuncio = responseItem.toAnuncio();
                        listaAnuncios.add(anuncio);
                    }

                    adapter.notifyDataSetChanged();
                    atualizarVisibilidade();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao carregar anúncios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnuncioResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void eliminarAnuncio(Anuncio anuncio, int position) {
        if (anuncio == null || anuncio.id == null) {
            Log.e("ELIMINAR_ANUNCIO", "ERRO: Anúncio nulo ou sem ID!");
            Toast.makeText(this, "Erro: Anúncio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            Toast.makeText(this, "Erro: Utilizador não logado", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ELIMINAR_ANUNCIO", "=== INICIANDO ELIMINAÇÃO ===");
        Log.d("ELIMINAR_ANUNCIO", "Título: " + anuncio.titulo);
        Log.d("ELIMINAR_ANUNCIO", "ID do anúncio: " + anuncio.id);
        Log.d("ELIMINAR_ANUNCIO", "Posição na lista: " + position);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Anúncio")
                .setMessage("Tem certeza que deseja eliminar o anúncio \"" + anuncio.titulo + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    Log.d("ELIMINAR_ANUNCIO", "Usuário confirmou eliminação. Chamando API...");

                    Call<Void> call = RetrofitClient.getApiService(this).eliminarAnuncio(anuncio.id, userId);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            Log.d("ELIMINAR_ANUNCIO", "Resposta do servidor recebida. Código HTTP: " + response.code());

                            if (response.isSuccessful()) {
                                Log.d("ELIMINAR_ANUNCIO", "SUCESSO! Anúncio eliminado no servidor.");

                                listaAnuncios.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, listaAnuncios.size());

                                Toast.makeText(MainActivity.this, "Anúncio eliminado com sucesso!", Toast.LENGTH_LONG).show();
                                atualizarVisibilidade(); // ou atualizarVisibilidadeListaVazia()

                                Log.d("ELIMINAR_ANUNCIO", "UI atualizada. Anúncio removido da posição " + position);
                            } else {
                                Log.e("ELIMINAR_ANUNCIO", "FALHA no servidor! Código: " + response.code());
                                Log.e("ELIMINAR_ANUNCIO", "Mensagem: " + response.message());
                                Toast.makeText(MainActivity.this,
                                        "Erro do servidor: " + response.code() + " " + response.message(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("ELIMINAR_ANUNCIO", "FALHA TOTAL: Sem ligação ou erro de rede");
                            Log.e("ELIMINAR_ANUNCIO", "Erro: " + t.getClass().getSimpleName() + " - " + t.getMessage());
                            t.printStackTrace();

                            Toast.makeText(MainActivity.this,
                                    "Sem ligação à internet ou servidor offline", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    Log.d("ELIMINAR_ANUNCIO", "Usuário cancelou a eliminação");
                })
                .show();
    }


    private void carregarContagemNotificacoes() {
        Long userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getLong("userId", -1);
        if (userId == -1) {
            Log.e("NOTIFICACOES", "UserId não encontrado");
            return;
        }

        Log.d("NOTIFICACOES", "Carregando contagem para userId: " + userId);

        Call<Integer> call = RetrofitClient.getApiService(this).getContagemNotificacoes(userId);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                Log.d("NOTIFICACOES", "Resposta recebida. Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    int contagem = response.body();
                    Log.d("NOTIFICACOES", "Contagem de notificações: " + contagem);
                    atualizarBadgeNotificacao(contagem);
                } else {
                    Log.e("NOTIFICACOES", "Resposta não sucedida. Código: " + response.code());
                    atualizarBadgeNotificacao(0);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e("NOTIFICACOES", "Erro de rede: " + t.getMessage());
                atualizarBadgeNotificacao(0);
            }
        });
    }

    private void atualizarBadgeNotificacao(int contagem) {
        runOnUiThread(() -> {
            Log.d("NOTIFICACOES", "Atualizando badge com: " + contagem);

            if (contagem > 0) {
                badgeNotificacao.setText(String.valueOf(contagem));
                badgeNotificacao.setVisibility(View.VISIBLE);
            } else {
                // Mostra "0" em vez de esconder
                badgeNotificacao.setText("0");
                badgeNotificacao.setVisibility(View.VISIBLE);
            }
        });
    }

    private void iniciarLocationService() {
        // Inicio o serviço de atualização leve (usa getLastLocation periodicamente)
        startService(new Intent(this, LocationUpdateService.class));
        Log.d("MainActivity", "LocationUpdateService iniciado");

        // Se tivermos permissão de background, iniciar o serviço em foreground para garantir atualizações contínuas
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startForegroundLocationIfNeeded();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao tentar iniciar ForegroundLocationService: " + e.getMessage(), e);
        }
    }

    private void startForegroundLocationIfNeeded() {
        try {
            // Pre-check: ensure we have both the foreground-service-location and a location permission on S+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                boolean hasFgs = checkSelfPermission("android.permission.FOREGROUND_SERVICE_LOCATION") == PackageManager.PERMISSION_GRANTED;
                boolean hasLocation = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (!hasFgs || !hasLocation) {
                    Log.w("MainActivity", "Não tem permissão necessária para iniciar FGS (fgs=" + hasFgs + ", loc=" + hasLocation + ")");
                    // Tenta solicitar permissões se possível
                    if (!hasFgs) {
                        ActivityCompat.requestPermissions(this, new String[]{"android.permission.FOREGROUND_SERVICE_LOCATION"}, FOREGROUND_SERVICE_LOCATION_PERMISSION_CODE);
                    }
                    if (!hasLocation) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    }
                    return;
                }

                // Extra heuristic: check AppOps for foreground-service-location if available to avoid OS-level denial
                try {
                    android.app.AppOpsManager aom = (android.app.AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                    if (aom != null) {
                        // OPSTR_FOREGROUND_SERVICE_LOCATION exists on newer APIs — obtain it reflectively so code compiles on older compileSdk
                        String op;
                        try {
                            java.lang.reflect.Field f = android.app.AppOpsManager.class.getField("OPSTR_FOREGROUND_SERVICE_LOCATION");
                            op = (String) f.get(null);
                        } catch (Throwable e) {
                            // Fallback string used by AppOps internally
                            op = "android:foreground_service_location";
                        }
                        int mode = aom.checkOpNoThrow(op, android.os.Process.myUid(), getPackageName());
                        if (mode != android.app.AppOpsManager.MODE_ALLOWED) {
                            Log.w("MainActivity", "AppOps forbids starting FGS (mode=" + mode + ") — skipping start");
                            Toast.makeText(this, "Não foi possível ativar localização em segundo plano: verifique configurações do dispositivo", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                } catch (Throwable t) {
                    // AppOps check failed — if permissions are already granted, try to start the FGS directly (best-effort);
                    // only show the dialog if the user still needs to grant permissions.
                    Log.d("MainActivity", "AppOps check not available: " + t.getMessage());

                    boolean hasFgsPerm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checkSelfPermission("android.permission.FOREGROUND_SERVICE_LOCATION") == PackageManager.PERMISSION_GRANTED;
                    boolean hasLocationPerm = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

                    if (hasFgsPerm && hasLocationPerm) {
                        final SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                        boolean autoAttempted = prefs.getBoolean("fgs_auto_attempted", false);
                        boolean fgRunning = prefs.getBoolean("fgLocationServiceRunning", false);
                        boolean currentPermsAll = hasFgsPerm && hasLocationPerm;

                        if (autoAttempted) {
                            if (fgRunning) {
                                Log.d("MainActivity", "FGS already running; skipping automatic attempt.");
                                return;
                            }
                            if (!currentPermsAll) {
                                Log.d("MainActivity", "FGS automatic attempt already done; skipping (permissions not satisfied).");
                                return;
                            }
                            Log.d("MainActivity", "FGS automatic attempt done previously but permissions now satisfied; proceeding to attempt again.");
                        }

                        // Permissions present — attempt to start the FGS and inform the user we couldn't verify AppOps
                        Toast.makeText(this, "Não foi possível verificar configurações do sistema; tentando ativar localização em 2º plano...", Toast.LENGTH_LONG).show();
                        Intent fg = new Intent(this, ao.co.isptec.aplm.projetoanuncioloc.Service.ForegroundLocationService.class);
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                ContextCompat.startForegroundService(this, fg);
                            } else {
                                startService(fg);
                            }
                            Log.d("MainActivity", "ForegroundLocationService solicitado após falha AppOps (tentativa automática)");
                            // Mark that we attempted an automatic activation and record whether permissions were all present
                            prefs.edit().putBoolean("fgs_auto_attempted", true).putBoolean("fgs_auto_attempted_perms_all_granted", currentPermsAll).apply();
                            Log.d("MainActivity", "fgs_auto_attempted set=true, permsAll=" + currentPermsAll + " at " + System.currentTimeMillis());
                        } catch (SecurityException se) {
                            Log.e("MainActivity", "SecurityException ao tentar iniciar FGS após falha AppOps: " + se.getMessage(), se);
                            // Fall back to asking the user explicitly
                            new android.app.AlertDialog.Builder(this)
                                    .setTitle("Não foi possível ativar")
                                    .setMessage("Não foi possível iniciar a localização em segundo plano devido a restrições do sistema. Deseja tentar novamente e ajustar permissões?")
                                    .setPositiveButton("Tentar", (dialog, which) -> {
                                        // Record that user chose to try so we won't ask again
                                        prefs.edit().putBoolean("fgs_verification_shown", true).apply();

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 200);
                                            }
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            if (checkSelfPermission("android.permission.FOREGROUND_SERVICE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                                                ActivityCompat.requestPermissions(this, new String[]{"android.permission.FOREGROUND_SERVICE_LOCATION"}, FOREGROUND_SERVICE_LOCATION_PERMISSION_CODE);
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancelar", null)
                                    .show();
                        }
                        return;
                    }

                    // Caso contrário, pedimos ao utilizador para tentar e pedimos as permissões necessárias
                    final SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    if (prefs.getBoolean("fgs_verification_shown", false)) {
                        Log.d("MainActivity", "Skipping system verification dialog (already attempted).");
                        return;
                    }

                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Verificação de sistema não disponível")
                            .setMessage("Não foi possível verificar se o sistema permite a localização em segundo plano. Deseja tentar ativar mesmo assim? Pode não funcionar em alguns dispositivos.")
                            .setPositiveButton("Tentar", (dialog, which) -> {
                                // Record that the user chose to try so we won't ask again
                                prefs.edit().putBoolean("fgs_verification_shown", true).apply();

                                // Request notification permission (Android 13+)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 200);
                                    }
                                }

                                // Request FGS-specific permission on S+
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    if (checkSelfPermission("android.permission.FOREGROUND_SERVICE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(this, new String[]{"android.permission.FOREGROUND_SERVICE_LOCATION"}, FOREGROUND_SERVICE_LOCATION_PERMISSION_CODE);
                                    }
                                }

                                // Attempt to start the foreground service anyway; ForegroundLocationService will handle failures gracefully
                                Intent fg = new Intent(this, ao.co.isptec.aplm.projetoanuncioloc.Service.ForegroundLocationService.class);
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        ContextCompat.startForegroundService(this, fg);
                                    } else {
                                        startService(fg);
                                    }
                                    Log.d("MainActivity", "ForegroundLocationService solicitado (forçado)");
                                    // Record that we forced an attempt and capture current permission state
                                    try {
                                        SharedPreferences prefsLocal = getSharedPreferences("app_prefs", MODE_PRIVATE);
                                        boolean hasFgsPermLocal = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checkSelfPermission("android.permission.FOREGROUND_SERVICE_LOCATION") == PackageManager.PERMISSION_GRANTED;
                                        boolean hasLocPermLocal = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                                        prefsLocal.edit().putBoolean("fgs_auto_attempted", true).putBoolean("fgs_auto_attempted_perms_all_granted", hasFgsPermLocal && hasLocPermLocal).apply();
                                        Log.d("MainActivity", "fgs_auto_attempted set=true (forced), permsAll=" + (hasFgsPermLocal && hasLocPermLocal) + " at " + System.currentTimeMillis());
                                    } catch (Exception ex) { /* ignore */ }
                                } catch (SecurityException se) {
                                    Log.e("MainActivity", "SecurityException ao tentar iniciar FGS forçado: " + se.getMessage(), se);
                                    Toast.makeText(this, "Não foi possível iniciar localização em background: conceda permissão de localização em segundo plano", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                    return;
                }
            }

            Intent fg = new Intent(this, ao.co.isptec.aplm.projetoanuncioloc.Service.ForegroundLocationService.class);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(this, fg);
                } else {
                    startService(fg);
                }
                Log.d("MainActivity", "ForegroundLocationService solicitado");
                // Record automatic attempt state and current permission snapshot
                try {
                    SharedPreferences prefsLocal = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    boolean hasFgsPermLocal = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checkSelfPermission("android.permission.FOREGROUND_SERVICE_LOCATION") == PackageManager.PERMISSION_GRANTED;
                    boolean hasLocPermLocal = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                    prefsLocal.edit().putBoolean("fgs_auto_attempted", true).putBoolean("fgs_auto_attempted_perms_all_granted", hasFgsPermLocal && hasLocPermLocal).apply();
                    Log.d("MainActivity", "fgs_auto_attempted set=true (auto), permsAll=" + (hasFgsPermLocal && hasLocPermLocal) + " at " + System.currentTimeMillis());
                } catch (Exception ex) { /* ignore */ }
            } catch (SecurityException se) {
                Log.e("MainActivity", "SecurityException ao tentar iniciar FGS: " + se.getMessage(), se);
                Toast.makeText(this, "Não foi possível iniciar localização em background: conceda permissão de localização em segundo plano", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Falha ao iniciar ForegroundLocationService: " + e.getMessage(), e);
        }
    }

    private void stopForegroundLocationService() {
        try {
            Intent fg = new Intent(this, ao.co.isptec.aplm.projetoanuncioloc.Service.ForegroundLocationService.class);
            stopService(fg);
            Log.d("MainActivity", "ForegroundLocationService parado");
        } catch (Exception e) {
            Log.e("MainActivity", "Falha ao parar ForegroundLocationService: " + e.getMessage(), e);
        }
    }

    private void stopLocationUpdateService() {
        try {
            Intent i = new Intent(this, LocationUpdateService.class);
            stopService(i);
            Log.d("MainActivity", "LocationUpdateService parado");
        } catch (Exception e) {
            Log.e("MainActivity", "Falha ao parar LocationUpdateService: " + e.getMessage(), e);
        }
    }




}