package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;

public class AdicionarAnunciosActivity extends AppCompatActivity implements AdicionarKeyDialog.OnKeyAddedListener {

    private static final String TAG = "AActivity"; // Tag para logs

    // Views do layout
    private ImageView btnBack, btnAddLocation, btnAdicionarChave;
    private TextView tvDataInicio, tvDataFim, tvHoraInicio, tvHoraFim, tvTipoRestricao, tvModoEntrega, tvLocalSelecionado;
    private EditText etTitulo, etMensagem, etPesquisarChaves;
    private LinearLayout llLocal, llDataInicio, llDataFim, llHoraInicio, llHoraFim, llTipoRestricao, llModoEntrega, llImagem, layoutEmptyChaves;
    private Spinner spinnerRestricao, spinnerModoEntrega, spinnerLocais;
    private CardView cardChavesContainer;
    private Button btnPublicar;
    private RecyclerView rvChavesRestricoes;
    private ao.co.isptec.aplm.projetoanuncioloc.Adapters.ProfileKeyAdapter keyAdapter;
    private String localSelecionado = null;
    private String caminhoImagem = null;

    // Para restrições (chaves públicas)
    private Map<String, List<String>> restricoesPerfil = new HashMap<>();
    private List<ProfileKey> allKeys; // Chaves públicas
    private List<ProfileKey> chavesFiltradas = new ArrayList<>(); // Para search

