package com.example.gerenciamentodefilmes.viewmodel


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.gerenciamentodefilmes.model.Validacao
import com.example.gerenciamentodefilmes.model.entity.Filme

class FilmeViewModel: ViewModel() {

    var listaFilmes = mutableStateOf(listOf<Filme>())
        private set

    fun salvarFilme(titulo: String, diretor: String) : String {
        if (Validacao.haCamposEmBranco(titulo, diretor)) {
            return "Preencha todos os campos!"
        }

        val filme = Filme(
            Validacao.getId(),
            titulo,
            diretor
        )

        listaFilmes.value += filme

        return "Filme salvo com sucesso!"
    }

    fun excluirFilme(id: Int) {
        listaFilmes.value = listaFilmes.value.filter { it.id != id }
    }

    fun atualizarFilme(id: Int, titulo: String, diretor: String) : String {
        if (Validacao.haCamposEmBranco(titulo, diretor)) {
            return ("Ao editar, preencha todos os dados do filme!")
        }

        val filmesAtualizados = listaFilmes.value.map { filme ->
            if (filme.id == id) {
                filme.copy(titulo = titulo, diretor = diretor)
            } else {
                filme
            }
        }

        listaFilmes.value = filmesAtualizados

        return "Filme atualizado!"
    }
}