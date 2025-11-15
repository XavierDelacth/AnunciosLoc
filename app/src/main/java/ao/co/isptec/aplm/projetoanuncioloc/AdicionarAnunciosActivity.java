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

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.ProfileKeyAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;

public class AdicionarAnunciosActivity extends AppCompatActivity implements AdicionarKeyDialog.OnKeyAddedListener {

    private static final String TAG = "AActivity";
    public static final int REQUEST_CODE_EDIT = 1001;

    // Modo da activity (criar ou editar)
    private boolean modoEdicao = false;
    private Anuncio anuncioParaEditar = null;
    private int posicaoAnuncio = -1;

    // Views do layout
    private ImageView btnBack, btnAddLocation, btnAdicionarChave;
    private TextView tvDataInicio, tvDataFim, tvHoraInicio, tvHoraFim, tvTipoRestricao,
            tvModoEntrega, tvLocalSelecionado, tvTituloTela;
    private EditText etTitulo, etMensagem, etPesquisarChaves;
    private LinearLayout llLocal, llDataInicio, llDataFim, llHoraInicio, llHoraFim,
            llTipoRestricao, llModoEntrega, llImagem, layoutEmptyChaves;
    private Spinner spinnerRestricao, spinnerModoEntrega, spinnerLocais;
    private CardView cardChavesContainer;
    private Button btnPublicar;
    private RecyclerView rvChavesRestricoes;
    private ProfileKeyAdapter keyAdapter;
    private String localSelecionado = null;
    private String caminhoImagem = null;

    // Para restrições (chaves públicas)
    private Map<String, List<String>> restricoesPerfil = new HashMap<>();
    private List<ProfileKey> allKeys;
    private List<ProfileKey> chavesFiltradas = new ArrayList<>();

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
                        tvLocalSelecionado.setTextColor(getColor(android.R.color.black));
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

        // Verifica se está em modo edição
        verificarModoEdicao();

