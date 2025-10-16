package com.example.gerenciamentodefilmes.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gerenciamentodefilmes.viewmodel.FilmeViewModel
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import com.example.gerenciamentodefilmes.model.database.AppDatabase
import com.example.gerenciamentodefilmes.model.entity.Filme
import com.example.gerenciamentodefilmes.viewmodel.FilmeViewModelFactory
import android.content.Intent
import androidx.compose.material3.*
import com.example.gerenciamentodefilmes.model.entity.Diretor
import com.example.gerenciamentodefilmes.viewmodel.DiretorViewModel
import com.example.gerenciamentodefilmes.viewmodel.DiretorViewModelFactory

class MainActivity : ComponentActivity() {

    private val filmeViewModel: FilmeViewModel by viewModels {
        val dao = AppDatabase.getDatabase(applicationContext).getFilmeDao()
        FilmeViewModelFactory(dao)
    }

    private val diretorViewModel: DiretorViewModel by viewModels {
        val dao = AppDatabase.getDatabase(applicationContext).getDiretorDao()
        DiretorViewModelFactory(dao)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(filmeViewModel, diretorViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        diretorViewModel.buscarTodos()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(filmeViewModel: FilmeViewModel, diretorViewModel: DiretorViewModel) {
    var titulo by remember { mutableStateOf("") }
    var diretorSelecionado by remember { mutableStateOf<Diretor?>(null) }
    var filmeExcluir by remember { mutableStateOf<Filme?>(null) }
    var textoBotao by remember { mutableStateOf("Salvar") }
    var modoEditar by remember { mutableStateOf(false) }

    val listaFilmes by filmeViewModel.listaFilmes
    val listaDiretores by diretorViewModel.listaDiretores
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Variável de estado para exibir ou ocultar a caixa de diálogo
    var mostrarCaixaDialogo by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // Caixa de diálogo para confirmação de exclusão
    if (mostrarCaixaDialogo) {
        ExcluirFilme(onConfirm = {
            filmeExcluir?.let { filmeViewModel.excluirFilme(it) }
            mostrarCaixaDialogo = false
        }, onDismiss = { mostrarCaixaDialogo = false })
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Lista de Filmes",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {
                context.startActivity(Intent(context, DiretorActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Adicionar Diretor")
        }

        Spacer(modifier = Modifier.height(15.dp))

        if (listaDiretores.isEmpty()) {
            Text(
                text = "Cadastre ao menos um diretor para adicionar filmes.",
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            TextField(
                value = titulo,
                onValueChange = { titulo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Título do filme") }
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Dropdown Menu para selecionar o diretor
            Box {
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = diretorSelecionado?.nome ?: "Selecione um diretor")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listaDiretores.forEach {
                        DropdownMenuItem(text = { Text(text = it.nome) }, onClick = {   diretorSelecionado = it
                            expanded = false
                        }
                        )

                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (diretorSelecionado == null) {
                        Toast.makeText(context, "Selecione um diretor!", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    val retorno: String? = if (modoEditar) {
                        filmeExcluir?.let {
                            filmeViewModel.atualizarFilme(it.id, titulo, diretorSelecionado!!.id)
                                .also {
                                    modoEditar = false
                                    textoBotao = "Salvar"
                                }
                        }
                    } else {
                        filmeViewModel.salvarFilme(titulo, diretorSelecionado!!.id)
                    }

                    Toast.makeText(context, retorno, Toast.LENGTH_LONG).show()

                    titulo = ""
                    diretorSelecionado = null
                    focusManager.clearFocus()
                }
            ) {
                Text(text = textoBotao)
            }

            Spacer(modifier = Modifier.height(15.dp))
        }

        // Lista de filmes
        LazyColumn {
            items(listaFilmes) { filme ->
                // Encontre o diretor correspondente ao filme
                val diretor = listaDiretores.find { it.id == filme.diretorId }

                // Exibir título do filme e nome do diretor
                Text(
                    text = "${filme.titulo} (${diretor?.nome ?: "Diretor não encontrado"})",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row {
                    Button(onClick = {
                        filmeExcluir = filme
                        mostrarCaixaDialogo = true
                    }) {
                        Text(text = "Excluir")
                    }

                    Button(onClick = {
                        modoEditar = true
                        filmeExcluir = filme
                        titulo = filme.titulo
                        diretorSelecionado = listaDiretores.find { it.id == filme.diretorId }
                        textoBotao = "Atualizar"
                    }) {
                        Text(text = "Atualizar")
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))
            }
        }

    }
}

@Composable
fun ExcluirFilme(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Confirmar exclusão") },
        text = { Text(text = "Tem certeza que deseja excluir este filme?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = "Sim, excluir")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Não, cancelar")
            }
        }
    )
}

