package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.Arrays;
import java.util.List;

public class EditarWiFiDialog extends DialogFragment {

    private EditText etNomeLocalWiFi, etSSID;
    private Button btnCancelarWiFi, btnSalvarWiFi;
    private ImageView btnFecharWiFi;

    // Dados para edição
    private String nomeAtual;
    private String ssidAtual;

    // Callback para retornar dados editados
    public interface OnLocalWiFiEditadoListener {
        void onLocalWiFiEditado(String nome, List<String> ssids);
    }

    private OnLocalWiFiEditadoListener listener;

    public static EditarWiFiDialog newInstance(String nome, String ssid, OnLocalWiFiEditadoListener listener) {
        EditarWiFiDialog dialog = new EditarWiFiDialog();
        dialog.nomeAtual = nome;
        dialog.ssidAtual = ssid;
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_wifi, container, false);
        initViews(view);
        preencherDados();
        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        etNomeLocalWiFi = view.findViewById(R.id.etNomeLocalWiFi);
        etSSID = view.findViewById(R.id.etSSID);
        btnCancelarWiFi = view.findViewById(R.id.btnCancelarWiFi);
        btnSalvarWiFi = view.findViewById(R.id.btnAdicionarWiFi);  // Reutiliza botão "Adicionar"
        btnFecharWiFi = view.findViewById(R.id.btnFecharWiFi);

        // Altera texto do botão para "Salvar"
        btnSalvarWiFi.setText("Salvar");

        // Esconde botão GPS (não precisa alternar em modo edição)
        Button btnGps = view.findViewById(R.id.btnGpsToggle);
        if (btnGps != null) {
            btnGps.setVisibility(View.GONE);
        }
    }

    private void preencherDados() {
        if (nomeAtual != null) {
            etNomeLocalWiFi.setText(nomeAtual);
        }
        if (ssidAtual != null) {
            etSSID.setText(ssidAtual);
        }
    }

    private void setupClickListeners() {
        btnFecharWiFi.setOnClickListener(v -> dismiss());
        btnCancelarWiFi.setOnClickListener(v -> dismiss());

        btnSalvarWiFi.setOnClickListener(v -> {
            String nome = etNomeLocalWiFi.getText().toString().trim();
            String ssid = etSSID.getText().toString().trim();

            // Validação
            if (nome.isEmpty() || ssid.isEmpty()) {
                if (nome.isEmpty()) {
                    etNomeLocalWiFi.setError("Nome obrigatório");
                    etNomeLocalWiFi.requestFocus();
                }
                if (ssid.isEmpty()) {
                    etSSID.setError("SSID obrigatório");
                    if (nome.isEmpty()) etSSID.requestFocus();
                }
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Callback com dados editados
            if (listener != null) {
                listener.onLocalWiFiEditado(nome, Arrays.asList(ssid));
            }
            dismiss();
        });
    }
}