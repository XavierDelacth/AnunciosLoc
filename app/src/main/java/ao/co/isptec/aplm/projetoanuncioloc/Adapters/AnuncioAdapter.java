package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.R;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import ao.co.isptec.aplm.projetoanuncioloc.VisualizarAnuncioDialog;

public class AnuncioAdapter extends RecyclerView.Adapter<AnuncioAdapter.ViewHolder> {

    private Context context;
    private List<Anuncio> lista;
    private OnBookmarkClickListener onBookmarkClickListener;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(Anuncio anuncio, int position);
    }

    public AnuncioAdapter(Context context, List<Anuncio> lista, OnBookmarkClickListener listener) {
        this.context = context;
        this.lista = lista;
        this.onBookmarkClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anuncio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Anuncio a = lista.get(position);
        holder.tvTitulo.setText(a.titulo);
        holder.tvDescricao.setText(a.descricao);

        String urlImagem = a.getImagemUrl();

        if (urlImagem != null && !urlImagem.isEmpty()) {

            holder.imgAnuncio.setImageTintList(null);
            holder.imgAnuncio.setImageTintMode(null);
            holder.imgAnuncio.clearColorFilter();
            holder.imgAnuncio.setBackgroundColor(Color.TRANSPARENT);

            // Lógica de carregamento similar
                if (urlImagem.startsWith("http://") || urlImagem.startsWith("https://")) {
                    // URL normal
                    Glide.with(context)
                            .load(urlImagem)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
                            .placeholder(R.drawable.espaco_image)
                            .error(R.drawable.espaco_image)
                            .into(holder.imgAnuncio);
                } else if (urlImagem.startsWith("/uploads/") || urlImagem.startsWith("uploads/")) {
                    // Construir URL completa para caminhos relativos
                    String baseUrl = RetrofitClient.BASE_URL;
                    String fullUrl = baseUrl + (urlImagem.startsWith("/") ? urlImagem.substring(1) : urlImagem);

                    Glide.with(context)
                            .load(fullUrl)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
                            .placeholder(R.drawable.espaco_image)
                            .error(R.drawable.espaco_image)
                            .into(holder.imgAnuncio);
                } else {
                    // Para outros casos (URIs locais, etc.)
                    try {
                        Glide.with(context)
                                .load(urlImagem)
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
                                .placeholder(R.drawable.espaco_image)
                                .error(R.drawable.espaco_image)
                                .into(holder.imgAnuncio);
                    } catch (Exception e) {
                        holder.imgAnuncio.setImageResource(R.drawable.espaco_image);
                    }
                }
            } else {
                holder.imgAnuncio.setImageResource(R.drawable.espaco_image);
            }

        // BOOKMARK NA LISTA (sempre salvo = true na tela de guardados)
        holder.btnSalvar.setImageResource(R.drawable.ic_bookmark_salvo);

        // CLIQUE NO ITEM INTEIRO → abre tela completa
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Anuncio anuncio = lista.get(pos);
                VisualizarAnuncioDialog dialog = VisualizarAnuncioDialog.newInstance(
                        anuncio,
                        pos,
                        (positionCallback, saved) -> {
                            // Se desguardou no dialog, remove da lista
                            if (!saved) {
                                lista.remove(positionCallback);
                                AnuncioAdapter.this.notifyItemRemoved(positionCallback);
                            }
                        }
                );
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "VisualizarAnuncio");
            }
        });

        // CLIQUE NO BOOKMARK → remove dos guardados
        holder.btnSalvar.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                onBookmarkClickListener.onBookmarkClick(lista.get(pos), pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // Método para remover item da lista
    public void removeItem(int position) {
        lista.remove(position);
        notifyItemRemoved(position);
    }

    // VIEWHOLDER
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescricao;
        ImageView btnSalvar,imgAnuncio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvDescricao = itemView.findViewById(R.id.tv_descricao);
            btnSalvar = itemView.findViewById(R.id.btn_salvar);
            imgAnuncio = itemView.findViewById(R.id.img_anuncio);
        }
    }
}