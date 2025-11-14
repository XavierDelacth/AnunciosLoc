package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.R;
import ao.co.isptec.aplm.projetoanuncioloc.VisualizarAnuncioMainDialog; // MUDANÇA: Import correto da classe

public class MainAnuncioAdapter extends RecyclerView.Adapter<MainAnuncioAdapter.ViewHolder> {

    private final List<Anuncio> lista;
    private final AppCompatActivity activity; // Para mostrar o diálogo

    public MainAnuncioAdapter(AppCompatActivity activity, List<Anuncio> lista) {
        this.activity = activity;
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_anuncio_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Anuncio anuncio = lista.get(position);
        holder.tvTitulo.setText(anuncio.titulo);
        holder.tvDescricao.setText(anuncio.descricao);

        // Carrega imagem (se houver)
        if (anuncio.imagem != null && !anuncio.imagem.isEmpty()) {
            holder.imgAnuncio.setImageURI(android.net.Uri.parse(anuncio.imagem));
        } else {
            holder.imgAnuncio.setImageResource(R.drawable.espaco_image); // Placeholder
        }

        // CLIQUE NO ITEM INTEIRO → Abre o DIALOGO CORRETO (MUDANÇA: Usa VisualizarAnuncioMainDialog)
        holder.itemView.setOnClickListener(v -> {
            VisualizarAnuncioMainDialog dialog = VisualizarAnuncioMainDialog.newInstance(
                    anuncio,
                    position,
                    (pos, saved) -> {
                        // Callback para atualizar bookmark no futuro (opcional)
                        if (saved != lista.get(pos).salvo) {
                            lista.get(pos).salvo = saved;
                            notifyItemChanged(pos);
                        }
                    }
            );
            dialog.show(activity.getSupportFragmentManager(), "VisualizarAnuncioMainDialog");
        });

        // NOVO: CLIQUE NA LIXEIRA → Remove o anúncio da lista
        holder.imgDelete.setOnClickListener(v -> {
            lista.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, lista.size());
            Toast.makeText(activity, "Anúncio removido!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // ViewHolder atualizado (com lixeira)
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescricao;
        ImageView imgAnuncio, imgDelete; // NOVO: Adiciona imgDelete

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvDescricao = itemView.findViewById(R.id.tv_descricao);
            imgAnuncio = itemView.findViewById(R.id.img_anuncio);
            imgDelete = itemView.findViewById(R.id.btn_excluir); // NOVO: ID da lixeira
        }
    }
}