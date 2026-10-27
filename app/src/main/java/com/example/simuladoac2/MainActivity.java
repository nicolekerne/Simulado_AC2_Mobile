package com.example.simuladoac2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ObraAdapter adapter;
    private List<Obra> listaObras = new ArrayList<>();
    private Obra obraEmEdicao = null;

    private EditText etNome, etAno, etNota, etBusca;
    private Spinner spTipo, spGenero, spFiltroTipo;
    private CheckBox cbJaAssistiu, cbFiltroAssistidos;
    private Button btnSalvar, btnCancelar;
    private RecyclerView rvObras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        inicializarComponentes();
        configurarSpinners();
        configurarRecyclerView();
        configurarEventosFiltros();
        carregarDadosFirestore();

        btnSalvar.setOnClickListener(v -> guardarObra());
        btnCancelar.setOnClickListener(v -> cancelarEdicao());
    }

    private void inicializarComponentes() {
        etNome = findViewById(R.id.etNome);
        spTipo = findViewById(R.id.spTipo);
        spGenero = findViewById(R.id.spGenero);
        etAno = findViewById(R.id.etAno);
        etNota = findViewById(R.id.etNota);
        cbJaAssistiu = findViewById(R.id.cbJaAssistiu);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnCancelar = findViewById(R.id.btnCancelar);

        rvObras = findViewById(R.id.rvObras);
        etBusca = findViewById(R.id.etBusca);
        spFiltroTipo = findViewById(R.id.spFiltroTipo);
        cbFiltroAssistidos = findViewById(R.id.cbFiltroAssistidos);
    }

    private void configurarSpinners() {
        String[] tipos = {"Selecione", "Filme", "Série", "Documentário", "Animação"};
        spTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tipos));

        String[] generos = {"Selecione", "Ação", "Comédia", "Drama", "Terror", "Ficção Científica"};
        spGenero.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, generos));

        String[] tiposFiltro = {"Todos", "Filme", "Série", "Documentário", "Animação"};
        spFiltroTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tiposFiltro));
    }

    private void configurarRecyclerView() {
        adapter = new ObraAdapter(listaObras, new ObraAdapter.OnObraClickListener() {
        @Override
        public void onEditarClick(Obra obra) {
            prepararEdicao(obra);
        }

        @Override
        public void onExcluirClick(Obra obra) {
            confirmarExclusao(obra);
        }
    });
        rvObras.setLayoutManager(new LinearLayoutManager(this));
        rvObras.setAdapter(adapter);
    }

    private void configurarEventosFiltros() {
        etBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { aplicarFiltros(); }
        });

        spFiltroTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { aplicarFiltros(); }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cbFiltroAssistidos.setOnCheckedChangeListener((buttonView, isChecked) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String termo = etBusca.getText().toString().trim().toLowerCase();
        String tipoSelecionado = spFiltroTipo.getSelectedItem().toString();
        boolean apenasAssistidos = cbFiltroAssistidos.isChecked();

        List<Obra> listaFiltrada = new ArrayList<>();
        for (Obra obra : listaObras) {
        boolean bateNome = termo.isEmpty() || obra.getNome().toLowerCase().contains(termo);
        boolean bateTipo = tipoSelecionado.equals("Todos") || obra.getTipo().equals(tipoSelecionado);
        boolean bateAssistido = !apenasAssistidos || obra.getJaAssistiu();

        if (bateNome && bateTipo && bateAssistido) {
            listaFiltrada.add(obra);
        }
    }
        adapter.atualizarLista(listaFiltrada);
    }

    private void carregarDadosFirestore() {
        db.collection("obras").orderBy("nome", Query.Direction.ASCENDING)
            .addSnapshotListener((snapshots, erro) -> {
        if (erro != null || snapshots == null) return;
        listaObras.clear();
        for (QueryDocumentSnapshot doc : snapshots) {
        Obra obra = doc.toObject(Obra.class);
        obra.setId(doc.getId());
        listaObras.add(obra);
    }
        aplicarFiltros();
    });
    }

    private void guardarObra() {
        String nome = etNome.getText().toString().trim();
        String tipo = spTipo.getSelectedItem().toString();
        String genero = spGenero.getSelectedItem().toString();
        String anoStr = etAno.getText().toString().trim();
        String notaStr = etNota.getText().toString().trim();

        if (nome.isEmpty() || anoStr.isEmpty() || notaStr.isEmpty()) {
            Toast.makeText(this, "Preencha o nome, ano e nota!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tipo.equals("Selecione") || genero.equals("Selecione")) {
            Toast.makeText(this, "Selecione um tipo e gênero válidos!", Toast.LENGTH_SHORT).show();
            return;
        }

        int ano = Integer.parseInt(anoStr);
        double nota = Double.parseDouble(notaStr);
        boolean jaAssistiu = cbJaAssistiu.isChecked();

        String id = (obraEmEdicao != null) ? obraEmEdicao.getId() : "";
        Obra obra = new Obra(id, nome, tipo, genero, ano, nota, jaAssistiu);

        if (obraEmEdicao == null) {
            db.collection("obras").add(obra).addOnSuccessListener(docRef -> {
                Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
                limparCampos();
            });
        } else {
            db.collection("obras").document(obra.getId()).set(obra).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                limparCampos();
            });
        }
    }

    private void prepararEdicao(Obra obra) {
        obraEmEdicao = obra;
        etNome.setText(obra.getNome());
        etAno.setText(String.valueOf(obra.getAno()));
        etNota.setText(String.valueOf(obra.getNota()));
        cbJaAssistiu.setChecked(obra.getJaAssistiu());

        for (int i = 0; i < spTipo.getCount(); i++) {
        if (spTipo.getItemAtPosition(i).equals(obra.getTipo())) spTipo.setSelection(i);
    }
        for (int i = 0; i < spGenero.getCount(); i++) {
        if (spGenero.getItemAtPosition(i).equals(obra.getGenero())) spGenero.setSelection(i);
    }

        btnSalvar.setText("Atualizar");
        btnCancelar.setVisibility(View.VISIBLE);
    }

    private void cancelarEdicao() {
        limparCampos();
    }

    private void limparCampos() {
        obraEmEdicao = null;
        etNome.setText("");
        etAno.setText("");
        etNota.setText("");
        cbJaAssistiu.setChecked(false);
        spTipo.setSelection(0);
        spGenero.setSelection(0);
        btnSalvar.setText("Salvar");
        btnCancelar.setVisibility(View.GONE);
    }

    private void confirmarExclusao(Obra obra) {
        new AlertDialog.Builder(this)
            .setTitle("Excluir")
            .setMessage("Deseja excluir a obra '" + obra.getNome() + "'?")
            .setPositiveButton("Sim", (dialog, which) -> {
        db.collection("obras").document(obra.getId()).delete().addOnSuccessListener(aVoid -> {
        Toast.makeText(this, "Registro excluído", Toast.LENGTH_SHORT).show();
    });
    })
        .setNegativeButton("Não", null)
        .show();
    }
}