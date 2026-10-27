package com.example.simuladoac2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ObraAdapter
    private var listaObras = mutableListOf<Obra>()
    private var obraEmEdicao: Obra? = null

    // Componentes de Cadastro
    private lateinit var etNome: EditText
    private lateinit var spTipo: Spinner
    private lateinit var spGenero: Spinner
    private lateinit var etAno: EditText
    private lateinit var etNota: EditText
    private lateinit var cbJaAssistiu: CheckBox
    private lateinit var btnSalvar: Button
    private lateinit var btnCancelar: Button

    // Componentes de Listagem e Filtros
    private lateinit var rvObras: RecyclerView
    private lateinit var etBusca: EditText
    private lateinit var spFiltroTipo: Spinner
    private lateinit var cbFiltroAssistidos: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()

        inicializarComponentes()
        configurarSpinners()
        configurarRecyclerView()
        configurarEventosFiltros()
        carregarDadosFirestore()

        btnSalvar.setOnClickListener { guardarObra() }
        btnCancelar.setOnClickListener { cancelarEdicao() }
    }

    private fun inicializarComponentes() {
        etNome = findViewById(R.id.etNome)
        spTipo = findViewById(R.id.spTipo)
        spGenero = findViewById(R.id.spGenero)
        etAno = findViewById(R.id.etAno)
        etNota = findViewById(R.id.etNota)
        cbJaAssistiu = findViewById(R.id.cbJaAssistiu)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnCancelar = findViewById(R.id.btnCancelar)

        rvObras = findViewById(R.id.rvObras)
        etBusca = findViewById(R.id.etBusca)
        spFiltroTipo = findViewById(R.id.spFiltroTipo)
        cbFiltroAssistidos = findViewById(R.id.cbFiltroAssistidos)
    }

    private fun configurarSpinners() {
        val tipos = arrayOf("Selecione", "Filme", "Série", "Documentário", "Animação")
        spTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)

        val generos = arrayOf("Selecione", "Ação", "Comédia", "Drama", "Terror", "Ficção Científica")
        spGenero.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, generos)

        val tiposFiltro = arrayOf("Todos", "Filme", "Série", "Documentário", "Animação")
        spFiltroTipo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposFiltro)
    }

    private fun configurarRecyclerView() {
        adapter = ObraAdapter(listaObras, { obraClicada -> prepararEdicao(obraClicada) }, { obraLongClick -> confirmarExclusao(obraLongClick) })
        rvObras.layoutManager = LinearLayoutManager(this)
        rvObras.adapter = adapter
    }

    private fun configurarEventosFiltros() {
        etBusca.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { aplicarFiltros() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        spFiltroTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { aplicarFiltros() }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        cbFiltroAssistidos.setOnCheckedChangeListener { _, _ -> aplicarFiltros() }
    }

    private fun aplicarFiltros() {
        val termo = etBusca.text.toString().trim().lowercase()
        val tipoSelecionado = spFiltroTipo.selectedItem.toString()
        val apenasAssistidos = cbFiltroAssistidos.isChecked

        val listaFiltrada = listaObras.filter { obra ->
            val bateNome = termo.isEmpty() || obra.nome.lowercase().contains(termo)
            val bateTipo = tipoSelecionado == "Todos" || obra.tipo == tipoSelecionado
            val bateAssistido = if (apenasAssistidos) obra.jaAssistiu else true

            bateNome && bateTipo && bateAssistido
        }
        adapter.atualizarLista(listaFiltrada)
    }

    private fun carregarDadosFirestore() {
        db.collection("obras").orderBy("nome", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, erro ->
                if (erro != null) return@addSnapshotListener
                listaObras.clear()
                for (doc in snapshots!!) {
                    val obra = doc.toObject(Obra::class.java)
                    obra.id = doc.id
                    listaObras.add(obra)
                }
                aplicarFiltros() // Aplica os filtros atuais aos dados acabados de carregar
            }
    }

    private fun guardarObra() {
        val nome = etNome.text.toString().trim()
        val tipo = spTipo.selectedItem.toString()
        val genero = spGenero.selectedItem.toString()
        val anoStr = etAno.text.toString().trim()
        val notaStr = etNota.text.toString().trim()

        if (nome.isEmpty() || anoStr.isEmpty() || notaStr.isEmpty()) {
            Toast.makeText(this, "Preencha o nome, ano e nota!", Toast.LENGTH_SHORT).show()
            return
        }
        if (tipo == "Selecione" || genero == "Selecione") {
            Toast.makeText(this, "Selecione um tipo e um género válidos!", Toast.LENGTH_SHORT).show()
            return
        }

        val ano = anoStr.toIntOrNull() ?: 0
        val nota = notaStr.toDoubleOrNull() ?: 0.0

        val obra = Obra(id = obraEmEdicao?.id ?: "", nome = nome, tipo = tipo, genero = genero, ano = ano, nota = nota, jaAssistiu = cbJaAssistiu.isChecked)

        if (obraEmEdicao == null) {
            db.collection("obras").add(obra).addOnSuccessListener {
                Toast.makeText(this, "Guardado com sucesso!", Toast.LENGTH_SHORT).show()
                limparCampos()
            }
        } else {
            db.collection("obras").document(obra.id).set(obra).addOnSuccessListener {
                Toast.makeText(this, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                limparCampos()
            }
        }
    }

    private fun prepararEdicao(obra: Obra) {
        obraEmEdicao = obra
        etNome.setText(obra.nome)
        etAno.setText(obra.ano.toString())
        etNota.setText(obra.nota.toString())
        cbJaAssistiu.isChecked = obra.jaAssistiu

        for (i in 0 until spTipo.count) { if (spTipo.getItemAtPosition(i) == obra.tipo) spTipo.setSelection(i) }
        for (i in 0 until spGenero.count) { if (spGenero.getItemAtPosition(i) == obra.genero) spGenero.setSelection(i) }

        btnSalvar.text = "Atualizar"
        btnCancelar.visibility = View.VISIBLE
    }

    private fun cancelarEdicao() { limparCampos() }

    private fun limparCampos() {
        obraEmEdicao = null
        etNome.text.clear()
        etAno.text.clear()
        etNota.text.clear()
        cbJaAssistiu.isChecked = false
        spTipo.setSelection(0)
        spGenero.setSelection(0)
        btnSalvar.text = "Salvar"
        btnCancelar.visibility = View.GONE
    }

    private fun confirmarExclusao(obra: Obra) {
        AlertDialog.Builder(this)
            .setTitle("Excluir")
            .setMessage("Pretende eliminar a obra '${obra.nome}'?")
            .setPositiveButton("Sim") { _, _ ->
                db.collection("obras").document(obra.id).delete().addOnSuccessListener {
                    Toast.makeText(this, "Registo eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }
}