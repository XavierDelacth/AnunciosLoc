package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.R;
import ao.co.isptec.aplm.projetoanuncioloc.VisualizarAnuncioActivity;

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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Anuncio a = lista.get(position);
        holder.tvTitulo.setText(a.titulo);
        holder.tvDescricao.setText(a.descricao);

        // BOOKMARK
        holder.btnSalvar.setImageResource(a.salvo ? R.drawable.ic_bookmark_salvo : R.drawable.ic_bookmark_nao_salvo);

        // VER MAIS AUTOMÁTICO
        holder.tvDescricao.post(() -> {
            if (holder.tvDescricao.getLineCount() > 2) {
                holder.btnVerMais.setVisibility(View.VISIBLE);
            } else {
                holder.btnVerMais.setVisibility(View.GONE);
            }
        });

        // CLIQUE NO ITEM → abre tela completa
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VisualizarAnuncioActivity.class);
            intent.putExtra("anuncio", a); // AGORA FUNCIONA!
            context.startActivity(intent);
        });

        // CLIQUE NO "VER MAIS"
        holder.btnVerMais.setOnClickListener(v -> holder.itemView.performClick());

        // CLIQUE NO BOOKMARK
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
        TextView tvTitulo, tvDescricao, btnVerMais;
        ImageView btnSalvar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvDescricao = itemView.findViewById(R.id.tv_descricao);
            btnVerMais = itemView.findViewById(R.id.btn_ver_mais); // AQUI ESTAVA FALTANDO
            btnSalvar = itemView.findViewById(R.id.btn_salvar);
        }
    }
}