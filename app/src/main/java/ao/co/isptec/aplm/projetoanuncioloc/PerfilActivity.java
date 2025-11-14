package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.ProfileKeyAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;


public class PerfilActivity extends AppCompatActivity {

    public static List<ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey> allKeysStatic = new ArrayList<>();
    public static Map<String, List<String>> mySelectedKeysStatic = new HashMap<>();
    // UI Components
    private ImageButton btnLogout, btnEditUsername,btnBack,  btnSaveUsername, btnCancelUsername, btnAddKey;
    private TextView tvUsername;
    private EditText etUsername, etSearchKeys;
    private LinearLayout layoutUsername, layoutEditUsername, layoutEmptyState;
    private Button btnChangePassword, btnTabMyKeys, btnTabPublicKeys;
    private RecyclerView rvKeys;

    // Data
    private String username = "João Silva";
    private boolean isMyKeysTab = true;
    private ProfileKeyAdapter adapter;
    private List<ProfileKey> allKeys;
    private Map<String, List<String>> mySelectedKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        initializeViews();
        initializeData();
        setupRecyclerView();
        setupListeners();
        updateKeysList();
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
        btnBack = findViewById(R.id.btn_back);  // ← ADICIONA ESTA LINHA
        // Keys Section
        btnTabMyKeys = findViewById(R.id.btn_tab_my_keys);
        btnTabPublicKeys = findViewById(R.id.btn_tab_public_keys);
        etSearchKeys = findViewById(R.id.et_search_keys);
        btnAddKey = findViewById(R.id.btn_add_key);
        rvKeys = findViewById(R.id.rv_keys);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
    }

    private void initializeData() {
        // Initialize my selected keys
        mySelectedKeys = new HashMap<>();
        List<String> clubValues = new ArrayList<>();
        clubValues.add("Real Madrid");
        mySelectedKeys.put("Clube", clubValues);

        List<String> professionValues = new ArrayList<>();
        professionValues.add("Estudante");
        mySelectedKeys.put("Profissao", professionValues);

        List<String> interestValues = new ArrayList<>();
        interestValues.add("Tecnologia");
        interestValues.add("Música");
        mySelectedKeys.put("Interesse", interestValues);

        // Initialize all available keys
        allKeys = new ArrayList<>();
        allKeys.add(createKey("Clube", new String[]{
                "Real Madrid", "Barcelona", "1º de Agosto", "Petro de Luanda", "Benfica", "Porto"
        }));
        allKeys.add(createKey("Profissao", new String[]{
                "Estudante", "Professor", "Engenheiro", "Médico", "Designer", "Programador"
        }));
        allKeys.add(createKey("Cidade", new String[]{
                "Luanda", "Benguela", "Huambo", "Lobito", "Namibe", "Lubango"
        }));
        allKeys.add(createKey("Interesse", new String[]{
                "Tecnologia", "Desporto", "Música", "Cinema", "Livros", "Viagens"
        }));

        allKeysStatic.clear();
        allKeysStatic.addAll(allKeys); // COPIA PARA STATIC

        mySelectedKeysStatic.clear();
        mySelectedKeysStatic.putAll(mySelectedKeys);
    }

    private ProfileKey createKey(String name, String[] values) {
        ProfileKey key = new ProfileKey(name);
        List<String> availableValues = new ArrayList<>();
        for (String value : values) {
            availableValues.add(value);
        }
        key.setAvailableValues(availableValues);

        // Set selected values if exist
        if (mySelectedKeys.containsKey(name)) {
            key.setSelectedValues(mySelectedKeys.get(name));
        }

        return key;
    }

    private void setupRecyclerView() {
        adapter = new ProfileKeyAdapter(this, new ArrayList<>(), isMyKeysTab);
        adapter.setOnValueClickListener((keyName, value) -> {
            toggleValueSelection(keyName, value);
        });

        rvKeys.setLayoutManager(new LinearLayoutManager(this));
        rvKeys.setAdapter(adapter);
    }

    private void setupListeners() {
        // Logout
        btnLogout.setOnClickListener(v -> {
            // Limpar dados de login (SharedPreferences, se tiveres)
            getSharedPreferences("login", MODE_PRIVATE).edit().clear().apply();

            Intent intent = new Intent(PerfilActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Em setupListeners() do PerfilActivity.java
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, AlterarSenhaActivity.class);
            startActivity(intent);
        });

        // Edit Username
        // Em setupListeners()
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
            }
            layoutEditUsername.setVisibility(View.GONE);
            layoutUsername.setVisibility(View.VISIBLE);
        });

        // Change Password
        btnChangePassword.setOnClickListener(v -> showPasswordDialog());

        // Tabs
        btnTabMyKeys.setOnClickListener(v -> switchTab(true));
        btnTabPublicKeys.setOnClickListener(v -> switchTab(false));

        // Dentro de setupListeners()
        btnBack.setOnClickListener(v -> finish());  // ← VOLTA PARA A TELA ANTERIOR
        // Search
        etSearchKeys.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterKeys(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Add Key
        btnAddKey.setOnClickListener(v -> showAddKeyDialog());  // ← ADICIONA ESTA LINHA!
    }

    private void showEditUsername() {
        layoutUsername.setVisibility(View.GONE);
        layoutEditUsername.setVisibility(View.VISIBLE);
        etUsername.setText(username);
        etUsername.requestFocus();
    }

    private void saveUsername() {
        String newUsername = etUsername.getText().toString().trim();
        if (newUsername.isEmpty()) {
            Toast.makeText(this, "Nome não pode estar vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        username = newUsername;
        tvUsername.setText(username);
        cancelEditUsername();
        Toast.makeText(this, "Nome atualizado", Toast.LENGTH_SHORT).show();
    }

    private void cancelEditUsername() {
        layoutUsername.setVisibility(View.VISIBLE);
        layoutEditUsername.setVisibility(View.GONE);
    }

    private void showPasswordDialog() {
        // Agora abre a Activity em vez do DialogFragment
        Intent intent = new Intent(PerfilActivity.this, AlterarSenhaActivity.class);
        startActivity(intent);
    }

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


    private void updateKeysList() {
        List<ProfileKey> displayKeys;

        if (isMyKeysTab) {
            // Show only keys with selected values
            displayKeys = new ArrayList<>();
            for (ProfileKey key : allKeys) {
                if (!key.getSelectedValues().isEmpty()) {
                    displayKeys.add(key);
                }
            }
        } else {
            // Show all keys
            displayKeys = new ArrayList<>(allKeys);
        }

        adapter.updateKeys(displayKeys);

        // Show/hide empty state
        if (displayKeys.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvKeys.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvKeys.setVisibility(View.VISIBLE);
        }
    }

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

    private void toggleValueSelection(String keyName, String value) {
        for (ProfileKey key : allKeys) {
            if (key.getName().equals(keyName)) {
                List<String> selected = key.getSelectedValues();
                if (selected.contains(value)) {
                    selected.remove(value);
                } else {
                    selected.add(value);
                }

                // Update map
                if (selected.isEmpty()) {
                    mySelectedKeys.remove(keyName);
                } else {
                    mySelectedKeys.put(keyName, selected);
                }

                if (selected.isEmpty()) {
                    mySelectedKeysStatic.remove(keyName);
                } else {
                    mySelectedKeysStatic.put(keyName, new ArrayList<>(selected));
                }

                adapter.notifyDataSetChanged();

                // Update list if in my keys tab
                if (isMyKeysTab) {
                    updateKeysList();
                }
                break;
            }
        }
    }
    private void showAddKeyDialog() {
        AdicionarKeyDialog dialog = AdicionarKeyDialog.newInstance(allKeys, mySelectedKeys);
        dialog.setOnKeyAddedListener((keyName, values) -> {
            ProfileKey existingKey = findKeyByName(keyName);
            if (existingKey == null) {
                existingKey = new ProfileKey(keyName, values);
                allKeys.add(existingKey);
                allKeysStatic.add(existingKey); // SALVA NA STATIC
            } else {
                existingKey.getAvailableValues().addAll(values);
            }
            updateKeysList();
            Toast.makeText(this, "Chave adicionada!", Toast.LENGTH_SHORT).show();
        });
        dialog.show(getSupportFragmentManager(), "AddKeyDialog");
    }


    private ProfileKey findKeyByName(String keyName) {
        for (ProfileKey key : allKeys) {
            if (key.getName().equals(keyName)) {
                return key;
            }
        }
        return null;  // Não encontrou a chave
    }

}