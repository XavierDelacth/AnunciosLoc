package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ao.co.isptec.aplm.projetoanuncioloc.Adapters.LocalAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Interface.OnLocalAddedListener;  // CORREÇÃO: Import correto da interface comum
import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;

public class AdicionarLocalActivity extends AppCompatActivity implements OnLocalAddedListener {  // Implementa a interface comum

    private ImageView btnBackLocais;
    private EditText etSearchLocais;
    private Button btnAddLocalFixo;
    private RecyclerView rvLocais;
    private TextView tvEmptyLocais;
    private LocalAdapter adapter;
    private List<Local> listaLocaisCompleta;  // Lista original para filtro
    private List<Local> listaLocais;  // Lista filtrada para adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_local);

        initViews();
        setupToolbar();
        setupListaLocais();  // Simula lista dinâmica
        setupSearchFilter();
        setupAddButton();
    }

    private void initViews() {
        btnBackLocais = findViewById(R.id.btnBackLocais);
        etSearchLocais = findViewById(R.id.etSearchLocais);
        btnAddLocalFixo = findViewById(R.id.btnAddLocalFixo);
        rvLocais = findViewById(R.id.rv_locais);  // ID correto do XML
        tvEmptyLocais = findViewById(R.id.tvEmptyLocais);
    }

    private void setupToolbar() {
        btnBackLocais.setOnClickListener(v -> finish());
    }

    // Simula lista de locais (pronta para BD: substitui por query)
    private void setupListaLocais() {
        listaLocaisCompleta = new ArrayList<>();
        // Simulação de 4 locais (do teu XML original, com dados completos)
        listaLocaisCompleta.add(new Local(1L, "Belas Shopping", -8.9200, 13.2300, 500, Arrays.asList("WiFi-Belas")));
        listaLocaisCompleta.add(new Local(2L, "Zango", -8.9500, 13.2000, 300, Arrays.asList("Hotspot-Zango")));
        listaLocaisCompleta.add(new Local(3L, "Ginásio do Camama I", -8.8300, 13.2500, 400, null));  // GPS (wifiIds null)
        listaLocaisCompleta.add(new Local(4L, "Museu Nacional", -8.8100, 13.2400, 200, Arrays.asList("WiFi-Museu")));

        listaLocais = new ArrayList<>(listaLocaisCompleta);  // Cópia inicial
        adapter = new LocalAdapter(listaLocais);
        adapter.setOnLocalClickListener((local, position) -> {
            // Retorna o local selecionado para a activity chamadora (ex.: AdicionarAnuncios)
            Intent result = new Intent();
            result.putExtra("localSelecionado", local.getNome());
            result.putExtra("latitude", local.getLatitude());
            result.putExtra("longitude", local.getLongitude());
            result.putExtra("raio", local.getRaio());
            setResult(RESULT_OK, result);
            finish();
        });

        rvLocais.setLayoutManager(new LinearLayoutManager(this));
        rvLocais.setAdapter(adapter);
        atualizarVisibilidade();  // Inicializa visibilidade
    }

    private void setupSearchFilter() {
        etSearchLocais.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                listaLocais.clear();
                if (query.isEmpty()) {
                    listaLocais.addAll(listaLocaisCompleta);  // Mostra todos
                } else {
                    for (Local local : listaLocaisCompleta) {
                        if (local.getNome().toLowerCase().contains(query)) {
                            listaLocais.add(local);  // Filtra por nome
                        }
                    }
                }
                adapter.notifyDataSetChanged();  // Refresh adapter
                atualizarVisibilidade();  // Atualiza empty se vazia
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Atualiza visibilidade (RecyclerView ou empty)
    private void atualizarVisibilidade() {
        if (listaLocais.isEmpty()) {
            rvLocais.setVisibility(View.GONE);
            tvEmptyLocais.setVisibility(View.VISIBLE);
            tvEmptyLocais.setText(etSearchLocais.getText().toString().isEmpty() ? "Nenhum local adicionado" : "Nenhum local encontrado");
        } else {
            rvLocais.setVisibility(View.VISIBLE);
            tvEmptyLocais.setVisibility(View.GONE);
        }
    }

    private void setupAddButton() {
        btnAddLocalFixo.setOnClickListener(v -> {
            // Abre diálogo GPS por default
            AdicionarGPSDialog gpsDialog = AdicionarGPSDialog.newInstance(this);  // 'this' como listener (implementa interface)
            gpsDialog.show(getSupportFragmentManager(), "AdicionarGPSDialog");
        });

        // NOVO: Botão ou toggle para WiFi (adiciona no layout se não tiveres)
        Button btnWifiToggle = findViewById(R.id.btnWifiToggle);  // Adiciona no XML se necessário
        if (btnWifiToggle != null) {
            btnWifiToggle.setOnClickListener(v -> {
                AdicionarWIFIDialog wifiDialog = AdicionarWIFIDialog.newInstance(this);  // 'this' como listener comum
                wifiDialog.show(getSupportFragmentManager(), "AdicionarWIFIDialog");
            });
        }
    }

    // IMPLEMENTAÇÃO DOS MÉTODOS DA INTERFACE COMUM (callback para salvar)
    @Override
    public void onLocalAddedGPS(String nome, double lat, double lng, int raio) {
        // Salva GPS na lista (futuro: BD)
        Local novoLocal = new Local(listaLocaisCompleta.size() + 1, nome, lat, lng, raio, null);
        listaLocaisCompleta.add(novoLocal);
        listaLocais.clear();
        listaLocais.addAll(listaLocaisCompleta);
        adapter.notifyDataSetChanged();  // Refresh lista
        atualizarVisibilidade();
        Toast.makeText(this, "Local GPS adicionado à lista!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocalAddedWiFi(String nome, List<String> ssids) {
        // Salva WiFi na lista (futuro: BD)
        Local novoLocal = new Local(listaLocaisCompleta.size() + 1, nome, 0.0, 0.0, 0, ssids);
        listaLocaisCompleta.add(novoLocal);
        listaLocais.clear();
        listaLocais.addAll(listaLocaisCompleta);
        adapter.notifyDataSetChanged();
        atualizarVisibilidade();
        Toast.makeText(this, "Local WiFi adicionado à lista!", Toast.LENGTH_SHORT).show();
    }
}