        initViews();
        if (!initViewsSucesso) {
            Toast.makeText(this, "Erro ao carregar layout. Verifica XML.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initRestricoes();
        setupSpinner();
        setupSpinnerModoEntrega();
        setupSpinnerLocais();
        setupClickListeners();
        atualizarVisibilidadeChaves();

        // Se está em modo edição, preenche os campos
        if (modoEdicao && anuncioParaEditar != null) {
            preencherDadosParaEdicao();
        } else {
            resetarDataHora(tvDataInicio, "dd/mm/aaaa");
            resetarDataHora(tvDataFim, "dd/mm/aaaa");
            resetarDataHora(tvHoraInicio, "hh:mm");
            resetarDataHora(tvHoraFim, "hh:mm");
        }

        Log.d(TAG, "onCreate concluído");
    }

    private void verificarModoEdicao() {
        Intent intent = getIntent();
        if (intent.hasExtra("anuncio")) {
            modoEdicao = true;
            anuncioParaEditar = intent.getParcelableExtra("anuncio");
            posicaoAnuncio = intent.getIntExtra("position", -1);
            Log.d(TAG, "Modo EDIÇÃO ativado - Posição: " + posicaoAnuncio);
        } else {
            modoEdicao = false;
            Log.d(TAG, "Modo CRIAÇÃO ativado");
        }
    }

    private void preencherDadosParaEdicao() {
        Log.d(TAG, "Preenchendo dados para edição");

        // Título da tela
        if (tvTituloTela != null) {
            tvTituloTela.setText("Editar Anúncio");
        }

        // Botão
        if (btnPublicar != null) {
            btnPublicar.setText("Salvar Alterações");
        }

        // Campos básicos
        if (etTitulo != null) {
            etTitulo.setText(anuncioParaEditar.titulo);
        }

        if (etMensagem != null) {
            etMensagem.setText(anuncioParaEditar.descricao);
        }

        // Local
        if (anuncioParaEditar.local != null && !anuncioParaEditar.local.isEmpty()) {
            localSelecionado = anuncioParaEditar.local;
            if (tvLocalSelecionado != null) {
                tvLocalSelecionado.setText(localSelecionado);
                tvLocalSelecionado.setTextColor(getColor(android.R.color.black));
            }
            // Seleciona no spinner se existir
            selecionarNoSpinnerLocais(localSelecionado);
        }

        // Imagem
        if (anuncioParaEditar.imagem != null && !anuncioParaEditar.imagem.isEmpty()) {
            caminhoImagem = anuncioParaEditar.imagem;
            ImageView ivPreview = findViewById(R.id.ivPreviewImagem);
            TextView tvHint = findViewById(R.id.tvImagemHint);
            if (ivPreview != null && tvHint != null) {
                ivPreview.setImageURI(Uri.parse(caminhoImagem));
                tvHint.setText("Imagem selecionada");
            }
        }

        // Datas e horas
        if (tvDataInicio != null && anuncioParaEditar.dataInicio != null) {
            tvDataInicio.setText(anuncioParaEditar.dataInicio);
            tvDataInicio.setTextColor(getColor(android.R.color.black));
        }

        if (tvDataFim != null && anuncioParaEditar.dataFim != null) {
            tvDataFim.setText(anuncioParaEditar.dataFim);
            tvDataFim.setTextColor(getColor(android.R.color.black));
        }

        if (tvHoraInicio != null && anuncioParaEditar.horaInicio != null) {
            tvHoraInicio.setText(anuncioParaEditar.horaInicio);
            tvHoraInicio.setTextColor(getColor(android.R.color.black));
        }

        if (tvHoraFim != null && anuncioParaEditar.horaFim != null) {
            tvHoraFim.setText(anuncioParaEditar.horaFim);
            tvHoraFim.setTextColor(getColor(android.R.color.black));
        }

        // Tipo de restrição
        if (anuncioParaEditar.tipoRestricao != null) {
            if (tvTipoRestricao != null) {
                tvTipoRestricao.setText(anuncioParaEditar.tipoRestricao);
            }
            selecionarNoSpinnerRestricao(anuncioParaEditar.tipoRestricao);
        }

        // Modo de entrega
        if (anuncioParaEditar.modoEntrega != null) {
            if (tvModoEntrega != null) {
                tvModoEntrega.setText(anuncioParaEditar.modoEntrega);
            }
            selecionarNoSpinnerModoEntrega(anuncioParaEditar.modoEntrega);
        }

        // Chaves de restrição
        if (anuncioParaEditar.getChavesPerfil() != null && !anuncioParaEditar.getChavesPerfil() .isEmpty()) {
            restricoesPerfil.clear();
            restricoesPerfil.putAll(anuncioParaEditar.getChavesPerfil() );

            // Marca os valores selecionados nas chaves
            for (Map.Entry<String, List<String>> entry : restricoesPerfil.entrySet()) {
                String keyName = entry.getKey();
                List<String> selectedValues = entry.getValue();

                for (ProfileKey key : allKeys) {
                    if (key.getName().equals(keyName)) {
                        for (String value : selectedValues) {
                            key.toggleValue(value);
                        }
                        break;
                    }
                }
            }

            if (keyAdapter != null) {
                keyAdapter.notifyDataSetChanged();
            }
        }

        Log.d(TAG, "Dados preenchidos para edição");
    }

    private void selecionarNoSpinnerLocais(String local) {
        if (spinnerLocais == null) return;

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerLocais.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(local)) {
                    spinnerLocais.setSelection(i, false);
                    break;
                }
            }
        }
    }

    private void selecionarNoSpinnerRestricao(String restricao) {
        if (spinnerRestricao == null) return;

        String[] restricoes = {"Whitelist", "Blacklist"};
        for (int i = 0; i < restricoes.length; i++) {
            if (restricoes[i].equals(restricao)) {
                spinnerRestricao.setSelection(i, false);
                break;
            }
        }
    }

    private void selecionarNoSpinnerModoEntrega(String modo) {
        if (spinnerModoEntrega == null) return;

        String[] modos = {"Centralizado", "Descentralizado"};
        for (int i = 0; i < modos.length; i++) {
            if (modos[i].equals(modo)) {
                spinnerModoEntrega.setSelection(i, false);
                break;
            }
        }
    }

    private boolean initViewsSucesso = true;

    private void initViews() {
        Log.d(TAG, "initViews chamado - Inicializando views");
        initViewsSucesso = true;

        btnBack = findViewById(R.id.btnBack);
        if (btnBack == null) { Log.e(TAG, "btnBack não encontrado!"); initViewsSucesso = false; }

        // Título da tela (pode não existir no XML atual, será adicionado depois se necessário)
        tvTituloTela = findViewById(R.id.tvTitulo);

        llLocal = findViewById(R.id.llLocal);
        if (llLocal == null) { Log.e(TAG, "llLocal não encontrado!"); initViewsSucesso = false; }

        btnAddLocation = findViewById(R.id.btnAddLocation);
        if (btnAddLocation == null) { Log.e(TAG, "btnAddLocation não encontrado!"); initViewsSucesso = false; }

        tvLocalSelecionado = findViewById(R.id.tvLocalSelecionado);
        if (tvLocalSelecionado == null) { Log.e(TAG, "tvLocalSelecionado não encontrado!"); initViewsSucesso = false; }

        etTitulo = findViewById(R.id.etTitulo);
        if (etTitulo == null) { Log.e(TAG, "etTitulo não encontrado!"); initViewsSucesso = false; }

        etMensagem = findViewById(R.id.etMensagem);
        if (etMensagem == null) { Log.e(TAG, "etMensagem não encontrado!"); initViewsSucesso = false; }

        llImagem = findViewById(R.id.llImagem);
        if (llImagem == null) { Log.e(TAG, "llImagem não encontrado!"); initViewsSucesso = false; }

        llDataInicio = findViewById(R.id.llDataInicio);
        llDataFim = findViewById(R.id.llDataFim);
        llHoraInicio = findViewById(R.id.llHoraInicio);
        llHoraFim = findViewById(R.id.llHoraFim);

        tvDataInicio = findViewById(R.id.tvDataInicio);
        tvDataFim = findViewById(R.id.tvDataFim);
        tvHoraInicio = findViewById(R.id.tvHoraInicio);
        tvHoraFim = findViewById(R.id.tvHoraFim);

        llTipoRestricao = findViewById(R.id.llTipoRestricao);
        tvTipoRestricao = findViewById(R.id.tvTipoRestricao);
        spinnerRestricao = findViewById(R.id.spinnerRestricao);

        llModoEntrega = findViewById(R.id.llModoEntrega);
        tvModoEntrega = findViewById(R.id.tvModoEntrega);
        spinnerModoEntrega = findViewById(R.id.spinnerModoEntrega);

        spinnerLocais = findViewById(R.id.spinnerLocais);
        btnPublicar = findViewById(R.id.btnPublicar);

        etPesquisarChaves = findViewById(R.id.etPesquisarChaves);
        btnAdicionarChave = findViewById(R.id.btnAdicionarChave);
        rvChavesRestricoes = findViewById(R.id.rv_chaves_restricoes);
        layoutEmptyChaves = findViewById(R.id.layout_empty_chaves);
        cardChavesContainer = findViewById(R.id.card_chaves_container);

        if (tvTipoRestricao != null) {
            tvTipoRestricao.setText("Whitelist");
        }

        if (tvModoEntrega != null) {
            tvModoEntrega.setText("Centralizado");
        }

        Log.d(TAG, "initViews concluído - Status: " + (initViewsSucesso ? "OK" : "FALHOU"));
    }

    private void initRestricoes() {
        Log.d(TAG, "initRestricoes chamado");
        allKeys = new ArrayList<>();
        allKeys.add(new ProfileKey("Gênero", Arrays.asList("Masculino", "Feminino", "Outro")));
        allKeys.add(new ProfileKey("Idade", Arrays.asList("18-24", "25-34", "35+")));
        allKeys.add(new ProfileKey("Clube Favorito", Arrays.asList("Real Madrid", "Barcelona", "Benfica")));

        chavesFiltradas.clear();
        chavesFiltradas.addAll(allKeys);

        if (rvChavesRestricoes != null && keyAdapter == null) {
            rvChavesRestricoes.setLayoutManager(new LinearLayoutManager(this));
            keyAdapter = new ProfileKeyAdapter(this, chavesFiltradas, false);

            keyAdapter.setOnValueClickListener((keyName, value) -> {
                for (ProfileKey key : allKeys) {
                    if (key.getName().equals(keyName)) {
                        key.toggleValue(value);

                        if (key.hasSelectedValues()) {
                            restricoesPerfil.put(keyName, new ArrayList<>(key.getSelectedValues()));
                        } else {
                            restricoesPerfil.remove(keyName);
                        }
                        break;
                    }
                }
                keyAdapter.notifyDataSetChanged();
            });

            rvChavesRestricoes.setAdapter(keyAdapter);
        }

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
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners chamado");

        btnBack.setOnClickListener(v -> finish());

        if (btnAddLocation != null) {
            btnAddLocation.setOnClickListener(v -> {
                Intent intent = new Intent(AdicionarAnunciosActivity.this, AdicionarLocalActivity.class);
                localLauncher.launch(intent);
            });
        }

        llImagem.setOnClickListener(v -> abrirGaleriaImagem());
        llDataInicio.setOnClickListener(v -> showDatePicker(tvDataInicio));
        llDataFim.setOnClickListener(v -> showDatePicker(tvDataFim));
        llHoraInicio.setOnClickListener(v -> showTimePicker(tvHoraInicio));
        llHoraFim.setOnClickListener(v -> showTimePicker(tvHoraFim));

        btnPublicar.setOnClickListener(v -> {
            if (modoEdicao) {
                salvarAlteracoes();
            } else {
                publicarAnuncio();
            }
        });

        btnAdicionarChave.setOnClickListener(v -> abrirAdicionarKeyDialog());

        if (llTipoRestricao != null && spinnerRestricao != null) {
            llTipoRestricao.setOnClickListener(v -> spinnerRestricao.performClick());
        }

        if (llModoEntrega != null && spinnerModoEntrega != null) {
            llModoEntrega.setOnClickListener(v -> spinnerModoEntrega.performClick());
        }

        if (llLocal != null && spinnerLocais != null) {
            llLocal.setOnClickListener(v -> spinnerLocais.performClick());
        }
    }

    private void filtrarChaves(String query) {
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
    }

    private void atualizarVisibilidadeChaves() {
        if (chavesFiltradas.isEmpty()) {
            rvChavesRestricoes.setVisibility(View.GONE);
            layoutEmptyChaves.setVisibility(View.VISIBLE);
        } else {
            rvChavesRestricoes.setVisibility(View.VISIBLE);
            layoutEmptyChaves.setVisibility(View.GONE);
        }
    }

    private void abrirGaleriaImagem() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagemLauncher.launch(intent);
    }

    private void showDatePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format("%02d/%02d/%04d", day, month + 1, year);
            textView.setText(date);
            textView.setTextColor(getColor(android.R.color.black));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String time = String.format("%02d:%02d", hour, minute);
            textView.setText(time);
            textView.setTextColor(getColor(android.R.color.black));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void setupSpinner() {
        if (spinnerRestricao != null && tvTipoRestricao != null) {
            String[] restricoes = {"Whitelist", "Blacklist"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, restricoes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRestricao.setAdapter(adapter);

            spinnerRestricao.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selected = restricoes[position];
                    tvTipoRestricao.setText(selected);

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
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            spinnerRestricao.setSelection(0, false);
        }
    }

    private void setupSpinnerModoEntrega() {
        if (spinnerModoEntrega != null && tvModoEntrega != null) {
            String[] modos = {"Centralizado", "Descentralizado"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerModoEntrega.setAdapter(adapter);

            spinnerModoEntrega.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    tvModoEntrega.setText(modos[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            spinnerModoEntrega.setSelection(0, false);
        }
    }

    private void setupSpinnerLocais() {
        if (spinnerLocais != null && tvLocalSelecionado != null) {
            String[] locais = {"Benguela", "Largo da Independência", "Belas Shopping", "Ginásio do Camama I"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locais);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLocais.setAdapter(adapter);

            spinnerLocais.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    localSelecionado = locais[position];
                    tvLocalSelecionado.setText(localSelecionado);
                    tvLocalSelecionado.setTextColor(getColor(android.R.color.black));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            // Não define seleção padrão se está em modo edição
            if (!modoEdicao) {
                spinnerLocais.setSelection(0, false);
            }
        }
    }

    private void abrirAdicionarKeyDialog() {
        AdicionarKeyDialog dialog = AdicionarKeyDialog.newInstance(allKeys, restricoesPerfil);
        dialog.setOnKeyAddedListener(this);
        dialog.show(getSupportFragmentManager(), "AdicionarKeyDialog");
    }

    @Override
    public void onKeyAdded(String keyName, List<String> values) {
        restricoesPerfil.put(keyName, values);
        filtrarChaves(etPesquisarChaves.getText().toString());
        if (keyAdapter != null) {
            keyAdapter.notifyDataSetChanged();
        }
        atualizarVisibilidadeChaves();
        Toast.makeText(this, "Chave pública adicionada: " + keyName, Toast.LENGTH_SHORT).show();
    }

    // SALVAR ALTERAÇÕES (modo edição)
    private void salvarAlteracoes() {
        Log.d(TAG, "salvarAlteracoes chamado");

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
            Toast.makeText(this, "Por favor, adicione um local de propagação", Toast.LENGTH_LONG).show();
            return;
        }
        if (titulo.isEmpty()) {
            Toast.makeText(this, "Digite um título para o anúncio", Toast.LENGTH_LONG).show();
            return;
        }
        if (mensagem.isEmpty()) {
            Toast.makeText(this, "Escreva uma mensagem para o anúncio", Toast.LENGTH_LONG).show();
            return;
        }
        if (dataInicio.equals("dd/mm/aaaa") || dataFim.equals("dd/mm/aaaa")) {
            Toast.makeText(this, "Selecione as datas de início e fim", Toast.LENGTH_LONG).show();
            return;
        }
        if (horaInicio.equals("hh:mm") || horaFim.equals("hh:mm")) {
            Toast.makeText(this, "Selecione os horários", Toast.LENGTH_LONG).show();
            return;
        }
        if (!"Nenhuma".equals(restricao) && restricoesPerfil.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos uma chave pública de restrição", Toast.LENGTH_LONG).show();
            return;
        }

        // Atualiza o anúncio existente
        anuncioParaEditar.titulo = titulo;
        anuncioParaEditar.descricao = mensagem;
        anuncioParaEditar.local = localSelecionado;
        anuncioParaEditar.imagem = caminhoImagem;
        anuncioParaEditar.dataInicio = dataInicio;
        anuncioParaEditar.dataFim = dataFim;
        anuncioParaEditar.horaInicio = horaInicio;
        anuncioParaEditar.horaFim = horaFim;
        anuncioParaEditar.tipoRestricao = restricao;
        anuncioParaEditar.modoEntrega = modoEntrega;

        // Atualiza chaves
        anuncioParaEditar.getChavesPerfil().clear();
        for (Map.Entry<String, List<String>> entry : restricoesPerfil.entrySet()) {
            anuncioParaEditar.addChave(entry.getKey(), entry.getValue());
        }

        // Retorna resultado para MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("anuncio_editado", anuncioParaEditar);
        resultIntent.putExtra("position", posicaoAnuncio);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Anúncio atualizado com sucesso!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Anúncio editado e retornado");
        finish();
    }

    // PUBLICAR ANÚNCIO (modo criação)
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
        // TODO: Implementar salvamento no BD ou servidor
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

        restricoesPerfil.clear();
        for (ProfileKey key : allKeys) {
            key.getSelectedValues().clear();
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