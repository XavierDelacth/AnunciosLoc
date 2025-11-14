package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
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
import ao.co.isptec.aplm.projetoanuncioloc.VisualizarAnuncioDialog;

public class AnuncioAdapter extends RecyclerView.Adapter<AnuncioAdapter.ViewHolder> {

    private Context context;
    private List<Anuncio> lista;

    public AnuncioAdapter(Context context, List<Anuncio> lista) {
        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anuncio, parent, false);
        return new ViewHolder(view);
    }

    // AnuncioAdapter.java — onBindViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Anuncio a = lista.get(position);
        holder.tvTitulo.setText(a.titulo);
        holder.tvDescricao.setText(a.descricao);

        // BOOKMARK NA LISTA (permanece igual)
        holder.btnSalvar.setImageResource(a.salvo ? R.drawable.ic_bookmark_salvo : R.drawable.ic_bookmark_nao_salvo);

        // CLIQUE NO ITEM INTEIRO → abre tela completa (ATUALIZADO)
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Anuncio anuncio = lista.get(pos);
                // NOVO: Passe position e listener (lambda que atualiza a lista)
                VisualizarAnuncioDialog dialog = VisualizarAnuncioDialog.newInstance(
                        anuncio,
                        pos,
                        (positionCallback, saved) -> {
                            // Atualiza o objeto original na lista
                            lista.get(positionCallback).salvo = saved;
                            // Notifica a mudança no adapter
                            AnuncioAdapter.this.notifyItemChanged(positionCallback);
                        }
                );
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "VisualizarAnuncio");
            }
        });

        // CLIQUE NO BOOKMARK (permanece igual — já atualiza a lista local)
        holder.btnSalvar.setOnClickListener(v -> {
            a.salvo = !a.salvo;
            notifyItemChanged(position);
            Toast.makeText(context, a.salvo ? "Guardado!" : "Removido!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // VIEWHOLDER CORRIGIDO
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescricao;
        ImageView btnSalvar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvDescricao = itemView.findViewById(R.id.tv_descricao);
             btnSalvar = itemView.findViewById(R.id.btn_salvar);
        }
    }
}