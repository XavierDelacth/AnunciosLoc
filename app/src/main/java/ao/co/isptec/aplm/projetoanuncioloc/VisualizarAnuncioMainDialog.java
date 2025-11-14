package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;
import ao.co.isptec.aplm.projetoanuncioloc.Adapters.ProfileKeyAdapter;

public class VisualizarAnuncioMainDialog extends DialogFragment {

    private static final String TAG = "VisualizarAnuncioDialog";
    private static final String ARG_ANUNCIO = "anuncio";
    private static final String ARG_POSITION = "position";

    public interface BookmarkCallback {
        void onBookmarkChanged(int position, boolean saved);
    }

    private Anuncio anuncio;
    private int position;
    private BookmarkCallback callback;
    private List<ProfileKey> allKeys = new ArrayList<>();
    private List<ProfileKey> chavesFiltradas = new ArrayList<>();
    private ProfileKeyAdapter keyAdapter;

    // Views
    private ImageButton btnClose;
    private ImageView imgAnnouncement;
    private TextView tvTitle, tvContent, tvLocal, tvTipoRestricao, tvDataInicio, tvDataFim;
    private TextView tvHoraInicio, tvHoraFim, tvModoEntrega;
    private EditText etPesquisarChaves;
    private RecyclerView rvChavesRestricoes;
    private LinearLayout layoutEmptyChaves;
    private CardView cardChavesContainer;

