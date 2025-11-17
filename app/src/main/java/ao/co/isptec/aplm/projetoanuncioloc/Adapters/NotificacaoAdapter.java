package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Notificacao;
import ao.co.isptec.aplm.projetoanuncioloc.R;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.ViewHolder> {

    private Context context;
    private List<Notificacao> lista;

    public NotificacaoAdapter(Context context, List<Notificacao> lista) {
        this.context = context;
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notificacao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacao n = lista.get(position);
        holder.tvTitulo.setText(n.getTitulo());
        holder.tvDescricao.setText(n.getDescricao());
        holder.tvData.setText(n.getDataEnvio().toString().replace("T", " ").substring(0, 19));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescricao, tvData;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloNotif);
            tvDescricao = itemView.findViewById(R.id.tvDescricaoNotif);
            tvData = itemView.findViewById(R.id.tvDataNotif);
        }
    }
}