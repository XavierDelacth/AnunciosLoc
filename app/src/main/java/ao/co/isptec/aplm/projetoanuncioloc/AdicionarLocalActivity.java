package ao.co.isptec.aplm.projetoanuncioloc;

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
import androidx.cardview.widget.CardView;

public class AdicionarLocalActivity extends AppCompatActivity {

    private EditText etSearch;
    private Button btnAddLocal;
    private CardView cardBelas, cardZango, cardCamama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_local); // ← AGORA FUNCIONA!

        initViews();
        setupToolbar();
        setupSearchFilter();
        setupAddButton();
        setupCardClicks();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        btnAddLocal = findViewById(R.id.btnAddLocal);
        cardBelas = findViewById(R.id.cardBelas);
        cardZango = findViewById(R.id.cardZango);
        cardCamama = findViewById(R.id.cardCamama);
    }

    private void setupToolbar() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnProfile).setOnClickListener(v -> showToast("Perfil"));
        findViewById(R.id.btnNotification).setOnClickListener(v -> showToast("Notificações"));
    }

    private void setupSearchFilter() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarLocais(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupAddButton() {
        btnAddLocal.setOnClickListener(v -> {
            String nome = etSearch.getText().toString().trim();
            if (nome.isEmpty()) {
                etSearch.setError("Digite o nome do local");
                etSearch.requestFocus();
                return;
            }
            Intent intent = new Intent(this, ConfirmarLocalActivity.class);
            intent.putExtra("nomeLocal", nome);
            startActivity(intent);
        });
    }

    private void setupCardClicks() {
        View.OnClickListener cardClick = v -> {
            CardView card = (CardView) v;
            LinearLayout innerLayout = (LinearLayout) ((LinearLayout) card.getChildAt(0)).getChildAt(0);
            TextView tvNome = (TextView) innerLayout.getChildAt(0);
            String nomeLocal = tvNome.getText().toString();

            Intent result = new Intent();
            result.putExtra("localSelecionado", nomeLocal);
            setResult(RESULT_OK, result);
            finish();
        };

        cardBelas.setOnClickListener(cardClick);
        cardZango.setOnClickListener(cardClick);
        cardCamama.setOnClickListener(cardClick);
    }

    private void filtrarLocais(String query) {
        CardView[] cards = {cardBelas, cardZango, cardCamama};
        String[] nomes = {"Belas Shopping", "Zango", "Ginásio do Camama"};

        for (int i = 0; i < cards.length; i++) {
            boolean visible = nomes[i].toLowerCase().contains(query.toLowerCase());
            cards[i].setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}