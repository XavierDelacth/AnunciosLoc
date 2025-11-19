package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Notificacao;
import ao.co.isptec.aplm.projetoanuncioloc.R;



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

import ao.co.isptec.aplm.projetoanuncioloc.Model.Notificacao;
import ao.co.isptec.aplm.projetoanuncioloc.R;
import ao.co.isptec.aplm.projetoanuncioloc.VisualizarAnuncioDialog;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.ViewHolder> {

    private Context context;
    private List<Notificacao> lista;
    private OnSaveClickListener onSaveClickListener;

    public interface OnSaveClickListener {
        void onSaveClick(Notificacao notificacao, int position);
    }

    public NotificacaoAdapter(Context context, List<Notificacao> lista, OnSaveClickListener listener) {
        this.context = context;
        this.lista = lista;
        this.onSaveClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anuncio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacao notificacao = lista.get(position);
        holder.tvTitulo.setText(notificacao.getTitulo());
        holder.tvDescricao.setText(notificacao.getDescricao());

        // BOOKMARK - sempre mostra como não salvo inicialmente
        holder.btnSalvar.setImageResource(R.drawable.ic_bookmark_nao_salvo);

        // CLIQUE NO ITEM → abre VisualizarAnuncioDialog
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Notificacao notif = lista.get(pos);

                // Converter Notificacao para Anuncio
                Anuncio anuncio = notif.toAnuncio();

                VisualizarAnuncioDialog dialog = VisualizarAnuncioDialog.newInstance(
                        anuncio,
                        pos,
                        (positionCallback, saved) -> {
                            // Callback quando o bookmark é alterado no dialog
                            if (saved) {
                                // Se salvou no dialog, atualiza o ícone na lista
                                holder.btnSalvar.setImageResource(R.drawable.ic_bookmark_salvo);
                                // Notifica o listener para guardar o anúncio
                                onSaveClickListener.onSaveClick(lista.get(positionCallback), positionCallback);
                            } else {
                                holder.btnSalvar.setImageResource(R.drawable.ic_bookmark_nao_salvo);
                            }
                        }
                );
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "VisualizarAnuncio");
            }
        });

        // CLIQUE DIRETO NO BOOKMARK → guarda o anúncio
        holder.btnSalvar.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                // Muda o ícone imediatamente para feedback visual
                holder.btnSalvar.setImageResource(R.drawable.ic_bookmark_salvo);

                // Notifica o listener para guardar o anúncio
                onSaveClickListener.onSaveClick(lista.get(pos), pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // Método para atualizar o estado de um item específico
    public void updateItemSavedState(int position, boolean saved) {
        if (position >= 0 && position < lista.size()) {
            notifyItemChanged(position);
        }
    }

    // VIEWHOLDER
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