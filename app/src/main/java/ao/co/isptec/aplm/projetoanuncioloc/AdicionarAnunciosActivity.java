package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class AdicionarAnunciosActivity extends AppCompatActivity {

    private ImageView btnBack, btnAddLocation, btnAdicionarChave;
    private TextView tvDataInicio, tvDataFim, tvHoraInicio, tvHoraFim, tvTipoRestricao;
    private EditText etMensagem, etPesquisarChaves;
    private LinearLayout llLocal, llDataInicio, llDataFim, llHoraInicio, llHoraFim, llTipoRestricao;
    private Spinner spinnerRestricao;
    private Button btnPublicar;
    private TextView tvLocalSelecionado;


    private String localSelecionado = null; // Guarda o nome do local
    private static final int REQUEST_LOCAL = 101;


    // Launcher moderno (melhor que startActivityForResult)
    private final ActivityResultLauncher<Intent> localLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String nome = result.getData().getStringExtra("nome_local");
                    String tipo = result.getData().getStringExtra("tipo");

                    if (nome != null && !nome.isEmpty()) {
                        localSelecionado = nome;
                        tvLocalSelecionado.setText(nome);
                        tvLocalSelecionado.setTextColor(getColor(R.color.verde_principal));
                        Toast.makeText(this, tipo + " adicionado: " + nome, Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> chaveLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String novaChave = result.getData().getStringExtra("nova_chave");
                    String valor = result.getData().getStringExtra("valor_adicionado");

                    if (novaChave != null) {
                        Toast.makeText(this, "Chave criada: " + novaChave, Toast.LENGTH_SHORT).show();
                        // Aqui você pode atualizar a lista de chaves dinamicamente
                    } else if (valor != null) {
                        Toast.makeText(this, "Valor adicionado: " + valor, Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_anuncios);

        initViews();
        setupSpinner();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        llLocal = findViewById(R.id.llLocal);
        btnAddLocation = findViewById(R.id.btnAddLocation);
        tvLocalSelecionado = findViewById(R.id.tvLocalSelecionado);  // ← FALTA ESTA LINHA!
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

        btnAddLocation.setOnClickListener(v -> abrirAdicionarLocal());
        llLocal.setOnClickListener(v -> abrirAdicionarLocal());

        btnAdicionarChave.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddicionarKeyActivity.class);
            chaveLauncher.launch(intent);
        });


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

    private void abrirAdicionarLocal() {
        Intent intent = new Intent(this, AdicionarGPS.class);
        localLauncher.launch(intent);
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

        // VALIDAÇÃO DO LOCAL
        if (localSelecionado == null || localSelecionado.equals("Selecionar um local")) {
            Toast.makeText(this, "Por favor, adicione um local de propagação", Toast.LENGTH_LONG).show();
            return;
        }

        if (mensagem.isEmpty()) {
            showError("Escreva uma mensagem para o anúncio");
            return;
        }
        if (dataInicio.equals("dd/mm/aaaa") || dataFim.equals("dd/mm/aaaa")) {
            showError("Selecione as datas de início e fim");
            return;
        }
        if (horaInicio.equals("hh:mm") || horaFim.equals("hh:mm")) {
            showError("Selecione os horários");
            return;
        }

        Anuncio novoAnuncio = new Anuncio(
                mensagem,
                localSelecionado, // agora usa o local real
                dataInicio, dataFim, horaInicio, horaFim, restricao
        );

        boolean sucesso = salvarAnuncio(novoAnuncio);
        if (sucesso) {
            Toast.makeText(this, "Anúncio publicado com sucesso!", Toast.LENGTH_SHORT).show();
            limparCampos();
            finish();
        } else {
            showError("Erro ao publicar. Tente novamente.");
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
        etMensagem.setText("");
        resetarDataHora(tvDataInicio, "dd/mm/aaaa");
        resetarDataHora(tvDataFim, "dd/mm/aaaa");
        resetarDataHora(tvHoraInicio, "hh:mm");
        resetarDataHora(tvHoraFim, "hh:mm");
        tvTipoRestricao.setText("Whitelist");
        spinnerRestricao.setSelection(0);
        etPesquisarChaves.setText("");

        // RESETAR LOCAL
        localSelecionado = null;
        tvLocalSelecionado.setText("Selecionar um local");
        tvLocalSelecionado.setTextColor(getColor(android.R.color.darker_gray));

    }

    private void resetarDataHora(TextView textView, String textoPadrao) {
        textView.setText(textoPadrao);
        textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
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