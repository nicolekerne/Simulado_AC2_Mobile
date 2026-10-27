package com.example.simuladoac2

import com.google.firebase.firestore.DocumentId

data class Obra(
    @DocumentId
    var id: String = "",
    var nome: String = "",
    var tipo: String = "",
    var genero: String = "",
    var ano: Int = 0,
    var nota: Double = 0.0,
    var jaAssistiu: Boolean = false
) {
    // Construtor vazio necessário para o Firestore
    constructor() : this("", "", "", "", 0, 0.0, false)
}