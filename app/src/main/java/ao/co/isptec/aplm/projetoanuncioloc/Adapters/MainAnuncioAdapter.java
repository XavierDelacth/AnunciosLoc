package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.AdicionarAnunciosActivity;
import ao.co.isptec.aplm.projetoanuncioloc.MainActivity;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.R;
import ao.co.isptec.aplm.projetoanuncioloc.VisualizarAnuncioMainDialog;


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

        // === CARREGA A IMAGEM DA BASE DE DADOS COM GLIDE ===
        String urlImagem = a.getImagemUrl();

        if (urlImagem != null && !urlImagem.isEmpty()) {
            Glide.with(context)
                    .load(urlImagem)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
                    .placeholder(R.drawable.espaco_image)
                    .error(R.drawable.espaco_image)
                    .into(holder.imgAnuncio);
        } else {
            // Se não tiver imagem, mostra o placeholder normal
            holder.imgAnuncio.setImageResource(R.drawable.espaco_image);
        }

        // Se for a lista de meus anúncios, mostra os botões de ação
        if (isMeusAnuncios) {
            holder.layoutAcoes.setVisibility(View.VISIBLE);

            holder.btnEditar.setOnClickListener(v -> {
                Intent intent = new Intent(context, AdicionarAnunciosActivity.class);
                intent.putExtra("MODO_EDICAO", true);
                intent.putExtra("ANUNCIO_PARA_EDITAR", a);  // Envia o anúncio completo
                intent.putExtra("POSICAO", position); // opcional, para atualizar lista depois
                ((AppCompatActivity) context).startActivityForResult(intent, MainActivity.REQUEST_CODE_EDITAR_ANUNCIO);
            });

            holder.btnExcluir.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && onActionClickListener != null) {
                    onActionClickListener.onDeleteClick(lista.get(pos), pos);
                }
            });
        } else {
            holder.layoutAcoes.setVisibility(View.GONE);
        }

        // CLIQUE NO ITEM INTEIRO → abre o VisualizarAnuncioMainDialog (MUDANÇA AQUI)
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Anuncio anuncio = lista.get(pos);

                // Usar VisualizarAnuncioMainDialog em vez do VisualizarAnuncioDialog
                VisualizarAnuncioMainDialog dialog = VisualizarAnuncioMainDialog.newInstance(
                        anuncio,
                        pos,
                        new VisualizarAnuncioMainDialog.BookmarkCallback() {
                            @Override
                            public void onBookmarkChanged(int position, boolean saved) {
                                // Callback vazio pois na MainActivity não usamos bookmark
                                // Mas mantemos a interface para compatibilidade
                            }
                        }
                );
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "VisualizarAnuncioMain");
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
        ImageView btnEditar, btnExcluir, imgAnuncio;
        View layoutAcoes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvDescricao = itemView.findViewById(R.id.tv_descricao);
            btnEditar = itemView.findViewById(R.id.btnEditarAnuncio);
            btnExcluir = itemView.findViewById(R.id.btnExcluirAnuncio);
            imgAnuncio = itemView.findViewById(R.id.img_anuncio);
            layoutAcoes = itemView.findViewById(R.id.layoutAcoes);
        }
    }
}