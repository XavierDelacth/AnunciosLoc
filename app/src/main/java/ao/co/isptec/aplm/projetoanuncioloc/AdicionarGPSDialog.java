package ao.co.isptec.aplm.projetoanuncioloc;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.List;
import java.util.Locale;

import ao.co.isptec.aplm.projetoanuncioloc.Interface.OnLocalAddedListener;  // Import da interface comum

public class AdicionarGPSDialog extends DialogFragment implements OnMapReadyCallback {

    private GoogleMap mapa;
    private FusedLocationProviderClient clienteLocalizacao;
    private static final int REQ_LOCALIZACAO = 100;

    private EditText etNomeLocal, etLatitude, etLongitude, etRaio;
    private Button btnCancelar, btnAdicionar, btnWifi;  // btnWifi para alternar
    private ImageView btnFechar;
    private Switch switchMapear;

    // Usa a interface comum
    private OnLocalAddedListener listener;

    // Factory method para newInstance
    public static AdicionarGPSDialog newInstance(OnLocalAddedListener listener) {
        AdicionarGPSDialog dialog = new AdicionarGPSDialog();
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (OnLocalAddedListener) context;  // Cast seguro para a interface comum
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clienteLocalizacao = LocationServices.getFusedLocationProviderClient(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_gps, container, false);
        initViews(view);
        setupClickListeners(view);
        return view;
    }

    private void initViews(View view) {
        etNomeLocal = view.findViewById(R.id.etNomeLocal);
        etLatitude = view.findViewById(R.id.etLatitude);
        etLongitude = view.findViewById(R.id.etLongitude);
        etRaio = view.findViewById(R.id.etRaio);
        btnCancelar = view.findViewById(R.id.btnCancelar);
        btnAdicionar = view.findViewById(R.id.btnAdicionar);
        btnWifi = view.findViewById(R.id.btnWifi);  // Certifica-te de que o ID existe no XML
        btnFechar = view.findViewById(R.id.btnFechar);
        switchMapear = view.findViewById(R.id.switchMapear);

        // Inicializa mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragmentoMapa);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupClickListeners(View view) {
        btnFechar.setOnClickListener(v -> dismiss());
        btnCancelar.setOnClickListener(v -> dismiss());

        // CORREÇÃO PRINCIPAL: Clique no btnWifi abre o WiFi Dialog (sem cast, usa interface comum)
        if (btnWifi != null) {
            btnWifi.setOnClickListener(v -> {
                dismiss();  // Fecha este diálogo GPS
                // Passa o mesmo listener comum (sem cast, tipos compatíveis agora)
                AdicionarWIFIDialog wifiDialog = AdicionarWIFIDialog.newInstance(listener);
                wifiDialog.show(getParentFragmentManager(), "AdicionarWIFIDialog");
            });
        }

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

                // Callback com método GPS
                if (listener != null) {
                    listener.onLocalAddedGPS(nome, lat, lng, raio);
                }
                Toast.makeText(requireContext(), "Local GPS adicionado!", Toast.LENGTH_SHORT).show();
                dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Verifique os valores numéricos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;
        mapa.getUiSettings().setZoomControlsEnabled(true);
        mapa.getUiSettings().setMyLocationButtonEnabled(true);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacao();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCALIZACAO);
        }

        mapa.setOnMapClickListener(latLng -> {
            mapa.clear();
            mapa.addMarker(new MarkerOptions().position(latLng).title("Selecionado"));
            etLatitude.setText(String.format(Locale.US, "%.6f", latLng.latitude));
            etLongitude.setText(String.format(Locale.US, "%.6f", latLng.longitude));
        });
    }

    private void ativarLocalizacao() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        mapa.setMyLocationEnabled(true);
        clienteLocalizacao.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng atual = new LatLng(location.getLatitude(), location.getLongitude());
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(atual, 15));
                etLatitude.setText(String.format(Locale.US, "%.6f", atual.latitude));
                etLongitude.setText(String.format(Locale.US, "%.6f", atual.longitude));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCALIZACAO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacao();
        }
    }

    // Back button fecha o diálogo
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (clienteLocalizacao != null) {
            clienteLocalizacao = null;
        }
    }
}