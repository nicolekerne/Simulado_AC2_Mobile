package com.example.simuladoac2;

import com.google.firebase.firestore.DocumentId;

public class Obra {
    @DocumentId
    private String id;
    private String nome;
    private String tipo;
    private String genero;
    private int ano;
    private double nota;
    private boolean jaAssistiu;

    // Construtor vazio obrigatório para o Firestore
    public Obra() {}

    public Obra(String id, String nome, String tipo, String genero, int ano, double nota, boolean jaAssistiu) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.genero = genero;
        this.ano = ano;
        this.nota = nota;
        this.jaAssistiu = jaAssistiu;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public double getNota() { return nota; }
    public void setNota(double nota) { this.nota = nota; }

    public boolean getJaAssistiu() { return jaAssistiu; }
    public void setJaAssistiu(boolean jaAssistiu) { this.jaAssistiu = jaAssistiu; }
}