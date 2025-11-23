package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.ProfileKeyAdapter;
import android.util.Log;
import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnLogout, btnEditUsername, btnBack, btnSaveUsername, btnCancelUsername, btnAddKey;
    private TextView tvUsername, btnTabMyKeys, btnTabPublicKeys;
    private EditText etUsername, etSearchKeys;
    private LinearLayout layoutUsername, layoutEditUsername, layoutEmptyState;
    private Button btnChangePassword;
    private RecyclerView rvKeys;

    // Data
    private boolean isMyKeysTab = true;
    private ProfileKeyAdapter adapter;
    private List<ProfileKey> allKeys;
    private Map<String, List<String>> mySelectedKeys;

    // Chave para SharedPreferences
    private static final String PREFS_SELECTIONS = "my_profile_selections";
    private Long currentUserId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        initializeViews();

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Usuário");
        tvUsername.setText(username);

        // Validação de login
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // guarda userId actual para usar nas prefs de seleção
        this.currentUserId = userId;

        initializeData();
        setupRecyclerView();
        setupListeners();
        carregarPerfisDoBackend();
    }

    private void initializeViews() {
        // Header
        btnLogout = findViewById(R.id.btn_logout);
        btnEditUsername = findViewById(R.id.btn_edit_username);
        btnSaveUsername = findViewById(R.id.btn_save_username);
        btnBack = findViewById(R.id.btn_back);
        btnCancelUsername = findViewById(R.id.btn_cancel_username);
        tvUsername = findViewById(R.id.tv_username);
        etUsername = findViewById(R.id.et_username);
        layoutUsername = findViewById(R.id.layout_username);
        layoutEditUsername = findViewById(R.id.layout_edit_username);
        btnChangePassword = findViewById(R.id.btn_change_password);

        // Keys Section
        btnTabMyKeys = findViewById(R.id.tv_tab_my_keys);
        btnTabPublicKeys = findViewById(R.id.tv_tab_public_keys);
        etSearchKeys = findViewById(R.id.et_search_keys);
        btnAddKey = findViewById(R.id.btn_add_key);
        rvKeys = findViewById(R.id.rv_keys);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
    }

    // ✅ CARREGA PERFIS DO BACKEND (CATÁLOGO PÚBLICO)
    private void carregarPerfisDoBackend() {
        Call<List<ProfileKey>> call = RetrofitClient.getApiService(this).getAllPerfis();
        call.enqueue(new Callback<List<ProfileKey>>() {
            @Override
            public void onResponse(Call<List<ProfileKey>> call, Response<List<ProfileKey>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allKeys.clear();
                    allKeys.addAll(response.body());

                    // ✅ RESTAURA AS SELEÇÕES PESSOAIS DO USUÁRIO
                    restaurarSelecoesPessoais();

                    updateKeysList();
                } else {
                    Toast.makeText(PerfilActivity.this, "Erro ao carregar perfis", Toast.LENGTH_SHORT).show();
                    updateKeysList();
                }
            }

            @Override
            public void onFailure(Call<List<ProfileKey>> call, Throwable t) {
                Toast.makeText(PerfilActivity.this, "Falha na rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateKeysList();
            }
        });
    }

    // ✅ INICIALIZA DADOS
    private void initializeData() {
        allKeys = new ArrayList<>();
        mySelectedKeys = new HashMap<>();

        // Carrega seleções salvas do usuário
        carregarSelecoesSalvas();
    }

    // ✅ CONFIGURA RECYCLERVIEW
    private void setupRecyclerView() {
        adapter = new ProfileKeyAdapter(this, new ArrayList<>(), isMyKeysTab);
        adapter.setOnValueClickListener((keyName, value) -> {
            toggleValueSelection(keyName, value);
        });

        rvKeys.setLayoutManager(new LinearLayoutManager(this));
        rvKeys.setAdapter(adapter);
    }

    // ✅ CONFIGURA LISTENERS
    private void setupListeners() {
        // Logout
        btnLogout.setOnClickListener(v -> logout());

        // Change Password
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, AlterarSenhaActivity.class);
            startActivity(intent);
        });

        // Edit Username
        btnEditUsername.setOnClickListener(v -> {
            layoutUsername.setVisibility(View.GONE);
            layoutEditUsername.setVisibility(View.VISIBLE);
            etUsername.setText(tvUsername.getText());
            etUsername.requestFocus();
        });

        btnCancelUsername.setOnClickListener(v -> {
            layoutEditUsername.setVisibility(View.GONE);
            layoutUsername.setVisibility(View.VISIBLE);
        });

        btnSaveUsername.setOnClickListener(v -> {
            String novoNome = etUsername.getText().toString().trim();
            if (!novoNome.isEmpty()) {
                tvUsername.setText(novoNome);
                // Aqui você pode salvar o novo nome no backend se quiser
            }
            layoutEditUsername.setVisibility(View.GONE);
            layoutUsername.setVisibility(View.VISIBLE);
        });

        // Tabs
        btnTabMyKeys.setOnClickListener(v -> switchTab(true));
        btnTabPublicKeys.setOnClickListener(v -> switchTab(false));

        // Back
        btnBack.setOnClickListener(v -> finish());

        // Search
        etSearchKeys.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterKeys(s.toString());
            }
        });

        // Add Key
        btnAddKey.setOnClickListener(v -> showAddKeyDialog());
    }

    // ✅ ALTERA ENTRE ABAS
    private void switchTab(boolean isMyKeys) {
        isMyKeysTab = isMyKeys;

        if (isMyKeys) {
            btnTabMyKeys.setBackgroundResource(R.drawable.bg_tab_selected);
            btnTabMyKeys.setTextColor(getResources().getColor(R.color.white));
            btnTabPublicKeys.setBackgroundResource(R.drawable.bg_tab_unselected);
            btnTabPublicKeys.setTextColor(getResources().getColor(R.color.teal_700));
        } else {
            btnTabPublicKeys.setBackgroundResource(R.drawable.bg_tab_selected);
            btnTabPublicKeys.setTextColor(getResources().getColor(R.color.white));
            btnTabMyKeys.setBackgroundResource(R.drawable.bg_tab_unselected);
            btnTabMyKeys.setTextColor(getResources().getColor(R.color.teal_700));
        }

        etSearchKeys.setText("");
        adapter.setShowOnlySelected(isMyKeys);
        updateKeysList();
    }

    // ✅ ATUALIZA LISTA DE CHAVES
    private void updateKeysList() {
        List<ProfileKey> displayKeys = new ArrayList<>();

        if (isMyKeysTab) {
            // Mostra apenas chaves com valores selecionados
            for (ProfileKey key : allKeys) {
                if (!key.getSelectedValues().isEmpty()) {
                    displayKeys.add(key);
                }
            }
        } else {
            // Mostra todas as chaves
            displayKeys.addAll(allKeys);
        }

        adapter.updateKeys(displayKeys);

        // Mostra/oculta estado vazio
        if (displayKeys.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvKeys.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvKeys.setVisibility(View.VISIBLE);
        }
    }

    // ✅ FILTRA CHAVES NA BUSCA
    private void filterKeys(String query) {
        List<ProfileKey> filtered = new ArrayList<>();

        for (ProfileKey key : allKeys) {
            if (isMyKeysTab && key.getSelectedValues().isEmpty()) {
                continue;
            }

            if (key.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(key);
                continue;
            }

            for (String value : key.getAvailableValues()) {
                if (value.toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(key);
                    break;
                }
            }
        }

        adapter.updateKeys(filtered);
    }

    // ✅ ALTERA SELEÇÃO DE VALORES
    private void toggleValueSelection(String keyName, String value) {
        for (ProfileKey key : allKeys) {
            if (key.getName().equals(keyName)) {
                if (key.getSelectedValues().contains(value)) {
                    // deselect -> remove on server
                    key.getSelectedValues().remove(value);
                } else {
                    key.getSelectedValues().add(value);
                }

                // Atualiza mapa de seleções
                if (key.getSelectedValues().isEmpty()) {
                    mySelectedKeys.remove(keyName);
                } else {
                    mySelectedKeys.put(keyName, new ArrayList<>(key.getSelectedValues()));
                }

                // ✅ SALVA SELEÇÕES LOCALMENTE
                salvarSelecoes();

                // Envia alteração ao backend (POST para adicionar, DELETE para remover valor)
                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                Long userId = prefs.getLong("userId", -1L);
                if (userId != -1L) {
                    if (key.getSelectedValues().contains(value)) {
                        // foi adicionado
                        Call<ao.co.isptec.aplm.projetoanuncioloc.Model.User> call = RetrofitClient.getApiService(this).adicionarPerfil(userId, keyName, value);
                        call.enqueue(new Callback<ao.co.isptec.aplm.projetoanuncioloc.Model.User>() {
                            @Override
                            public void onResponse(Call<ao.co.isptec.aplm.projetoanuncioloc.Model.User> call, Response<ao.co.isptec.aplm.projetoanuncioloc.Model.User> response) {
                                if (!response.isSuccessful()) {
                                    // rollback local change
                                    key.getSelectedValues().remove(value);
                                    if (key.getSelectedValues().isEmpty()) mySelectedKeys.remove(keyName);
                                    else mySelectedKeys.put(keyName, new ArrayList<>(key.getSelectedValues()));
                                    salvarSelecoes();
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(PerfilActivity.this, "Erro ao salvar perfil no servidor", Toast.LENGTH_SHORT).show();
                                }
                            }

                                        @Override
                                        public void onFailure(Call<ao.co.isptec.aplm.projetoanuncioloc.Model.User> call, Throwable t) {
                                            // rollback local change
                                            key.getSelectedValues().remove(value);
                                            if (key.getSelectedValues().isEmpty()) mySelectedKeys.remove(keyName);
                                            else mySelectedKeys.put(keyName, new ArrayList<>(key.getSelectedValues()));
                                            salvarSelecoes();
                                            adapter.notifyDataSetChanged();
                                            Log.e("PerfilActivity", "Erro ao adicionar perfil: ", t);
                                            String msg = t.getMessage() == null ? "Erro de rede" : t.getMessage();
                                            Toast.makeText(PerfilActivity.this, "Falha na rede: " + msg, Toast.LENGTH_SHORT).show();
                                        }
                        });
                    } else {
                        // foi removido
                        Call<ao.co.isptec.aplm.projetoanuncioloc.Model.User> call = RetrofitClient.getApiService(this).removerPerfilValor(userId, keyName, value);
                        call.enqueue(new Callback<ao.co.isptec.aplm.projetoanuncioloc.Model.User>() {
                            @Override
                            public void onResponse(Call<ao.co.isptec.aplm.projetoanuncioloc.Model.User> call, Response<ao.co.isptec.aplm.projetoanuncioloc.Model.User> response) {
                                if (!response.isSuccessful()) {
                                    // rollback local change (re-add)
                                    if (!key.getSelectedValues().contains(value)) key.getSelectedValues().add(value);
                                    mySelectedKeys.put(keyName, new ArrayList<>(key.getSelectedValues()));
                                    salvarSelecoes();
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(PerfilActivity.this, "Erro ao remover perfil no servidor", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ao.co.isptec.aplm.projetoanuncioloc.Model.User> call, Throwable t) {
                                // rollback local change (re-add)
                                if (!key.getSelectedValues().contains(value)) key.getSelectedValues().add(value);
                                mySelectedKeys.put(keyName, new ArrayList<>(key.getSelectedValues()));
                                salvarSelecoes();
                                adapter.notifyDataSetChanged();
                                Log.e("PerfilActivity", "Erro ao remover valor de perfil: ", t);
                                String msg = t.getMessage() == null ? "Erro de rede" : t.getMessage();
                                Toast.makeText(PerfilActivity.this, "Falha na rede: " + msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                adapter.notifyDataSetChanged();

                if (isMyKeysTab) {
                    updateKeysList();
                }
                break;
            }
        }
    }

    // ✅ ADICIONA NOVA CHAVE PÚBLICA
    private void showAddKeyDialog() {
        AdicionarKeyDialog dialog = AdicionarKeyDialog.newInstance(allKeys, mySelectedKeys);
        dialog.setOnKeyAddedListener((keyName, values) -> {
            salvarNovaChaveNoBackend(keyName, values);
        });
        dialog.show(getSupportFragmentManager(), "AddKeyDialog");
    }

    // ✅ SALVA NOVA CHAVE NO BACKEND (CATÁLOGO PÚBLICO)
    private void salvarNovaChaveNoBackend(String keyName, List<String> values) {
        ProfileKey existingKey = findKeyByName(keyName);

        if (existingKey == null) {
            // Chave nova - cria no catálogo público
            Map<String, Object> request = new HashMap<>();
            request.put("chave", keyName);
            request.put("valores", values);

            Call<ProfileKey> call = RetrofitClient.getApiService(this).criarPerfil(request);
            call.enqueue(new Callback<ProfileKey>() {
                @Override
                public void onResponse(Call<ProfileKey> call, Response<ProfileKey> response) {
                    if (response.isSuccessful()) {
                        // Recarrega o catálogo público
                        carregarPerfisDoBackend();
                        Toast.makeText(PerfilActivity.this, "Chave pública adicionada!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PerfilActivity.this, "Erro ao criar chave", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ProfileKey> call, Throwable t) {
                    Log.e("PerfilActivity", "Erro ao criar chave pública: ", t);
                    String msg = t.getMessage() == null ? "Erro de rede" : t.getMessage();
                    Toast.makeText(PerfilActivity.this, "Falha na rede: " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Chave existente - adiciona valores ao catálogo público
            Call<ProfileKey> call = RetrofitClient.getApiService(this).adicionarValores(keyName, values);
            call.enqueue(new Callback<ProfileKey>() {
                @Override
                public void onResponse(Call<ProfileKey> call, Response<ProfileKey> response) {
                    if (response.isSuccessful()) {
                        // Recarrega o catálogo público
                        carregarPerfisDoBackend();
                        Toast.makeText(PerfilActivity.this, "Valores adicionados à chave pública!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PerfilActivity.this, "Erro ao adicionar valores", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ProfileKey> call, Throwable t) {
                    Log.e("PerfilActivity", "Erro ao adicionar valores à chave pública: ", t);
                    String msg = t.getMessage() == null ? "Erro de rede" : t.getMessage();
                    Toast.makeText(PerfilActivity.this, "Falha na rede: " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // ✅ BUSCA CHAVE POR NOME
    private ProfileKey findKeyByName(String keyName) {
        for (ProfileKey key : allKeys) {
            if (key.getName().equals(keyName)) {
                return key;
            }
        }
        return null;
    }

    // ✅ 🎯 MÉTODOS CRÍTICOS: GERENCIAMENTO DE SELEÇÕES PESSOAIS

    // CARREGA SELEÇÕES SALVAS DO USUÁRIO
    private void carregarSelecoesSalvas() {
        SharedPreferences prefs = getSharedPreferences(PREFS_SELECTIONS + "_" + currentUserId, MODE_PRIVATE);
        String json = prefs.getString("selections", "{}");

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
        Map<String, List<String>> savedSelections = gson.fromJson(json, type);

        if (savedSelections != null) {
            mySelectedKeys.clear();
            mySelectedKeys.putAll(savedSelections);
        }
    }

    // SALVA SELEÇÕES DO USUÁRIO LOCALMENTE
    private void salvarSelecoes() {
           SharedPreferences prefs = getSharedPreferences(PREFS_SELECTIONS + "_" + currentUserId, MODE_PRIVATE);
           Gson gson = new Gson();
           String json = gson.toJson(mySelectedKeys);

           prefs.edit().putString("selections", json).apply();
    }

    // RESTAURA SELEÇÕES PESSOAIS NAS CHAVES PÚBLICAS
    private void restaurarSelecoesPessoais() {
        for (ProfileKey key : allKeys) {
            // Limpa seleções anteriores e restaura as salvas
            key.setSelectedValues(new ArrayList<>());

            if (mySelectedKeys.containsKey(key.getName())) {
                List<String> valoresSelecionados = mySelectedKeys.get(key.getName());
                // Apenas adiciona valores que existem na chave pública
                for (String valor : valoresSelecionados) {
                    if (key.getAvailableValues().contains(valor)) {
                        key.getSelectedValues().add(valor);
                    }
                }
            }
        }
    }

    // ✅ LOGOUT
    private void logout() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            Toast.makeText(this, "Erro: Faça login novamente", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<Void> call = RetrofitClient.getApiService(this).logout(userId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                prefs.edit().clear().apply();
                // Limpa também as seleções pessoais associadas ao utilizador que fez logout
                getSharedPreferences(PREFS_SELECTIONS + "_" + currentUserId, MODE_PRIVATE).edit().clear().apply();
                // Também limpa as seleções locais se quiser
                // getSharedPreferences(PREFS_SELECTIONS, MODE_PRIVATE).edit().clear().apply();

                Toast.makeText(PerfilActivity.this, "Logout realizado com sucesso!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PerfilActivity.this, LoginActivity.class));
                finishAffinity();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(PerfilActivity.this, "Erro de rede", Toast.LENGTH_SHORT).show();
            }
        });
    }
}