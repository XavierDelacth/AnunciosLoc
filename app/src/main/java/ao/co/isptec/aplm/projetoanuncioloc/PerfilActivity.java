package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import com.google.android.material.tabs.TabLayout;

public class PerfilActivity extends AppCompatActivity {

    private Button btnBack, btnChangePassword, btnAddKey;
    private ImageButton btnClosePassword, btnEditName, btnConfirmEdit, btnCancelEdit;
    private TextView tvUsername;
    private EditText etUsername;
    private LinearLayout layoutEditButtons;
    private SearchView searchView;
    private TabLayout tabLayout;

    private String currentUsername = "João Silva";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        initViews();
        setupProfileSection();
        setupClickListeners();
        setupTabs();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnClosePassword = findViewById(R.id.btnClosePassword);
        tvUsername = findViewById(R.id.tvUsername);
        etUsername = findViewById(R.id.etUsername);
        btnEditName = findViewById(R.id.btnEditName);
        btnConfirmEdit = findViewById(R.id.btnConfirmEdit);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        layoutEditButtons = findViewById(R.id.layoutEditButtons);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnAddKey = findViewById(R.id.btnAddKey);
        searchView = findViewById(R.id.searchView);
        tabLayout = findViewById(R.id.tabLayout);
    }

    private void setupProfileSection() {
        tvUsername.setText(currentUsername);
        etUsername.setText(currentUsername);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(PerfilActivity.this, "Selecionado: " + tab.getText(), Toast.LENGTH_SHORT).show();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClosePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Sessão encerrada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnEditName.setOnClickListener(v -> startEditingName());
        btnConfirmEdit.setOnClickListener(v -> confirmNameEdit());
        btnCancelEdit.setOnClickListener(v -> cancelNameEdit());

        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(this, AlterarSenhaActivity.class)));
        btnAddKey.setOnClickListener(v -> startActivity(new Intent(this, AddicionarKeyActivity.class)));
    }

    private void startEditingName() {
        tvUsername.setVisibility(View.GONE);
        etUsername.setVisibility(View.VISIBLE);
        layoutEditButtons.setVisibility(View.VISIBLE);
        btnEditName.setVisibility(View.GONE);
        etUsername.requestFocus();
        etUsername.setSelection(etUsername.getText().length());
    }

    private void confirmNameEdit() {
        String newName = etUsername.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(this, "O nome não pode estar vazio", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUsername = newName;
        tvUsername.setText(newName);
        endEditingName();
        Toast.makeText(this, "Nome atualizado!", Toast.LENGTH_SHORT).show();
    }

    private void cancelNameEdit() {
        etUsername.setText(currentUsername);
        endEditingName();
    }

    private void endEditingName() {
        tvUsername.setVisibility(View.VISIBLE);
        etUsername.setVisibility(View.GONE);
        layoutEditButtons.setVisibility(View.GONE);
        btnEditName.setVisibility(View.VISIBLE);
    }
}