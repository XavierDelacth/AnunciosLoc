package ao.co.isptec.aplm.projetoanuncioloc;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

import ao.co.isptec.aplm.projetoanuncioloc.Interface.OnLocalAddedListener;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;
import ao.co.isptec.aplm.projetoanuncioloc.Request.LocalRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdicionarGPSDialog extends DialogFragment implements OnMapReadyCallback {

    private static final String TAG = "AdicionarGPSDialog";
    private GoogleMap mapa;
    private FusedLocationProviderClient clienteLocalizacao;
    private static final int REQ_LOCALIZACAO = 100;

    private EditText etNomeLocal, etLatitude, etLongitude, etRaio;
    private Button btnCancelar, btnAdicionar, btnWifi;
    private ImageView btnFechar;
    private Switch switchMapear;
    private View mapaContainer;

    private boolean camposBloqueados = false;
    private boolean mapaInicializado = false;
    private OnLocalAddedListener listener;

    public static AdicionarGPSDialog newInstance(OnLocalAddedListener listener) {
        AdicionarGPSDialog dialog = new AdicionarGPSDialog();
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (OnLocalAddedListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clienteLocalizacao = LocationServices.getFusedLocationProviderClient(requireContext());
        // Remove full screen style - pode causar problemas com o mapa
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_gps, container, false);

        // Remove título do dialog se existir
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        initViews(view);
        setupClickListeners(view);

        Log.d(TAG, "onCreateView: View criada");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ DELAY para garantir que o layout está pronto
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            inicializarMapa();
        }, 300);
    }

    private void initViews(View view) {
        etNomeLocal = view.findViewById(R.id.etNomeLocal);
        etLatitude = view.findViewById(R.id.etLatitude);
        etLongitude = view.findViewById(R.id.etLongitude);
        etRaio = view.findViewById(R.id.etRaio);
        btnCancelar = view.findViewById(R.id.btnCancelar);
        btnAdicionar = view.findViewById(R.id.btnAdicionar);
        btnWifi = view.findViewById(R.id.btnWifi);
        btnFechar = view.findViewById(R.id.btnFechar);
        switchMapear = view.findViewById(R.id.switchMapear);
        mapaContainer = view.findViewById(R.id.fragmentoMapa);

        Log.d(TAG, "initViews: Views inicializadas");
    }

    /**
     * ✅ MÉTODO CRÍTICO: Inicializa o mapa com tratamento robusto
     */
    private void inicializarMapa() {
        try {
            // Tenta obter fragmento existente
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.fragmentoMapa);

            if (mapFragment == null) {
                Log.d(TAG, "Criando novo SupportMapFragment");
                mapFragment = SupportMapFragment.newInstance();

                getChildFragmentManager().beginTransaction()
                        .replace(R.id.fragmentoMapa, mapFragment)
                        .commitAllowingStateLoss();

                // Aguarda um pouco antes de obter o mapa
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager()
                            .findFragmentById(R.id.fragmentoMapa);
                    if (fragment != null) {
                        fragment.getMapAsync(this);
                        Log.d(TAG, "getMapAsync chamado após criação do fragmento");
                    }
                }, 500);
            } else {
                Log.d(TAG, "Fragmento de mapa já existe");
                mapFragment.getMapAsync(this);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar mapa: " + e.getMessage(), e);
            Toast.makeText(requireContext(),
                    "Erro ao carregar mapa. Verifique sua conexão.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners(View view) {
        btnFechar.setOnClickListener(v -> dismiss());
        btnCancelar.setOnClickListener(v -> dismiss());

        if (btnWifi != null) {
            btnWifi.setOnClickListener(v -> {
                dismiss();
                AdicionarWIFIDialog wifiDialog = AdicionarWIFIDialog.newInstance(listener);
                wifiDialog.show(getParentFragmentManager(), "AdicionarWIFIDialog");
            });
        }

        switchMapear.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                desbloquearCamposCoordenadas();
                Toast.makeText(requireContext(),
                        "Toque no mapa para selecionar nova localização",
                        Toast.LENGTH_SHORT).show();
            } else {
                if (!etLatitude.getText().toString().trim().isEmpty()) {
                    bloquearCamposCoordenadas();
                }
            }
        });

        btnAdicionar.setOnClickListener(v -> {
            String nome = etNomeLocal.getText().toString().trim();
            String latStr = etLatitude.getText().toString().trim();
            String lngStr = etLongitude.getText().toString().trim();
            String raioStr = etRaio.getText().toString().trim();

            if (nome.isEmpty() || latStr.isEmpty() || lngStr.isEmpty() || raioStr.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double lat = Double.parseDouble(latStr);
                double lng = Double.parseDouble(lngStr);
                int raio = Integer.parseInt(raioStr);

                SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                Long userId = prefs.getLong("userId", -1L);
                if (userId == -1L) {
                    Toast.makeText(requireContext(), "Erro: Faça login novamente", Toast.LENGTH_LONG).show();
                    return;
                }

                LocalRequest request = new LocalRequest(nome, "GPS", lat, lng, raio, null);

                Call<Local> call = RetrofitClient.getApiService(requireContext()).criarLocal(request, userId);
                call.enqueue(new Callback<Local>() {
                    @Override
                    public void onResponse(Call<Local> call, Response<Local> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(), "Local GPS adicionado com sucesso!", Toast.LENGTH_LONG).show();

                            if (listener != null) {
                                listener.onLocalAddedGPS(nome, lat, lng, raio);
                            }
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Erro no servidor: " + response.message(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Local> call, Throwable t) {
                        Toast.makeText(requireContext(), "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Verifique os valores numéricos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Mapa pronto!");
        mapa = googleMap;
        mapaInicializado = true;

        try {
            // Configurações básicas do mapa
            mapa.getUiSettings().setZoomControlsEnabled(true);
            mapa.getUiSettings().setMyLocationButtonEnabled(false); // Desabilitado inicialmente
            mapa.getUiSettings().setMapToolbarEnabled(false);
            mapa.getUiSettings().setCompassEnabled(true);
            mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            // Define posição padrão (Luanda) caso GPS falhe
            LatLng luanda = new LatLng(-8.8383, 13.2344);
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(luanda, 12));

            // Verifica permissões
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissão concedida, ativando localização");
                ativarLocalizacao();
            } else {
                Log.d(TAG, "Solicitando permissão de localização");
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCALIZACAO);
            }

            // Listener de clique no mapa
            mapa.setOnMapClickListener(latLng -> {
                if (!camposBloqueados) {
                    Log.d(TAG, "Clique no mapa: " + latLng.latitude + ", " + latLng.longitude);
                    mapa.clear();
                    mapa.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Localização Selecionada"));
                    etLatitude.setText(String.format(Locale.US, "%.6f", latLng.latitude));
                    etLongitude.setText(String.format(Locale.US, "%.6f", latLng.longitude));
                } else {
                    Toast.makeText(requireContext(),
                            "Coordenadas bloqueadas. Ative o Switch para alterar.",
                            Toast.LENGTH_SHORT).show();
                }
            });

            Log.d(TAG, "Mapa configurado com sucesso");

        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar mapa: " + e.getMessage(), e);
        }
    }

    private void ativarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Sem permissão para acessar localização");
            return;
        }

        try {
            if (mapa != null) {
                mapa.setMyLocationEnabled(true);
                mapa.getUiSettings().setMyLocationButtonEnabled(true);
            }

            Log.d(TAG, "Obtendo última localização conhecida...");
            clienteLocalizacao.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    Log.d(TAG, "Localização obtida: " + location.getLatitude() + ", " + location.getLongitude());

                    LatLng atual = new LatLng(location.getLatitude(), location.getLongitude());

                    if (mapa != null) {
                        // Limpa marcadores anteriores
                        mapa.clear();

                        // Adiciona marcador
                        mapa.addMarker(new MarkerOptions()
                                .position(atual)
                                .title("Sua Localização Atual"));

                        // Move câmera com animação
                        mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(atual, 16), 1500, null);
                    }

                    // Preenche campos
                    etLatitude.setText(String.format(Locale.US, "%.6f", atual.latitude));
                    etLongitude.setText(String.format(Locale.US, "%.6f", atual.longitude));

                    // Bloqueia campos
                    bloquearCamposCoordenadas();

                } else {
                    Log.w(TAG, "Localização é null");
                    Toast.makeText(requireContext(),
                            "Não foi possível obter sua localização. Toque no mapa para selecionar.",
                            Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Erro ao obter localização: " + e.getMessage(), e);
                Toast.makeText(requireContext(),
                        "Erro ao obter localização: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage(), e);
            Toast.makeText(requireContext(),
                    "Permissão de localização necessária",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void bloquearCamposCoordenadas() {
        etLatitude.setEnabled(false);
        etLongitude.setEnabled(false);
        etLatitude.setFocusable(false);
        etLongitude.setFocusable(false);
        etLatitude.setAlpha(0.6f);
        etLongitude.setAlpha(0.6f);
        camposBloqueados = true;
        switchMapear.setChecked(false);

        Log.d(TAG, "Campos de coordenadas bloqueados");
    }

    private void desbloquearCamposCoordenadas() {
        etLatitude.setEnabled(true);
        etLongitude.setEnabled(true);
        etLatitude.setFocusableInTouchMode(true);
        etLongitude.setFocusableInTouchMode(true);
        etLatitude.setAlpha(1.0f);
        etLongitude.setAlpha(1.0f);
        camposBloqueados = false;

        Log.d(TAG, "Campos de coordenadas desbloqueados");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: requestCode=" + requestCode);

        if (requestCode == REQ_LOCALIZACAO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissão concedida pelo usuário");
                if (mapa != null) {
                    ativarLocalizacao();
                }
            } else {
                Log.d(TAG, "Permissão negada pelo usuário");
                Toast.makeText(requireContext(),
                        "Permissão necessária para obter localização automática. Toque no mapa para selecionar.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Garante que o dialog ocupe a tela adequadamente
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Dialog resumido");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Limpando recursos");

        // Limpa o mapa
        if (mapa != null) {
            mapa.clear();
            mapa = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Dialog destruído");

        if (clienteLocalizacao != null) {
            clienteLocalizacao = null;
        }
    }
}