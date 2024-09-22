package com.example.datossinmvvm

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val db = crearDatabase(context)
    val dao = db.userDao()

    // Estado a HomeScreen
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var dataUser by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopBar(
                // Pasar los valores de firstName y lastName desde la topBar
                onAddClick = {
                    coroutineScope.launch {
                        val user = User(0, firstName, lastName)
                        AgregarUsuario(user = user, dao = dao)
                        firstName = "" // Limpiar después de agregar
                        lastName = ""  // Limpiar después de agregar
                    }
                },
                onListClick = {
                    coroutineScope.launch {
                        val data = getUsers(dao)
                        dataUser = data
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButtonComponent(onDeleteClick = {
                coroutineScope.launch {
                    deleteLastUser(dao)
                    val data = getUsers(dao)
                    dataUser = data
                }
            })
        },
        content = { padding ->
            // Pasar los datos y acciones necesarias a ScreenUser
            ScreenUser(
                padding = padding,
                firstName = firstName,
                lastName = lastName,
                onFirstNameChange = { firstName = it },
                onLastNameChange = { lastName = it },
                onAddUser = {
                    coroutineScope.launch {
                        val user = User(0, firstName, lastName)
                        AgregarUsuario(user = user, dao = dao)
                        firstName = ""
                        lastName = ""
                    }
                },
                onListUsers = {
                    coroutineScope.launch {
                        val data = getUsers(dao)
                        dataUser = data
                    }
                },
                dataUser = dataUser
            )
        }
    )
}


@Composable
fun ScreenUser(
    padding: PaddingValues,
    firstName: String,
    lastName: String,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onAddUser: () -> Unit,
    onListUsers: () -> Unit,
    dataUser: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Spacer(Modifier.height(50.dp))
        TextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            label = { Text("First Name: ") },
            singleLine = true
        )
        TextField(
            value = lastName,
            onValueChange = onLastNameChange,
            label = { Text("Last Name:") },
            singleLine = true
        )
        Button(
            onClick = onAddUser
        ) {
            Text("Agregar Usuario", fontSize = 16.sp)
        }
        Button(
            onClick = onListUsers
        ) {
            Text("Listar Usuarios", fontSize = 16.sp)
        }
        Text(
            text = dataUser, fontSize = 20.sp
        )
    }
}


@Composable
fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user_db"
    ).build()
}

suspend fun getUsers(dao: UserDao): String {
    var rpta: String = ""
    //LaunchedEffect(Unit) {
    val users = dao.getAll()
    users.forEach { user ->
        val fila = user.firstName + " - " + user.lastName + "\n"
        rpta += fila
    }
    //}
    return rpta
}

suspend fun AgregarUsuario(user: User, dao:UserDao): Unit {
    //LaunchedEffect(Unit) {
    try {
        dao.insert(user)
    }
    catch (e: Exception) {
        Log.e("User","Error: insert: ${e.message}")
    }
    //}
}

suspend fun deleteLastUser(userDao: UserDao) {
    // Obtiene el último usuario añadido
    val lastUser = userDao.ultimouser()

    // Si hay un usuario, lo elimina
    if (lastUser != null) {
        userDao.borraruser(lastUser.uid)
    }
}
@Composable
fun FloatingActionButtonComponent(onDeleteClick: () -> Unit) {
    FloatingActionButton(onClick = { onDeleteClick() }) {
        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onAddClick: () -> Unit, onListClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "Gestión de Nombres", color = Color.White) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Nombre", tint = Color.White)
            }
            IconButton(onClick = onListClick) {
                Icon(Icons.Default.List, contentDescription = "Listar Nombres", tint = Color.White)
            }
        }
    )
}
