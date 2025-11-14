package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;
import ao.co.isptec.aplm.projetoanuncioloc.R;

public class LocalAdapter extends RecyclerView.Adapter<LocalAdapter.LocalViewHolder> {

    private List<Local> listaLocais;
    private OnLocalClickListener listener;  // Para cliques (ex.: selecionar local)
    private Context context;  // NOVO: Para acessar cores

    public interface OnLocalClickListener {
        void onLocalClick(Local local, int position);
    }

    public LocalAdapter(List<Local> listaLocais) {
        this.listaLocais = listaLocais;
    }

    public void setOnLocalClickListener(OnLocalClickListener listener) {
        this.listener = listener;
    }

    public void updateLocais(List<Local> newLista) {
        this.listaLocais = newLista;
        notifyDataSetChanged();  // Refresh para BD futura
    }

    public void filtrarLocais(String query) {
        List<Local> filtrados = new ArrayList<>();
        for (Local local : listaLocais) {
            if (local.getNome().toLowerCase().contains(query.toLowerCase())) {
                filtrados.add(local);
            }
        }
        updateLocais(filtrados);  // Atualiza adapter com filtro
    }

    @NonNull
    @Override
    public LocalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_local, parent, false);
        this.context = parent.getContext();  // NOVO: Salva context para cores
        return new LocalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocalViewHolder holder, int position) {
        Local local = listaLocais.get(position);
        holder.tvNome.setText(local.getNome());
        holder.tvRaio.setText("Raio: " + local.getRaio() + "m | ");
        holder.tvLat.setText("Lat: " + local.getLatitude() + " | ");
        holder.tvLng.setText("Lng: " + local.getLongitude() + " | ");
        holder.tvTipo.setText(local.getTipo());

        // MUDANÇA PRINCIPAL: Cor dinâmica no tv_tipo baseada em tipo (GPS verde, WiFi laranja)
        if ("GPS".equals(local.getTipo())) {
            holder.tvTipo.setTextColor(ContextCompat.getColor(context, R.color.verde_principal));  // Verde para GPS
        } else if ("WiFi".equals(local.getTipo())) {
            holder.tvTipo.setTextColor(ContextCompat.getColor(context, R.color.laranja));  // Laranja para WiFi
        }

        // Clique no item (simulado: Toast, mas liga ao listener para selecionar/retornar)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLocalClick(local, position);
            } else {
                Toast.makeText(holder.itemView.getContext(), "Selecionado: " + local.getNome(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaLocais.size();
    }

    static class LocalViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvRaio, tvLat, tvLng, tvTipo;  // IDs do item_local.xml

        public LocalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tv_nome_local);
            tvRaio = itemView.findViewById(R.id.tv_raio);
            tvLat = itemView.findViewById(R.id.tv_lat);
            tvLng = itemView.findViewById(R.id.tv_lng);
            tvTipo = itemView.findViewById(R.id.tv_tipo);
        }
    }
}