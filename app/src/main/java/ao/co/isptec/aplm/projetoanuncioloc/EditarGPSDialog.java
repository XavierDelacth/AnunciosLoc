package ao.co.isptec.aplm.projetoanuncioloc;

import android.Manifest;
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
import java.util.Locale;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;

public class EditarGPSDialog extends DialogFragment implements OnMapReadyCallback {

    private GoogleMap mapa;
    private FusedLocationProviderClient clienteLocalizacao;
    private static final int REQ_LOCALIZACAO = 100;

    private EditText etNomeLocal, etLatitude, etLongitude, etRaio;
    private Button btnCancelar, btnSalvar;
    private ImageView btnFechar;
    private Switch switchMapear;

    // Dados do local a editar
    private Local localParaEditar;

    // Callback para retornar dados editados
    public interface OnLocalEditadoListener {
        void onLocalEditado(String nome, double lat, double lng, int raio);
    }

    private OnLocalEditadoListener listener;

    public static EditarGPSDialog newInstance(Local local, OnLocalEditadoListener listener) {
        EditarGPSDialog dialog = new EditarGPSDialog();
        dialog.localParaEditar = local;
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clienteLocalizacao = LocationServices.getFusedLocationProviderClient(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_gps, container, false);
        initViews(view);
        preencherDados();
        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        etNomeLocal = view.findViewById(R.id.etNomeLocal);
        etLatitude = view.findViewById(R.id.etLatitude);
        etLongitude = view.findViewById(R.id.etLongitude);
        etRaio = view.findViewById(R.id.etRaio);
        btnCancelar = view.findViewById(R.id.btnCancelar);
        btnSalvar = view.findViewById(R.id.btnAdicionar);  // Reutiliza o botão "Adicionar"
        btnFechar = view.findViewById(R.id.btnFechar);
        switchMapear = view.findViewById(R.id.switchMapear);

        // Altera texto do botão para "Salvar"
        btnSalvar.setText("Salvar");

        // Esconde botão WiFi (não precisa alternar em modo edição)
        Button btnWifi = view.findViewById(R.id.btnWifi);
        if (btnWifi != null) {
            btnWifi.setVisibility(View.GONE);
        }

        // Inicializa mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.fragmentoMapa);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void preencherDados() {
        if (localParaEditar != null) {
            etNomeLocal.setText(localParaEditar.getNome());
            etLatitude.setText(String.format(Locale.US, "%.6f", localParaEditar.getLatitude()));
            etLongitude.setText(String.format(Locale.US, "%.6f", localParaEditar.getLongitude()));
            etRaio.setText(String.valueOf(localParaEditar.getRaio()));
        }
    }

    private void setupClickListeners() {
        btnFechar.setOnClickListener(v -> dismiss());
        btnCancelar.setOnClickListener(v -> dismiss());

        btnSalvar.setOnClickListener(v -> {
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

                // Callback com dados editados
                if (listener != null) {
                    listener.onLocalEditado(nome, lat, lng, raio);
                }
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

        // Marca posição atual do local no mapa
        if (localParaEditar != null) {
            LatLng posicao = new LatLng(localParaEditar.getLatitude(), localParaEditar.getLongitude());
            mapa.addMarker(new MarkerOptions().position(posicao).title(localParaEditar.getNome()));
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(posicao, 15));
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacao();
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
    }
}