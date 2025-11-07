package ao.co.isptec.aplm.projetoanuncioloc;


import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;

public class VisualizarAnuncioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_anuncio_e_guardar);

        Anuncio anuncio = (Anuncio) getIntent().getSerializableExtra("anuncio");

        TextView title = findViewById(R.id.announcementTitle);
        TextView content = findViewById(R.id.announcementContent);
        Button saveBtn = findViewById(R.id.saveButton);

        title.setText(anuncio.titulo);
        content.setText(anuncio.descricao);

        saveBtn.setOnClickListener(v -> {
            // SALVAR NA BD AQUI (Room)
            Toast.makeText(this, "Anúncio guardado com sucesso!", Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> finish());
    }
}
