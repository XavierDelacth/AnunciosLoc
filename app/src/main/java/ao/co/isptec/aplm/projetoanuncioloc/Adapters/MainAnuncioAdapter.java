package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.R;
import ao.co.isptec.aplm.projetoanuncioloc.VisualizarAnuncioDialog;

public class MainAnuncioAdapter extends RecyclerView.Adapter<MainAnuncioAdapter.ViewHolder> {

    private Context context;
    private List<Anuncio> lista;
    private boolean isMeusAnuncios;
    private OnActionClickListener onActionClickListener;

    public interface OnActionClickListener {
        void onEditClick(Anuncio anuncio, int position);
        void onDeleteClick(Anuncio anuncio, int position);

        void onSaveClick(Anuncio anuncio, int position);
    }

    public MainAnuncioAdapter(Context context, List<Anuncio> lista) {
        this.context = context;
        this.lista = lista;
        this.isMeusAnuncios = false;
        this.onActionClickListener = null;
    }

    public MainAnuncioAdapter(Context context, List<Anuncio> lista, boolean isMeusAnuncios, OnActionClickListener listener) {
        this.context = context;
        this.lista = lista;
        this.isMeusAnuncios = isMeusAnuncios;
        this.onActionClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anuncio_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Anuncio a = lista.get(position);
        holder.tvTitulo.setText(a.titulo);
        holder.tvDescricao.setText(a.descricao);

        // Se for a lista de meus anúncios, mostra os botões de ação
        if (isMeusAnuncios) {
            holder.layoutAcoes.setVisibility(View.VISIBLE);

            holder.btnEditar.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    onActionClickListener.onEditClick(lista.get(pos), pos);
                }
            });

            holder.btnExcluir.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    onActionClickListener.onDeleteClick(lista.get(pos), pos);
                }
            });
        } else {
            holder.layoutAcoes.setVisibility(View.GONE);
        }

        // CLIQUE NO ITEM INTEIRO → abre tela completa
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Anuncio anuncio = lista.get(pos);
                VisualizarAnuncioDialog dialog = VisualizarAnuncioDialog.newInstance(
                        anuncio,
                        pos,
                        (positionCallback, saved) -> {
                            // Callback para atualizações (se necessário)
                        }
                );
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "VisualizarAnuncio");
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // VIEWHOLDER
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescricao;
        ImageView btnEditar, btnExcluir;
        View layoutAcoes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvDescricao = itemView.findViewById(R.id.tv_descricao);
            btnEditar = itemView.findViewById(R.id.btnEditarAnuncio);
            btnExcluir = itemView.findViewById(R.id.btnExcluirAnuncio);
            layoutAcoes = itemView.findViewById(R.id.layoutAcoes);
        }
    }
}