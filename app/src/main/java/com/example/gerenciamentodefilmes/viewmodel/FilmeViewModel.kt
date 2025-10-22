package com.example.gerenciamentodefilmes.viewmodel


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.gerenciamentodefilmes.model.entity.Filme
import androidx.lifecycle.viewModelScope
import com.example.gerenciamentodefilmes.model.dao.FilmeDao
import kotlinx.coroutines.launch

class FilmeViewModel(private  val filmeDao : FilmeDao): ViewModel() {

    var listaFilmes = mutableStateOf(listOf<Filme>())
        private set

    init {
        carregarFilmes()
    }

    private fun carregarFilmes(){
        viewModelScope.launch {
            listaFilmes.value = filmeDao.buscarTodos()
        }
    }

    fun salvarFilme(titulo: String, diretorId: Int) : String {
        if (titulo.isBlank()) {
            return "Preencha o título do campos!"
        }

        val filme = Filme(id = 0, titulo = titulo, diretorId = diretorId)

        viewModelScope.launch {
            filmeDao.inserir(filme)
            carregarFilmes()
        }

        return "Filme salvo com sucesso!"
    }

    fun excluirFilme(filme: Filme) {
        viewModelScope.launch {
            filmeDao.deletar(filme)
            carregarFilmes()
        }
    }

    fun atualizarFilme(id: Int, titulo: String, diretorId: Int) : String {
        if (titulo.isBlank()) {
            return ("Não pode deixar o título em branco")
        }

        val filme = listaFilmes.value.find { it.id == id } ?: return "Erro ao atualizar filme"
        val filmeAtualizado = filme.copy(titulo = titulo, diretorId = diretorId)

        viewModelScope.launch {
            filmeDao.atualizar(filmeAtualizado)
            carregarFilmes()
        }

        return "Filme atualizado!"
    }
}