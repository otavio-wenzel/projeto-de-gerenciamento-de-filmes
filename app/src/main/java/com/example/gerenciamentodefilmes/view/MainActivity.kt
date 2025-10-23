package com.example.gerenciamentodefilmes.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.gerenciamentodefilmes.model.database.AppDatabase
import com.example.gerenciamentodefilmes.model.entity.Diretor
import com.example.gerenciamentodefilmes.model.entity.Filme
import com.example.gerenciamentodefilmes.viewmodel.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import com.example.gerenciamentodefilmes.viewmodel.FilmeViewModel
import com.example.gerenciamentodefilmes.viewmodel.DiretorViewModel
import com.example.gerenciamentodefilmes.viewmodel.DiretorViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var filmeViewModel: FilmeViewModel
    private lateinit var diretorViewModel: DiretorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)
        val filmeDao = db.filmeDao()
        val diretorDao = db.diretorDao()

        filmeViewModel = ViewModelProvider(
            this,
            FilmeViewModelFactory(filmeDao)
        )[FilmeViewModel::class.java]

        diretorViewModel = ViewModelProvider(
            this,
            DiretorViewModelFactory(diretorDao)
        )[DiretorViewModel::class.java]

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

    var mostrarCaixaDialogo by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

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
            onClick = { context.startActivity(Intent(context, DiretorActivity::class.java)) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Adicionar Diretor") }

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
                label = { Text("Título do filme") }
            )

            Spacer(modifier = Modifier.height(15.dp))

            Box {
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(diretorSelecionado?.nome ?: "Selecione um diretor") }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listaDiretores.forEach {
                        DropdownMenuItem(
                            text = { Text(it.nome) },
                            onClick = {
                                diretorSelecionado = it
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
            ) { Text(textoBotao) }

            Spacer(modifier = Modifier.height(15.dp))
        }

        LazyColumn {
            items(listaFilmes) { filme ->
                val diretor = listaDiretores.find { it.id == filme.diretorId }

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
                    }) { Text("Excluir") }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = {
                        modoEditar = true
                        filmeExcluir = filme
                        titulo = filme.titulo
                        diretorSelecionado = listaDiretores.find { it.id == filme.diretorId }
                        textoBotao = "Atualizar"
                    }) { Text("Atualizar") }
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
        title = { Text("Confirmar exclusão") },
        text = { Text("Tem certeza que deseja excluir este filme?") },
        confirmButton = { Button(onClick = onConfirm) { Text("Sim, excluir") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Não, cancelar") } }
    )
}
