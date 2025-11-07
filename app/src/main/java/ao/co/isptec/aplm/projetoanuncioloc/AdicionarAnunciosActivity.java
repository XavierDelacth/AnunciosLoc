package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class AdicionarAnunciosActivity extends AppCompatActivity {

    private ImageView btnBack, btnAddLocation, btnAdicionarChave;
    private TextView tvDataInicio, tvDataFim, tvHoraInicio, tvHoraFim, tvTipoRestricao;
    private EditText etMensagem, etPesquisarChaves;
    private LinearLayout llLocal, llDataInicio, llDataFim, llHoraInicio, llHoraFim, llTipoRestricao;
    private Spinner spinnerRestricao;
    private Button btnPublicar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_anuncios);

        initViews();
        setupSpinner();
        setupClickListeners();
        setupChavesExpansivel();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        llLocal = findViewById(R.id.llLocal);
        btnAddLocation = findViewById(R.id.btnAddLocation);
        etMensagem = findViewById(R.id.etMensagem);
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
        btnAdicionarChave = findViewById(R.id.btnAdicionarChave);
        etPesquisarChaves = findViewById(R.id.etPesquisarChaves);
        btnPublicar = findViewById(R.id.btnPublicar);
    }

    private void setupSpinner() {
        String[] restricoes = {"Whitelist", "Blacklist"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, restricoes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRestricao.setAdapter(adapter);

        spinnerRestricao.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                tvTipoRestricao.setText(selected);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnAddLocation.setOnClickListener(v ->
                Toast.makeText(this, "Adicionar Local", Toast.LENGTH_SHORT).show());
        llLocal.setOnClickListener(v ->
                Toast.makeText(this, "Selecionar Local", Toast.LENGTH_SHORT).show());
        btnAdicionarChave.setOnClickListener(v ->
                Toast.makeText(this, "Adicionar Nova Chave", Toast.LENGTH_SHORT).show());
        llTipoRestricao.setOnClickListener(v -> spinnerRestricao.performClick());
        llDataInicio.setOnClickListener(v -> showDatePicker(tvDataInicio));
        llDataFim.setOnClickListener(v -> showDatePicker(tvDataFim));
        llHoraInicio.setOnClickListener(v -> showTimePicker(tvHoraInicio));
        llHoraFim.setOnClickListener(v -> showTimePicker(tvHoraFim));
        btnPublicar.setOnClickListener(v -> publicarAnuncio());

        etPesquisarChaves.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                pesquisarChaves();
                return true;
            }
            return false;
        });
    }

    private void setupChavesExpansivel() {
        // Configurar expansão das chaves
        findViewById(R.id.itemClube).setOnClickListener(v ->
                toggleChaveExpansivel(findViewById(R.id.valoresClube), findViewById(R.id.arrowClube)));

        findViewById(R.id.itemIdade).setOnClickListener(v ->
                toggleChaveExpansivel(findViewById(R.id.valoresIdade), findViewById(R.id.arrowIdade)));

        findViewById(R.id.itemInteresses).setOnClickListener(v ->
                toggleChaveExpansivel(findViewById(R.id.valoresInteresses), findViewById(R.id.arrowInteresses)));
    }

    private void toggleChaveExpansivel(LinearLayout valores, ImageView arrow) {
        if (valores.getVisibility() == View.VISIBLE) {
            valores.setVisibility(View.GONE);
            arrow.setRotation(0);
        } else {
            valores.setVisibility(View.VISIBLE);
            arrow.setRotation(180);
        }
    }

    private void showDatePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    textView.setText(date);
                    textView.setTextColor(getResources().getColor(android.R.color.black));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void showTimePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minute);
                    textView.setText(time);
                    textView.setTextColor(getResources().getColor(android.R.color.black));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePicker.show();
    }

    private void pesquisarChaves() {
        String query = etPesquisarChaves.getText().toString().trim();
        if (!query.isEmpty()) {
            Toast.makeText(this, "Pesquisando por: " + query, Toast.LENGTH_SHORT).show();
        }
    }

    private void publicarAnuncio() {
        String mensagem = etMensagem.getText().toString().trim();
        String restricao = tvTipoRestricao.getText().toString();
        String dataInicio = tvDataInicio.getText().toString();
        String dataFim = tvDataFim.getText().toString();
        String horaInicio = tvHoraInicio.getText().toString();
        String horaFim = tvHoraFim.getText().toString();

        // Validações
        if (mensagem.isEmpty()) {
            showError("Por favor, escreva uma mensagem para o anúncio");
            return;
        }

        if (dataInicio.equals("dd/mm/aaaa") || dataFim.equals("dd/mm/aaaa")) {
            showError("Por favor, selecione as datas de início e fim");
            return;
        }

        if (horaInicio.equals("hh:mm") || horaFim.equals("hh:mm")) {
            showError("Por favor, selecione os horários de início e fim");
            return;
        }

        // Criar objeto anúncio
        Anuncio novoAnuncio = new Anuncio(
                mensagem,
                "Local a definir",
                dataInicio,
                dataFim,
                horaInicio,
                horaFim,
                restricao
        );

        // Simular publicação bem-sucedida
        boolean sucesso = salvarAnuncio(novoAnuncio);

        if (sucesso) {
            Toast.makeText(this, "Anúncio publicado com sucesso!", Toast.LENGTH_SHORT).show();
            limparCampos();
            finish();
        } else {
            showError("Erro ao publicar anúncio. Tente novamente.");
        }
    }

    private boolean salvarAnuncio(Anuncio anuncio) {
        // TODO: Implementar lógica para salvar no banco de dados
        // Por enquanto, simula sucesso
        return true;
    }

    private void showError(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    private void limparCampos() {
        // Limpar mensagem
        etMensagem.setText("");

        // Resetar datas
        resetarDataHora(tvDataInicio, "dd/mm/aaaa");
        resetarDataHora(tvDataFim, "dd/mm/aaaa");
        resetarDataHora(tvHoraInicio, "hh:mm");
        resetarDataHora(tvHoraFim, "hh:mm");

        // Resetar restrição
        tvTipoRestricao.setText("Whitelist");
        spinnerRestricao.setSelection(0);

        // Limpar pesquisa
        etPesquisarChaves.setText("");

        // Fechar chaves expandidas
        fecharTodasChaves();
    }

    private void resetarDataHora(TextView textView, String textoPadrao) {
        textView.setText(textoPadrao);
        textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void fecharTodasChaves() {
        // Fechar chave Clube
        LinearLayout valoresClube = findViewById(R.id.valoresClube);
        ImageView arrowClube = findViewById(R.id.arrowClube);
        if (valoresClube.getVisibility() == View.VISIBLE) {
            valoresClube.setVisibility(View.GONE);
            arrowClube.setRotation(0);
        }

        // Fechar chave Idade
        LinearLayout valoresIdade = findViewById(R.id.valoresIdade);
        ImageView arrowIdade = findViewById(R.id.arrowIdade);
        if (valoresIdade.getVisibility() == View.VISIBLE) {
            valoresIdade.setVisibility(View.GONE);
            arrowIdade.setRotation(0);
        }

        // Fechar chave Interesses
        LinearLayout valoresInteresses = findViewById(R.id.valoresInteresses);
        ImageView arrowInteresses = findViewById(R.id.arrowInteresses);
        if (valoresInteresses.getVisibility() == View.VISIBLE) {
            valoresInteresses.setVisibility(View.GONE);
            arrowInteresses.setRotation(0);
        }
    }

    // Classe interna para representar o anúncio
    private static class Anuncio {
        private String mensagem;
        private String local;
        private String dataInicio;
        private String dataFim;
        private String horaInicio;
        private String horaFim;
        private String tipoRestricao;

        public Anuncio(String mensagem, String local, String dataInicio, String dataFim,
                       String horaInicio, String horaFim, String tipoRestricao) {
            this.mensagem = mensagem;
            this.local = local;
            this.dataInicio = dataInicio;
            this.dataFim = dataFim;
            this.horaInicio = horaInicio;
            this.horaFim = horaFim;
            this.tipoRestricao = tipoRestricao;
        }

        // Getters
        public String getMensagem() { return mensagem; }
        public String getLocal() { return local; }
        public String getDataInicio() { return dataInicio; }
        public String getDataFim() { return dataFim; }
        public String getHoraInicio() { return horaInicio; }
        public String getHoraFim() { return horaFim; }
        public String getTipoRestricao() { return tipoRestricao; }
    }
}