package com.example.simuladoac2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ObraAdapter extends RecyclerView.Adapter<ObraAdapter.ObraViewHolder> {

    private List<Obra> listaObras;
    private OnObraClickListener clickListener;

    public interface OnObraClickListener {
        void onEditarClick(Obra obra);
        void onExcluirClick(Obra obra);
    }

    public ObraAdapter(List<Obra> listaObras, OnObraClickListener clickListener) {
        this.listaObras = listaObras;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ObraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_obra, parent, false);
        return new ObraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObraViewHolder holder, int position) {
        Obra obra = listaObras.get(position);
        holder.tvNome.setText(obra.getNome());
        holder.tvTipoGenero.setText("Tipo: " + obra.getTipo() + " | Gênero: " + obra.getGenero());
        holder.tvAnoNota.setText("Ano: " + obra.getAno() + " | Nota: " + obra.getNota());

        // Clique curto (Editar)
        holder.itemView.setOnClickListener(v -> clickListener.onEditarClick(obra));

        // Clique longo (Excluir)
        holder.itemView.setOnLongClickListener(v -> {
        clickListener.onExcluirClick(obra);
        return true;
    });
    }

    @Override
    public int getItemCount() {
        return listaObras.size();
    }

    public void atualizarLista(List<Obra> novaLista) {
        this.listaObras = novaLista;
        notifyDataSetChanged();
    }

    static class ObraViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvTipoGenero, tvAnoNota;

        public ObraViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNomeItem);
            tvTipoGenero = itemView.findViewById(R.id.tvTipoGeneroItem);
            tvAnoNota = itemView.findViewById(R.id.tvAnoNotaItem);
        }
    }
}