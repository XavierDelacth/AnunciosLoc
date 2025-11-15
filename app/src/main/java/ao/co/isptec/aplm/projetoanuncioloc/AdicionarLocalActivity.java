package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.LocalAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Adapters.LocalAdapterTodosLocais;
import ao.co.isptec.aplm.projetoanuncioloc.Interface.OnLocalAddedListener;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;

public class AdicionarLocalActivity extends AppCompatActivity implements OnLocalAddedListener {

    // Views principais
    private ImageView btnBackLocais;
    private EditText etSearchLocais;
    private Button btnAddLocalFixo;

    // Tabs
    private LinearLayout tabTodosLocais, tabCriadosPorSi;
    private TextView tvTabTodosLocais, tvTabCriadosPorSi;
    private View indicatorTodosLocais, indicatorCriadosPorSi;
    private LinearLayout contentTodosLocais, contentCriadosPorSi;

    // RecyclerViews
    private RecyclerView rvTodosLocais, rvCriadosPorSi;
    private TextView tvEmptyTodosLocais, tvEmptyCriadosPorSi;

    // Adapters
    private LocalAdapterTodosLocais adapterTodos;
    private LocalAdapter adapterCriados;

    // Listas de dados
    private List<Local> listaTodosLocais;  // Lista completa (incluindo criados pelo usuário)
    private List<Local> listaCriadosPorSi;  // Apenas locais criados pelo usuário
    private List<Local> listaTodosFiltrada;  // Para busca na tab "Todos"
    private List<Local> listaCriadosFiltrada;  // Para busca na tab "Criados Por Si"

