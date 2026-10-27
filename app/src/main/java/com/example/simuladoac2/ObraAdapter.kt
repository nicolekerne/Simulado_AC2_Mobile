package com.example.simuladoac2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ObraAdapter(
    private var listaObras: List<Obra>,
    private val aoClicarEditar: (Obra) -> Unit,
    private val aoClicarLongoExcluir: (Obra) -> Unit
) : RecyclerView.Adapter<ObraAdapter.ObraViewHolder>() {

    class ObraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNome: TextView = view.findViewById(R.id.tvNomeItem)
        val tvTipoGenero: TextView = view.findViewById(R.id.tvTipoGeneroItem)
        val tvAnoNota: TextView = view.findViewById(R.id.tvAnoNotaItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObraViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_obra, parent, false)
        return ObraViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObraViewHolder, position: Int) {
        val obra = listaObras[position]

        // Exibe os dados na lista conforme exigido
        holder.tvNome.text = obra.nome
        holder.tvTipoGenero.text = "Tipo: ${obra.tipo} | Gênero: ${obra.genero}"
        holder.tvAnoNota.text = "Ano: ${obra.ano} | Nota: ${obra.nota}"

        // Clique curto: carregar dados para edição
        holder.itemView.setOnClickListener {
            aoClicarEditar(obra)
        }

        // Clique longo: exibir opção de excluir
        holder.itemView.setOnLongClickListener {
            aoClicarLongoExcluir(obra)
            true
        }
    }

    override fun getItemCount(): Int = listaObras.size

    fun atualizarLista(novaLista: List<Obra>) {
        listaObras = novaLista
        notifyDataSetChanged()
    }
}