    public static VisualizarAnuncioMainDialog newInstance(Anuncio anuncio, int position, BookmarkCallback listener) {
        VisualizarAnuncioMainDialog dialog = new VisualizarAnuncioMainDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANUNCIO, anuncio);
        args.putInt(ARG_POSITION, position);
        dialog.setArguments(args);
        dialog.callback = listener;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            anuncio = getArguments().getParcelable(ARG_ANUNCIO);
            position = getArguments().getInt(ARG_POSITION);
        }
        if (anuncio == null) {
            dismiss();
            return;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_visualizar_anuncio, container, false);
        initViews(view);
        setupClickListeners();
        preencherDados();
        setupChaves();
        return view;
    }

    private void initViews(View view) {
        Log.d(TAG, "Inicializando views no diálogo");
        btnClose = view.findViewById(R.id.btn_close);
        imgAnnouncement = view.findViewById(R.id.img_announcement);
        tvTitle = view.findViewById(R.id.announcementTitle);
        tvContent = view.findViewById(R.id.announcementContent);
        tvLocal = view.findViewById(R.id.tvLocal);
        tvTipoRestricao = view.findViewById(R.id.tvTipoRestricao);
        tvDataInicio = view.findViewById(R.id.tvDataInicio);
        tvDataFim = view.findViewById(R.id.tvDataFim);
        tvHoraInicio = view.findViewById(R.id.tvHoraInicio);
        tvHoraFim = view.findViewById(R.id.tvHoraFim);
        tvModoEntrega = view.findViewById(R.id.tvModoEntrega);
        etPesquisarChaves = view.findViewById(R.id.etPesquisarChaves);
        rvChavesRestricoes = view.findViewById(R.id.rv_chaves_restricoes);
        layoutEmptyChaves = view.findViewById(R.id.layout_empty_chaves);
        cardChavesContainer = view.findViewById(R.id.card_chaves_container);
    }

    private void setupClickListeners() {
        Log.d(TAG, "Configurando listeners no diálogo");
        btnClose.setOnClickListener(v -> dismiss());

        if (etPesquisarChaves != null) {
            etPesquisarChaves.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filtrarChaves(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void preencherDados() {
        if (anuncio == null) return;
        Log.d(TAG, "Preenchendo dados no diálogo");

        tvTitle.setText(anuncio.titulo);
        tvContent.setText(anuncio.descricao);

        // Imagem
        if (anuncio.imagem != null && !anuncio.imagem.isEmpty()) {
            try {
                Uri uri = Uri.parse(anuncio.imagem);
                imgAnnouncement.setImageURI(uri);
                imgAnnouncement.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar imagem: " + e.getMessage());
                imgAnnouncement.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imgAnnouncement.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Informações
        tvLocal.setText(anuncio.local != null ? anuncio.local : "Não especificado");
        tvTipoRestricao.setText(anuncio.getTipoRestricao() != null ? anuncio.getTipoRestricao() : "Nenhuma");
        tvDataInicio.setText(anuncio.getDataInicio() != null ? anuncio.getDataInicio() : "--/--/----");
        tvDataFim.setText(anuncio.getDataFim() != null ? anuncio.getDataFim() : "--/--/----");
        tvHoraInicio.setText(anuncio.getHoraInicio() != null ? anuncio.getHoraInicio() : "--:--");
        tvHoraFim.setText(anuncio.getHoraFim() != null ? anuncio.getHoraFim() : "--:--");
        tvModoEntrega.setText(anuncio.getModoEntrega() != null ? anuncio.getModoEntrega() : "Não especificado");
    }

    private void setupChaves() {
        Log.d(TAG, "=== Iniciando setupChaves ===");

        if (anuncio == null) {
            Log.e(TAG, "Anúncio é null!");
            mostrarEmptyState();
            return;
        }

        Map<String, List<String>> chavesMap = anuncio.getChavesPerfil();

        if (chavesMap == null || chavesMap.isEmpty()) {
            Log.d(TAG, "Sem chaves configuradas");
            mostrarEmptyState();
            return;
        }

        allKeys.clear();
        chavesFiltradas.clear();

        Log.d(TAG, "Processando " + chavesMap.size() + " chaves do anúncio");

        // Carrega as chaves públicas disponíveis
        List<ProfileKey> chavesPublicas = carregarChavesPublicas();

        // Marca os valores que foram selecionados no anúncio
        for (ProfileKey chavePublica : chavesPublicas) {
            String nomeChave = chavePublica.getName();

            Log.d(TAG, "Verificando chave pública: " + nomeChave);

            // Verifica se esta chave foi usada no anúncio
            if (chavesMap.containsKey(nomeChave)) {
                List<String> valoresSelecionados = chavesMap.get(nomeChave);
                Log.d(TAG, "  Encontrada no anúncio! Valores do anúncio: " + valoresSelecionados);

                if (valoresSelecionados != null && !valoresSelecionados.isEmpty()) {
                    // Cria uma cópia da chave com TODOS os valores disponíveis
                    ProfileKey key = new ProfileKey(nomeChave, new ArrayList<>(chavePublica.getAvailableValues()));

                    // CRÍTICO: Limpa qualquer seleção anterior
                    key.getSelectedValues().clear();

                    Log.d(TAG, "  Valores disponíveis na chave: " + key.getAvailableValues());

                    // Adiciona apenas os valores que realmente existem e foram selecionados
                    for (String valorSelecionado : valoresSelecionados) {
                        if (key.getAvailableValues().contains(valorSelecionado)) {
                            key.getSelectedValues().add(valorSelecionado);
                            Log.d(TAG, "    ✓ Valor selecionado adicionado: " + valorSelecionado);
                        } else {
                            Log.w(TAG, "    ✗ Valor '" + valorSelecionado + "' NÃO existe nos valores disponíveis! Ignorando.");
                        }
                    }

                    allKeys.add(key);
                    Log.d(TAG, "  ✓ Chave adicionada com " + key.getSelectedValues().size() + " valores realmente selecionados");
                    Log.d(TAG, "  Seleções finais: " + key.getSelectedValues());
                } else {
                    Log.d(TAG, "  Valores null ou vazios - ignorando");
                }
            } else {
                Log.d(TAG, "  Não foi usada no anúncio - ignorando");
            }
        }

        if (allKeys.isEmpty()) {
            Log.d(TAG, "Nenhuma chave configurada");
            mostrarEmptyState();
            return;
        }

        chavesFiltradas.addAll(allKeys);
        Log.d(TAG, "Total de chaves válidas: " + allKeys.size());

        setupRecyclerView();
    }

    private List<ProfileKey> carregarChavesPublicas() {
        List<ProfileKey> chaves = new ArrayList<>();

        // IMPORTANTE: Usa Arrays.asList() + ArrayList para compatibilidade
        chaves.add(new ProfileKey("Gênero", new ArrayList<>(Arrays.asList("Masculino", "Feminino", "Outro"))));
        chaves.add(new ProfileKey("Idade", new ArrayList<>(Arrays.asList("18-24", "25-34", "35+"))));
        chaves.add(new ProfileKey("Clube Favorito", new ArrayList<>(Arrays.asList("Real Madrid", "Barcelona", "Benfica"))));

        Log.d(TAG, "Chaves públicas carregadas: " + chaves.size());
        return chaves;
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Configurando RecyclerView com " + chavesFiltradas.size() + " chaves");

        rvChavesRestricoes.setLayoutManager(new LinearLayoutManager(getContext()));

        // IMPORTANTE: showOnlySelected = true para modo VISUALIZAÇÃO (somente leitura)
        // Mostra apenas os valores selecionados como texto, SEM botões interativos
        keyAdapter = new ProfileKeyAdapter(getContext(), chavesFiltradas, true);

        // NÃO define listener - modo somente leitura

        rvChavesRestricoes.setAdapter(keyAdapter);

        rvChavesRestricoes.setVisibility(View.VISIBLE);
        layoutEmptyChaves.setVisibility(View.GONE);

        Log.d(TAG, "RecyclerView configurado em modo SOMENTE LEITURA");
    }

    private void mostrarEmptyState() {
        Log.d(TAG, "Mostrando empty state");
        if (rvChavesRestricoes != null) {
            rvChavesRestricoes.setVisibility(View.GONE);
        }
        if (layoutEmptyChaves != null) {
            layoutEmptyChaves.setVisibility(View.VISIBLE);
        }
    }

    private void filtrarChaves(String query) {
        Log.d(TAG, "Filtrando chaves com query: '" + query + "'");
        chavesFiltradas.clear();

        if (query.isEmpty()) {
            chavesFiltradas.addAll(allKeys);
            Log.d(TAG, "Query vazia - mostrando todas as " + allKeys.size() + " chaves");
        } else {
            String queryLower = query.toLowerCase();
            for (ProfileKey key : allKeys) {
                if (key.getName().toLowerCase().contains(queryLower)) {
                    chavesFiltradas.add(key);
                }
            }
            Log.d(TAG, "Filtro resultou em " + chavesFiltradas.size() + " chaves");
        }

        if (keyAdapter != null) {
            keyAdapter.updateKeys(chavesFiltradas);
        }

        boolean hasResults = !chavesFiltradas.isEmpty();
        rvChavesRestricoes.setVisibility(hasResults ? View.VISIBLE : View.GONE);
        layoutEmptyChaves.setVisibility(hasResults ? View.GONE : View.VISIBLE);
    }
}