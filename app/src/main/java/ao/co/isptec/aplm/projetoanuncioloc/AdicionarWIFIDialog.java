package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

import ao.co.isptec.aplm.projetoanuncioloc.Interface.OnLocalAddedListener;

public class AdicionarWIFIDialog extends DialogFragment {

    private static final String TAG = "AdicionarWIFIDialog";

    private EditText etNomeLocalWiFi, etSSID;
    private Button btnCancelarWiFi, btnAdicionarWiFi, btnGps;
    private ImageView btnFecharWiFi;

    // Interface comum para ambos diálogos (GPS e WiFi)
    private OnLocalAddedListener listener;

    // Factory method para newInstance
    public static AdicionarWIFIDialog newInstance(OnLocalAddedListener listener) {
        AdicionarWIFIDialog dialog = new AdicionarWIFIDialog();
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnLocalAddedListener) {
            listener = (OnLocalAddedListener) context;
        }
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
        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        Log.d(TAG, "Inicializando views do WiFi Dialog");

        etNomeLocalWiFi = view.findViewById(R.id.etNomeLocalWiFi);
        etSSID = view.findViewById(R.id.etSSID);
        btnCancelarWiFi = view.findViewById(R.id.btnCancelarWiFi);
        btnAdicionarWiFi = view.findViewById(R.id.btnAdicionarWiFi);
        btnGps = view.findViewById(R.id.btnGpsToggle);
        btnFecharWiFi = view.findViewById(R.id.btnFecharWiFi);

        // Validação de views
        if (btnGps == null) {
            Log.e(TAG, "ERRO: btnGps não encontrado no layout! Verifique o ID no XML.");
        } else {
            Log.d(TAG, "btnGps encontrado com sucesso");
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "Configurando listeners");

        // Botão fechar
        if (btnFecharWiFi != null) {
            btnFecharWiFi.setOnClickListener(v -> {
                Log.d(TAG, "Botão fechar clicado");
                dismiss();
            });
        }

        // Botão cancelar
        if (btnCancelarWiFi != null) {
            btnCancelarWiFi.setOnClickListener(v -> {
                Log.d(TAG, "Botão cancelar clicado");
                dismiss();
            });
        }

        // CRÍTICO: Botão GPS - Volta para o GPS Dialog
        if (btnGps != null) {
            btnGps.setOnClickListener(v -> {
                Log.d(TAG, "Botão GPS clicado - Alternando para GPS Dialog");

                // Fecha este diálogo WiFi primeiro
                dismiss();

                // Usa postDelayed para garantir que o dismiss complete antes de abrir o novo
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            // Abre o GPS Dialog com o mesmo listener
                            AdicionarGPSDialog gpsDialog = AdicionarGPSDialog.newInstance(listener);
                            gpsDialog.show(getParentFragmentManager(), "AdicionarGPSDialog");
                            Log.d(TAG, "GPS Dialog aberto com sucesso");
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao abrir GPS Dialog: " + e.getMessage());
                            Toast.makeText(getContext(), "Erro ao alternar para GPS", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            Log.e(TAG, "btnGps é null - não pode configurar listener!");
        }

        // Botão adicionar WiFi
        if (btnAdicionarWiFi != null) {
            btnAdicionarWiFi.setOnClickListener(v -> {
                Log.d(TAG, "Botão adicionar WiFi clicado");

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

                // Callback com método WiFi
                if (listener != null) {
                    listener.onLocalAddedWiFi(nome, Arrays.asList(ssid));
                    Log.d(TAG, "Local WiFi '" + nome + "' adicionado via callback");
                    Toast.makeText(requireContext(),
                            "Local WiFi '" + nome + "' (SSID: " + ssid + ") adicionado!",
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Log.e(TAG, "Listener é null - não pode adicionar local");
                    Toast.makeText(requireContext(), "Erro: listener não configurado", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Log.d(TAG, "Listeners configurados com sucesso");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "WiFi Dialog destruído");
    }
}