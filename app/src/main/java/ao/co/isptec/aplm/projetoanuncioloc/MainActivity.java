package ao.co.isptec.aplm.projetoanuncioloc;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
public class MainActivity extends AppCompatActivity {
    private CardView cardLocais, cardAnuncios;
    private TextView tabCriados, tabGuardados;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // seu layout principal com cards, tabs e toolbar
        // Inicializar cards
        cardLocais = findViewById(R.id.cardLocais);
        cardAnuncios = findViewById(R.id.cardAnuncios);
        // Inicializar tabs
        tabCriados = findViewById(R.id.tabCriados);
        tabGuardados = findViewById(R.id.tabGuardados);
        // Clique no card Locais -> abre tela Adicionar Chave
        cardLocais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdicionarChaveActivity.class);
                startActivity(intent);
            }
        });
        // Clique no card Anúncios -> abre tela Anúncios
        cardAnuncios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AnunciosActivity.class);
                startActivity(intent);
            }
        });
        // Tabs
        tabCriados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabCriados.setBackgroundColor(getResources().getColor(R.color.verde_principal));
                tabCriados.setTextColor(getResources().getColor(R.color.white));
                tabGuardados.setBackgroundColor(getResources().getColor(R.color.white));
                tabGuardados.setTextColor(getResources().getColor(R.color.verde_principal));
                // Aqui você pode atualizar a lista do RecyclerView
            }
        });
        tabGuardados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabGuardados.setBackgroundColor(getResources().getColor(R.color.verde_principal));
                tabGuardados.setTextColor(getResources().getColor(R.color.white));
                tabCriados.setBackgroundColor(getResources().getColor(R.color.white));
                tabCriados.setTextColor(getResources().getColor(R.color.verde_principal));
                // Aqui você pode atualizar a lista do RecyclerView
            }
        });
    }
}