    // Launchers
    private final ActivityResultLauncher<Intent> localLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Local selecionado retornado");
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String nome = result.getData().getStringExtra("nome_local");
                    String tipo = result.getData().getStringExtra("tipo");
                    if (nome != null && !nome.isEmpty()) {
                        localSelecionado = nome;
                        tvLocalSelecionado.setText(nome);
                        Toast.makeText(this, tipo + " adicionado: " + nome, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Local atualizado: " + nome);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> imagemLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Imagem selecionada retornada");
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        caminhoImagem = uri.toString();
                        ImageView ivPreview = findViewById(R.id.ivPreviewImagem);
                        ivPreview.setImageURI(uri);
                        TextView tvHint = findViewById(R.id.tvImagemHint);
                        tvHint.setText("Imagem selecionada");
                        Toast.makeText(this, "Imagem adicionada!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Imagem URI: " + uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate chamado - Activity iniciada");
        setContentView(R.layout.activity_criar_anuncios);

        initViews();
        if (!initViewsSucesso) {
            Toast.makeText(this, "Erro ao carregar layout. Verifica XML.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initRestricoes(); // Inicializa chaves públicas
        setupSpinner();
        setupSpinnerModoEntrega();
        setupSpinnerLocais();
        setupClickListeners();
        atualizarVisibilidadeChaves(); // Inicial: mostra lista

        resetarDataHora(tvDataInicio, "dd/mm/aaaa");
        resetarDataHora(tvDataFim, "dd/mm/aaaa");
        resetarDataHora(tvHoraInicio, "hh:mm");
        resetarDataHora(tvHoraFim, "hh:mm");

        Log.d(TAG, "onCreate concluído - Placeholders definidos para datas/horas");
    }

    private boolean initViewsSucesso = true;

    private void initViews() {
        Log.d(TAG, "initViews chamado - Inicializando views");
        initViewsSucesso = true;

        btnBack = findViewById(R.id.btnBack);
        if (btnBack == null) { Log.e(TAG, "btnBack não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "btnBack OK"); }

        llLocal = findViewById(R.id.llLocal);
        if (llLocal == null) { Log.e(TAG, "llLocal não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "llLocal OK"); }

        btnAddLocation = findViewById(R.id.btnAddLocation);
        if (btnAddLocation == null) { Log.e(TAG, "btnAddLocation não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "btnAddLocation OK"); }

        tvLocalSelecionado = findViewById(R.id.tvLocalSelecionado);
        if (tvLocalSelecionado == null) { Log.e(TAG, "tvLocalSelecionado não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "tvLocalSelecionado OK"); }

        etTitulo = findViewById(R.id.etTitulo);
        if (etTitulo == null) { Log.e(TAG, "etTitulo não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "etTitulo OK"); }

        etMensagem = findViewById(R.id.etMensagem);
        if (etMensagem == null) { Log.e(TAG, "etMensagem não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "etMensagem OK"); }

        llImagem = findViewById(R.id.llImagem);
        if (llImagem == null) { Log.e(TAG, "llImagem não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "llImagem OK"); }

        llDataInicio = findViewById(R.id.llDataInicio);
        if (llDataInicio == null) { Log.e(TAG, "llDataInicio não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "llDataInicio OK"); }

        llDataFim = findViewById(R.id.llDataFim);
        if (llDataFim == null) { Log.e(TAG, "llDataFim não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "llDataFim OK"); }

        llHoraInicio = findViewById(R.id.llHoraInicio);
        if (llHoraInicio == null) { Log.e(TAG, "llHoraInicio não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "llHoraInicio OK"); }

        llHoraFim = findViewById(R.id.llHoraFim);
        if (llHoraFim == null) { Log.e(TAG, "llHoraFim não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "llHoraFim OK"); }

        tvDataInicio = findViewById(R.id.tvDataInicio);
        if (tvDataInicio == null) { Log.e(TAG, "tvDataInicio não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "tvDataInicio OK"); }

        tvDataFim = findViewById(R.id.tvDataFim);
        if (tvDataFim == null) { Log.e(TAG, "tvDataFim não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "tvDataFim OK"); }

        tvHoraInicio = findViewById(R.id.tvHoraInicio);
        if (tvHoraInicio == null) { Log.e(TAG, "tvHoraInicio não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "tvHoraInicio OK"); }

        tvHoraFim = findViewById(R.id.tvHoraFim);
        if (tvHoraFim == null) { Log.e(TAG, "tvHoraFim não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "tvHoraFim OK"); }

        llTipoRestricao = findViewById(R.id.llTipoRestricao);
        if (llTipoRestricao == null) { Log.e(TAG, "llTipoRestricao não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "llTipoRestricao OK"); }

        tvTipoRestricao = findViewById(R.id.tvTipoRestricao);
        if (tvTipoRestricao == null) { Log.e(TAG, "tvTipoRestricao não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "tvTipoRestricao OK"); }

        spinnerRestricao = findViewById(R.id.spinnerRestricao);
        if (spinnerRestricao == null) { Log.e(TAG, "spinnerRestricao não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "spinnerRestricao OK"); }

        llModoEntrega = findViewById(R.id.llModoEntrega);
        if (llModoEntrega == null) { Log.e(TAG, "llModoEntrega não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "llModoEntrega OK"); }

        tvModoEntrega = findViewById(R.id.tvModoEntrega);
        if (tvModoEntrega == null) { Log.e(TAG, "tvModoEntrega não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "tvModoEntrega OK"); }

        spinnerModoEntrega = findViewById(R.id.spinnerModoEntrega);
        if (spinnerModoEntrega == null) { Log.e(TAG, "spinnerModoEntrega não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "spinnerModoEntrega OK"); }

        spinnerLocais = findViewById(R.id.spinnerLocais);
        if (spinnerLocais == null) { Log.e(TAG, "spinnerLocais não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "spinnerLocais OK"); }

        btnPublicar = findViewById(R.id.btnPublicar);
        if (btnPublicar == null) { Log.e(TAG, "btnPublicar não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "btnPublicar OK"); }

        // Views para chaves públicas
        etPesquisarChaves = findViewById(R.id.etPesquisarChaves);
        if (etPesquisarChaves == null) { Log.e(TAG, "etPesquisarChaves não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "etPesquisarChaves OK"); }

        btnAdicionarChave = findViewById(R.id.btnAdicionarChave);
        if (btnAdicionarChave == null) { Log.e(TAG, "btnAdicionarChave não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "btnAdicionarChave OK"); }

        rvChavesRestricoes = findViewById(R.id.rv_chaves_restricoes);
        if (rvChavesRestricoes == null) { Log.e(TAG, "rv_chaves_restricoes não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "rv_chaves_restricoes OK"); }

        layoutEmptyChaves = findViewById(R.id.layout_empty_chaves);
        if (layoutEmptyChaves == null) { Log.e(TAG, "layout_empty_chaves não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "layout_empty_chaves OK"); }

        cardChavesContainer = findViewById(R.id.card_chaves_container);
        if (cardChavesContainer == null) { Log.e(TAG, "card_chaves_container não encontrado!"); initViewsSucesso = false; } else { Log.d(TAG, "card_chaves_container OK"); }

        // Textos padrão
        if (tvTipoRestricao != null) {
            tvTipoRestricao.setText("Whitelist");
            Log.d(TAG, "tvTipoRestricao setado");
        } else {
            Log.e(TAG, "Não setou tvTipoRestricao (null)");
        }

        if (tvModoEntrega != null) {
            tvModoEntrega.setText("Centralizado");
            Log.d(TAG, "tvModoEntrega setado");
        } else {
            Log.e(TAG, "Não setou tvModoEntrega (null)");
        }

        if (initViewsSucesso) {
            Log.d(TAG, "initViews concluído - Todas views OK!");
        } else {
            Log.e(TAG, "initViews FALHOU - Alguma view null!");
        }
    }

    private void initRestricoes() {
        Log.d(TAG, "initRestricoes chamado - Inicializando chaves públicas");
        allKeys = new ArrayList<>();
        // Exemplos mock
        allKeys.add(new ProfileKey("Gênero", Arrays.asList("Masculino", "Feminino", "Outro")));
        allKeys.add(new ProfileKey("Idade", Arrays.asList("18-24", "25-34", "35+")));
        allKeys.add(new ProfileKey("Clube Favorito", Arrays.asList("Real Madrid", "Barcelona", "Benfica")));

        chavesFiltradas.clear();
        chavesFiltradas.addAll(allKeys);
        Log.d(TAG, "Chaves públicas carregadas: " + allKeys.size() + " itens");

        // Setup RecyclerView
        if (rvChavesRestricoes != null && keyAdapter == null) {
            rvChavesRestricoes.setLayoutManager(new LinearLayoutManager(this));
            // Use o ProfileKeyAdapter do package Adapters (showOnlySelected = false para mostrar todas)
            keyAdapter = new ao.co.isptec.aplm.projetoanuncioloc.Adapters.ProfileKeyAdapter(
                    this, chavesFiltradas, false);

            // Define o listener para cliques nos valores
            keyAdapter.setOnValueClickListener((keyName, value) -> {
                Log.d(TAG, "Valor clicado: " + keyName + " -> " + value);
                // Encontra a chave e toggle o valor
                for (ProfileKey key : allKeys) {
                    if (key.getName().equals(keyName)) {
                        key.toggleValue(value);

                        // Atualiza o mapa de restrições
                        if (key.hasSelectedValues()) {
                            restricoesPerfil.put(keyName, new ArrayList<>(key.getSelectedValues()));
                        } else {
                            restricoesPerfil.remove(keyName);
                        }
                        break;
                    }
                }
                keyAdapter.notifyDataSetChanged();
                Log.d(TAG, "Restrições atualizadas: " + restricoesPerfil);
            });

            rvChavesRestricoes.setAdapter(keyAdapter);
            Log.d(TAG, "RecyclerView configurado");
        } else if (keyAdapter != null) {
            keyAdapter.notifyDataSetChanged();
        }

        // TextWatcher para busca
        if (etPesquisarChaves != null) {
            etPesquisarChaves.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { filtrarChaves(s.toString()); }
            });
        }

        atualizarVisibilidadeChaves();
        if (cardChavesContainer != null) {
            cardChavesContainer.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "initRestricoes concluído - Chaves visíveis");
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners chamado - Configurando listeners");
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Botão Back clicado - Finalizando activity");
            finish();
        });
        if (btnAddLocation != null) {
            btnAddLocation.setOnClickListener(v -> {
                Intent intent = new Intent(AdicionarAnunciosActivity.this, AdicionarGPS.class);
                localLauncher.launch(intent);
            });
        }

        llImagem.setOnClickListener(v -> {
            Log.d(TAG, "llImagem clicado - Abrindo galeria");
            abrirGaleriaImagem();
        });
        llDataInicio.setOnClickListener(v -> {
            Log.d(TAG, "llDataInicio clicado - Abrindo DatePicker");
            showDatePicker(tvDataInicio);
        });
        llDataFim.setOnClickListener(v -> {
            Log.d(TAG, "llDataFim clicado - Abrindo DatePicker");
            showDatePicker(tvDataFim);
        });
        llHoraInicio.setOnClickListener(v -> {
            Log.d(TAG, "llHoraInicio clicado - Abrindo TimePicker");
            showTimePicker(tvHoraInicio);
        });
        llHoraFim.setOnClickListener(v -> {
            Log.d(TAG, "llHoraFim clicado - Abrindo TimePicker");
            showTimePicker(tvHoraFim);
        });
        btnPublicar.setOnClickListener(v -> {
            Log.d(TAG, "btnPublicar clicado - Validando e publicando anúncio");
            publicarAnuncio();
        });

        // Listener para botão adicionar chave pública
        btnAdicionarChave.setOnClickListener(v -> {
            Log.d(TAG, "btnAdicionarChave clicado - Abrindo dialog");
            abrirAdicionarKeyDialog();
        });

        if (llTipoRestricao != null && spinnerRestricao != null) {
            llTipoRestricao.setOnClickListener(v -> {
                spinnerRestricao.performClick();
            });
        }
        if (llModoEntrega != null && spinnerModoEntrega != null) {
            llModoEntrega.setOnClickListener(v -> {
                spinnerModoEntrega.performClick();
            });
        }

        if (llLocal != null && spinnerLocais != null) {
            llLocal.setOnClickListener(v ->
                    spinnerLocais.performClick());
        }

        Log.d(TAG, "setupClickListeners concluído");
    }

    // Filtro para pesquisa em chaves públicas
    private void filtrarChaves(String query) {
        Log.d(TAG, "Filtrando chaves com query: " + query);
        chavesFiltradas.clear();
        if (query.isEmpty()) {
            chavesFiltradas.addAll(allKeys);
        } else {
            for (ProfileKey key : allKeys) {
                if (key.getName().toLowerCase().contains(query.toLowerCase())) {
                    chavesFiltradas.add(key);
                }
            }
        }
        if (keyAdapter != null) {
            keyAdapter.updateKeys(chavesFiltradas);
        }
        atualizarVisibilidadeChaves();
        Log.d(TAG, "Filtro concluído - Itens filtrados: " + chavesFiltradas.size());
    }

    // Atualiza visibilidade do RV e empty state
    private void atualizarVisibilidadeChaves() {
        Log.d(TAG, "Atualizando visibilidade - Itens: " + chavesFiltradas.size());
        // Mostra as chaves se houver itens filtrados, independente de seleções
        if (chavesFiltradas.isEmpty()) {
            rvChavesRestricoes.setVisibility(View.GONE);
            layoutEmptyChaves.setVisibility(View.VISIBLE);
        } else {
            rvChavesRestricoes.setVisibility(View.VISIBLE);
            layoutEmptyChaves.setVisibility(View.GONE);
        }
    }

    // Abrir galeria para imagem
    private void abrirGaleriaImagem() {
        Log.d(TAG, "Abrindo galeria de imagens");
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagemLauncher.launch(intent);
    }

    // Date picker
    private void showDatePicker(TextView textView) {
        Log.d(TAG, "Abrindo DatePicker para: " + textView.getId());
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format("%02d/%02d/%04d", day, month + 1, year);
            textView.setText(date);
            Log.d(TAG, "Data selecionada: " + date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Time picker
    private void showTimePicker(TextView textView) {
        Log.d(TAG, "Abrindo TimePicker para: " + textView.getId());
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String time = String.format("%02d:%02d", hour, minute);
            textView.setText(time);
            Log.d(TAG, "Hora selecionada: " + time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    // Setup para restrição
    private void setupSpinner() {
        Log.d(TAG, "setupSpinner chamado - Configurando restrição com update visual");
        if (spinnerRestricao != null && tvTipoRestricao != null) {
            String[] restricoes = {"Whitelist", "Blacklist"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, restricoes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRestricao.setAdapter(adapter);

            // Remove qualquer listener anterior
            spinnerRestricao.setOnItemSelectedListener(null);

            // Define o listener ANTES da seleção inicial
            spinnerRestricao.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selected = restricoes[position];
                    Log.d(TAG, "Restrição selecionada: " + selected + " (posição " + position + ")");

                    // Atualiza a TextView imediatamente
                    tvTipoRestricao.setText(selected);

                    // Gerencia visibilidade do card de chaves
                    if ("Nenhuma".equals(selected)) {
                        if (cardChavesContainer != null) {
                            cardChavesContainer.setVisibility(View.GONE);
                        }
                    } else {
                        if (cardChavesContainer != null) {
                            cardChavesContainer.setVisibility(View.VISIBLE);
                        }
                        atualizarVisibilidadeChaves();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG, "onNothingSelected para restrição");
                }
            });

            // Define a seleção inicial (índice 0 = Whitelist)
            spinnerRestricao.setSelection(0, false);
            Log.d(TAG, "Spinner restrição configurado");
        }
    }

    private void setupSpinnerModoEntrega() {
        Log.d(TAG, "setupSpinnerModoEntrega chamado - Configurando modo com update visual");
        if (spinnerModoEntrega != null && tvModoEntrega != null) {
            String[] modos = {"Centralizado", "Descentralizado"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerModoEntrega.setAdapter(adapter);

            // Remove qualquer listener anterior
            spinnerModoEntrega.setOnItemSelectedListener(null);

            // Define o listener ANTES da seleção inicial
            spinnerModoEntrega.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selected = modos[position];
                    Log.d(TAG, "Modo selecionado: " + selected + " (posição " + position + ")");

                    // Atualiza a TextView imediatamente
                    tvModoEntrega.setText(selected);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG, "onNothingSelected para modo");
                }
            });

            // Define a seleção inicial (índice 0 = Centralizado)
            spinnerModoEntrega.setSelection(0, false);
            Log.d(TAG, "Spinner modo entrega configurado");
        }
    }

    private void setupSpinnerLocais() {
        Log.d(TAG, "setupSpinnerLocais chamado - Configurando locais");
        if (spinnerLocais != null && tvLocalSelecionado != null) {
            String[] locais = {"Benguela", "Largo da Independência", "Belas Shopping", "Ginásio do Camama I"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locais);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLocais.setAdapter(adapter);

            // Remove qualquer listener anterior
            spinnerLocais.setOnItemSelectedListener(null);

            // Define o listener ANTES da seleção inicial
            spinnerLocais.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    localSelecionado = locais[position];
                    Log.d(TAG, "Local selecionado: " + localSelecionado + " (posição " + position + ")");

                    // Atualiza a TextView imediatamente
                    tvLocalSelecionado.setText(localSelecionado);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.d(TAG, "onNothingSelected para local");
                }
            });

            // Define a seleção inicial (índice 0 = Benguela)
            spinnerLocais.setSelection(0, false);
            Log.d(TAG, "Spinner locais configurado");
        }
    }

    // Abrir dialog de chave
    private void abrirAdicionarKeyDialog() {
        Log.d(TAG, "Abrindo AdicionarKeyDialog");
        AdicionarKeyDialog dialog = AdicionarKeyDialog.newInstance(allKeys, restricoesPerfil);
        dialog.setOnKeyAddedListener(this);
        dialog.show(getSupportFragmentManager(), "AdicionarKeyDialog");
    }

    @Override
    public void onKeyAdded(String keyName, List<String> values) {
        Log.d(TAG, "Chave adicionada via callback: " + keyName + " com valores: " + values);
        restricoesPerfil.put(keyName, values);
        filtrarChaves(etPesquisarChaves.getText().toString());
        if (keyAdapter != null) {
            keyAdapter.notifyDataSetChanged();
        }
        atualizarVisibilidadeChaves();
        Toast.makeText(this, "Chave pública adicionada: " + keyName, Toast.LENGTH_SHORT).show();
    }

    // Publicar anúncio
    private void publicarAnuncio() {
        Log.d(TAG, "publicarAnuncio chamado - Iniciando validações");
        String titulo = etTitulo.getText().toString().trim();
        String mensagem = etMensagem.getText().toString().trim();
        String dataInicio = tvDataInicio.getText().toString();
        String dataFim = tvDataFim.getText().toString();
        String horaInicio = tvHoraInicio.getText().toString();
        String horaFim = tvHoraFim.getText().toString();
        String restricao = tvTipoRestricao.getText().toString();
        String modoEntrega = tvModoEntrega.getText().toString();

        // Validações
        if (localSelecionado == null || localSelecionado.equals("Selecionar um local")) {
            Log.w(TAG, "Validação falhou: Local não selecionado");
            Toast.makeText(this, "Por favor, adicione um local de propagação", Toast.LENGTH_LONG).show();
            return;
        }
        if (titulo.isEmpty()) {
            Log.w(TAG, "Validação falhou: Título vazio");
            Toast.makeText(this, "Digite um título para o anúncio", Toast.LENGTH_LONG).show();
            return;
        }
        if (mensagem.isEmpty()) {
            Log.w(TAG, "Validação falhou: Mensagem vazia");
            Toast.makeText(this, "Escreva uma mensagem para o anúncio", Toast.LENGTH_LONG).show();
            return;
        }
        if (dataInicio.equals("dd/mm/aaaa") || dataFim.equals("dd/mm/aaaa")) {
            Log.w(TAG, "Validação falhou: Datas não selecionadas");
            Toast.makeText(this, "Selecione as datas de início e fim", Toast.LENGTH_LONG).show();
            return;
        }
        if (horaInicio.equals("hh:mm") || horaFim.equals("hh:mm")) {
            Log.w(TAG, "Validação falhou: Horas não selecionadas");
            Toast.makeText(this, "Selecione os horários", Toast.LENGTH_LONG).show();
            return;
        }
        if (!"Nenhuma".equals(restricao) && restricoesPerfil.isEmpty()) {
            Log.w(TAG, "Validação falhou: Chaves vazias para restrição " + restricao);
            Toast.makeText(this, "Adicione pelo menos uma chave pública de restrição", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Validações OK - Criando Anuncio");
        Anuncio novoAnuncio = new Anuncio(titulo, mensagem, localSelecionado, caminhoImagem,
                dataInicio, dataFim, horaInicio, horaFim, restricao, modoEntrega);
        for (Map.Entry<String, List<String>> entry : restricoesPerfil.entrySet()) {
            novoAnuncio.addChave(entry.getKey(), entry.getValue());
        }

        boolean sucesso = salvarAnuncio(novoAnuncio);
        if (sucesso) {
            Log.d(TAG, "Anúncio salvo com sucesso");
            Toast.makeText(this, "Anúncio publicado com sucesso!", Toast.LENGTH_SHORT).show();
            limparCampos();
            finish();
        } else {
            Log.e(TAG, "Erro ao salvar anúncio");
            Toast.makeText(this, "Erro ao publicar. Tente novamente.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean salvarAnuncio(Anuncio anuncio) {
        Log.d(TAG, "salvarAnuncio chamado - Salvando: " + anuncio.titulo);
        // TODO: Implementar salvamento no BD ou servidor (F4 do PDF)
        // Por agora, simula sucesso
        return true;
    }

    private void limparCampos() {
        Log.d(TAG, "limparCampos chamado - Limpando campos");
        etTitulo.setText("");
        etMensagem.setText("");
        resetarDataHora(tvDataInicio, "dd/mm/aaaa");
        resetarDataHora(tvDataFim, "dd/mm/aaaa");
        resetarDataHora(tvHoraInicio, "hh:mm");
        resetarDataHora(tvHoraFim, "hh:mm");
        tvTipoRestricao.setText("Whitelist");
        spinnerRestricao.setSelection(0);
        tvModoEntrega.setText("Centralizado");
        spinnerModoEntrega.setSelection(0);
        etPesquisarChaves.setText("");

        localSelecionado = null;
        tvLocalSelecionado.setText("Selecionar um local");
        tvLocalSelecionado.setTextColor(getColor(android.R.color.darker_gray));
        caminhoImagem = null;
        ImageView ivPreview = findViewById(R.id.ivPreviewImagem);
        ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        TextView tvHint = findViewById(R.id.tvImagemHint);
        tvHint.setText("Toque para adicionar uma imagem");

        // Limpa chaves públicas e suas seleções
        restricoesPerfil.clear();
        for (ProfileKey key : allKeys) {
            key.getSelectedValues().clear();  // Limpa seleções de cada chave
        }
        chavesFiltradas.clear();
        chavesFiltradas.addAll(allKeys);
        if (keyAdapter != null) {
            keyAdapter.notifyDataSetChanged();
        }
        atualizarVisibilidadeChaves();
        Log.d(TAG, "Campos limpos - Chaves resetadas");
    }

    private void resetarDataHora(TextView textView, String textoPadrao) {
        if (textView != null) {
            textView.setText(textoPadrao);
            textView.setTextColor(getColor(android.R.color.darker_gray));
            Log.d(TAG, "Placeholder definido: " + textoPadrao);
        } else {
            Log.e(TAG, "TextView null - Não reseta data/hora");
        }
    }
}