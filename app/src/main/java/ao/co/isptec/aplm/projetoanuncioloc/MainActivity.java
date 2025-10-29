package ao.co.isptec.aplm.projetoanuncioloc;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("Bem-vindo ao AnunciosLoc!\n\nVocê está logado.");
        tv.setTextSize(20);
        tv.setPadding(50, 50, 50, 50);
        setContentView(tv);
    }
}