    // Controle de tab ativa
    private boolean isTabTodosAtiva = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_local);

        initViews();
        setupToolbar();
        setupTabs();
        setupListasLocais();
        setupSearchFilter();
        setupAddButton();
    }

    private void initViews() {
        btnBackLocais = findViewById(R.id.btnBackLocais);
        etSearchLocais = findViewById(R.id.etSearchLocais);
        btnAddLocalFixo = findViewById(R.id.btnAddLocalFixo);

        // Tabs
        tabTodosLocais = findViewById(R.id.tabTodosLocais);
        tabCriadosPorSi = findViewById(R.id.tabCriadosPorSi);
        tvTabTodosLocais = findViewById(R.id.tvTabTodosLocais);
        tvTabCriadosPorSi = findViewById(R.id.tvTabCriadosPorSi);
        indicatorTodosLocais = findViewById(R.id.indicatorTodosLocais);
        indicatorCriadosPorSi = findViewById(R.id.indicatorCriadosPorSi);

        // Conteúdos das tabs
        contentTodosLocais = findViewById(R.id.contentTodosLocais);
        contentCriadosPorSi = findViewById(R.id.contentCriadosPorSi);

        // RecyclerViews
        rvTodosLocais = findViewById(R.id.rvTodosLocais);
        rvCriadosPorSi = findViewById(R.id.rvCriadosPorSi);
        tvEmptyTodosLocais = findViewById(R.id.tvEmptyTodosLocais);
        tvEmptyCriadosPorSi = findViewById(R.id.tvEmptyCriadosPorSi);
    }

    private void setupToolbar() {
        btnBackLocais.setOnClickListener(v -> finish());
    }

    private void setupTabs() {
        // Click na tab "Todos Locais"
        tabTodosLocais.setOnClickListener(v -> {
            if (!isTabTodosAtiva) {
                mostrarTabTodos();
            }
        });

        // Click na tab "Criados Por Si"
        tabCriadosPorSi.setOnClickListener(v -> {
            if (isTabTodosAtiva) {
                mostrarTabCriados();
            }
        });
    }

    private void mostrarTabTodos() {
        isTabTodosAtiva = true;

        // Atualiza UI das tabs
        tvTabTodosLocais.setTextColor(getColor(R.color.verde_principal));
        tvTabCriadosPorSi.setTextColor(getColor(R.color.cinza_texto));
        indicatorTodosLocais.setVisibility(View.VISIBLE);
        indicatorCriadosPorSi.setVisibility(View.GONE);

        // Mostra conteúdo correspondente
        contentTodosLocais.setVisibility(View.VISIBLE);
        contentCriadosPorSi.setVisibility(View.GONE);

        // Limpa pesquisa ao trocar de tab
        etSearchLocais.setText("");
    }

    private void mostrarTabCriados() {
        isTabTodosAtiva = false;

        // Atualiza UI das tabs
        tvTabTodosLocais.setTextColor(getColor(R.color.cinza_texto));
        tvTabCriadosPorSi.setTextColor(getColor(R.color.verde_principal));
        indicatorTodosLocais.setVisibility(View.GONE);
        indicatorCriadosPorSi.setVisibility(View.VISIBLE);

        // Mostra conteúdo correspondente
        contentTodosLocais.setVisibility(View.GONE);
        contentCriadosPorSi.setVisibility(View.VISIBLE);

        // Limpa pesquisa ao trocar de tab
        etSearchLocais.setText("");
    }

    private void setupListasLocais() {
        // Simula locais existentes (alguns do sistema, alguns criados pelo usuário)
        listaTodosLocais = new ArrayList<>();
        listaTodosLocais.add(new Local(1L, "Belas Shopping", -8.9200, 13.2300, 500, null));
        listaTodosLocais.add(new Local(2L, "Zango", -8.9500, 13.2000, 300, null));
        listaTodosLocais.add(new Local(3L, "Ginásio do Camama I", -8.8300, 13.2500, 400, null));
        listaTodosLocais.add(new Local(4L, "Museu Nacional", -8.8100, 13.2400, 200, Arrays.asList("WiFi-Museu")));

        // Simula locais criados pelo usuário (IDs maiores que 100 = criados pelo usuário)
        listaCriadosPorSi = new ArrayList<>();
        listaCriadosPorSi.add(new Local(101L, "Minha Casa", -8.8500, 13.2600, 100, null));
        listaCriadosPorSi.add(new Local(102L, "Trabalho", -8.8200, 13.2300, 150, Arrays.asList("WiFi-Escritorio")));

        // Adiciona os criados pelo usuário à lista completa
        listaTodosLocais.addAll(listaCriadosPorSi);

        // Inicializa listas filtradas
        listaTodosFiltrada = new ArrayList<>(listaTodosLocais);
        listaCriadosFiltrada = new ArrayList<>(listaCriadosPorSi);

        // Setup Adapter "Todos Locais" (Read-only)
        adapterTodos = new LocalAdapterTodosLocais(listaTodosFiltrada);
        adapterTodos.setOnLocalClickListener((local, position) -> {
            // Retorna o local selecionado para a activity chamadora
            Intent result = new Intent();
            result.putExtra("localSelecionado", local.getNome());
            result.putExtra("latitude", local.getLatitude());
            result.putExtra("longitude", local.getLongitude());
            result.putExtra("raio", local.getRaio());
            setResult(RESULT_OK, result);
            finish();
        });
        rvTodosLocais.setLayoutManager(new LinearLayoutManager(this));
        rvTodosLocais.setAdapter(adapterTodos);

        // Setup Adapter "Criados Por Si" (Editable)
        adapterCriados = new LocalAdapter(listaCriadosFiltrada);
        adapterCriados.setOnLocalActionListener(new LocalAdapter.OnLocalActionListener() {
            @Override
            public void onLocalClick(Local local, int position) {
                // Retorna o local selecionado
                Intent result = new Intent();
                result.putExtra("localSelecionado", local.getNome());
                result.putExtra("latitude", local.getLatitude());
                result.putExtra("longitude", local.getLongitude());
                result.putExtra("raio", local.getRaio());
                setResult(RESULT_OK, result);
                finish();
            }

            @Override
            public void onEditClick(Local local, int position) {
                editarLocal(local, position);
            }

            @Override
            public void onDeleteClick(Local local, int position) {
                confirmarExclusao(local, position);
            }
        });
        rvCriadosPorSi.setLayoutManager(new LinearLayoutManager(this));
        rvCriadosPorSi.setAdapter(adapterCriados);

        atualizarVisibilidades();
    }

    private void setupSearchFilter() {
        etSearchLocais.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();

                if (isTabTodosAtiva) {
                    // Filtra lista "Todos Locais"
                    listaTodosFiltrada.clear();
                    if (query.isEmpty()) {
                        listaTodosFiltrada.addAll(listaTodosLocais);
                    } else {
                        for (Local local : listaTodosLocais) {
                            if (local.getNome().toLowerCase().contains(query)) {
                                listaTodosFiltrada.add(local);
                            }
                        }
                    }
                    adapterTodos.notifyDataSetChanged();
                } else {
                    // Filtra lista "Criados Por Si"
                    listaCriadosFiltrada.clear();
                    if (query.isEmpty()) {
                        listaCriadosFiltrada.addAll(listaCriadosPorSi);
                    } else {
                        for (Local local : listaCriadosPorSi) {
                            if (local.getNome().toLowerCase().contains(query)) {
                                listaCriadosFiltrada.add(local);
                            }
                        }
                    }
                    adapterCriados.notifyDataSetChanged();
                }

                atualizarVisibilidades();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void atualizarVisibilidades() {
        // Tab "Todos Locais"
        if (listaTodosFiltrada.isEmpty()) {
            rvTodosLocais.setVisibility(View.GONE);
            tvEmptyTodosLocais.setVisibility(View.VISIBLE);
            tvEmptyTodosLocais.setText(etSearchLocais.getText().toString().isEmpty()
                    ? "Nenhum local disponível"
                    : "Nenhum local encontrado");
        } else {
            rvTodosLocais.setVisibility(View.VISIBLE);
            tvEmptyTodosLocais.setVisibility(View.GONE);
        }

        // Tab "Criados Por Si"
        if (listaCriadosFiltrada.isEmpty()) {
            rvCriadosPorSi.setVisibility(View.GONE);
            tvEmptyCriadosPorSi.setVisibility(View.VISIBLE);
            tvEmptyCriadosPorSi.setText(etSearchLocais.getText().toString().isEmpty()
                    ? "Você ainda não criou nenhum local"
                    : "Nenhum local encontrado");
        } else {
            rvCriadosPorSi.setVisibility(View.VISIBLE);
            tvEmptyCriadosPorSi.setVisibility(View.GONE);
        }
    }

    private void setupAddButton() {
        btnAddLocalFixo.setOnClickListener(v -> {
            AdicionarGPSDialog gpsDialog = AdicionarGPSDialog.newInstance(this);
            gpsDialog.show(getSupportFragmentManager(), "AdicionarGPSDialog");
        });
    }

    // EDITAR LOCAL
    private void editarLocal(Local local, int position) {
        boolean isWifi = local.getWifiIds() != null && !local.getWifiIds().isEmpty();

        if (isWifi) {
            // Abre dialog WiFi em modo edição
            editarLocalWiFi(local, position);
        } else {
            // Abre dialog GPS em modo edição
            editarLocalGPS(local, position);
        }
    }

    private void editarLocalGPS(Local local, int position) {
        // Cria o dialog GPS passando os dados do local para edição
        EditarGPSDialog dialog = EditarGPSDialog.newInstance(local, (nomeEditado, latEditada, lngEditada, raioEditado) -> {
            // Atualiza o local na lista
            local.setNome(nomeEditado);
            local.setLatitude(latEditada);
            local.setLongitude(lngEditada);
            local.setRaio(raioEditado);

            // Notifica os adapters
            adapterCriados.notifyItemChanged(position);
            int posicaoTodos = listaTodosLocais.indexOf(local);
            if (posicaoTodos != -1) {
                adapterTodos.notifyItemChanged(posicaoTodos);
            }

            Toast.makeText(this, "Local atualizado com sucesso!", Toast.LENGTH_SHORT).show();
        });

        dialog.show(getSupportFragmentManager(), "EditarGPSDialog");
    }

    private void editarLocalWiFi(Local local, int position) {
        // Cria o dialog WiFi passando os dados do local para edição
        String ssidAtual = local.getWifiIds() != null && !local.getWifiIds().isEmpty()
                ? local.getWifiIds().get(0)
                : "";

        EditarWiFiDialog dialog = EditarWiFiDialog.newInstance(local.getNome(), ssidAtual,
                (nomeEditado, ssidsEditados) -> {
                    // Atualiza o local na lista
                    local.setNome(nomeEditado);
                    local.setWifiIds(ssidsEditados);

                    // Notifica os adapters
                    adapterCriados.notifyItemChanged(position);
                    int posicaoTodos = listaTodosLocais.indexOf(local);
                    if (posicaoTodos != -1) {
                        adapterTodos.notifyItemChanged(posicaoTodos);
                    }

                    Toast.makeText(this, "Local atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                });

        dialog.show(getSupportFragmentManager(), "EditarWiFiDialog");
    }

    // EXCLUIR LOCAL
    private void confirmarExclusao(Local local, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Local")
                .setMessage("Tem certeza que deseja excluir \"" + local.getNome() + "\"?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    excluirLocal(local, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirLocal(Local local, int position) {
        // Remove da lista "Criados Por Si"
        listaCriadosPorSi.remove(local);
        listaCriadosFiltrada.remove(local);
        adapterCriados.notifyItemRemoved(position);

        // Remove da lista "Todos Locais"
        int posicaoTodos = listaTodosLocais.indexOf(local);
        if (posicaoTodos != -1) {
            listaTodosLocais.remove(posicaoTodos);
            listaTodosFiltrada.remove(local);
            adapterTodos.notifyItemRemoved(posicaoTodos);
        }

        atualizarVisibilidades();
        Toast.makeText(this, "Local excluído com sucesso!", Toast.LENGTH_SHORT).show();
    }

    // CALLBACKS DA INTERFACE OnLocalAddedListener
    @Override
    public void onLocalAddedGPS(String nome, double lat, double lng, int raio) {
        // Cria novo local GPS (ID > 100 = criado pelo usuário)
        Long novoId = 100L + listaCriadosPorSi.size() + 1;
        Local novoLocal = new Local(novoId, nome, lat, lng, raio, null);

        // Adiciona às listas
        listaCriadosPorSi.add(novoLocal);
        listaTodosLocais.add(novoLocal);
        listaCriadosFiltrada.add(novoLocal);
        listaTodosFiltrada.add(novoLocal);

        // Notifica adapters
        adapterCriados.notifyItemInserted(listaCriadosPorSi.size() - 1);
        adapterTodos.notifyItemInserted(listaTodosLocais.size() - 1);

        atualizarVisibilidades();
        Toast.makeText(this, "Local GPS adicionado com sucesso!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocalAddedWiFi(String nome, List<String> ssids) {
        // Cria novo local WiFi
        Long novoId = 100L + listaCriadosPorSi.size() + 1;
        Local novoLocal = new Local(novoId, nome, 0.0, 0.0, 0, ssids);

        // Adiciona às listas
        listaCriadosPorSi.add(novoLocal);
        listaTodosLocais.add(novoLocal);
        listaCriadosFiltrada.add(novoLocal);
        listaTodosFiltrada.add(novoLocal);

        // Notifica adapters
        adapterCriados.notifyItemInserted(listaCriadosPorSi.size() - 1);
        adapterTodos.notifyItemInserted(listaTodosLocais.size() - 1);

        atualizarVisibilidades();
        Toast.makeText(this, "Local WiFi adicionado com sucesso!", Toast.LENGTH_SHORT).show();
    }
}