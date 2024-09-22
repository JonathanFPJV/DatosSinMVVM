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
    Scaffold(
        topBar = {
            TopBar(
                onAddClick = {},
                onListClick = {}
            )
        },
        floatingActionButton = {
            FloatingActionButtonComponent(onDeleteClick = {
                // Acción para eliminar un elemento
            })
        },
        content = { padding ->
            // Llamar a ScreenUser y pasarle el padding
            ScreenUser(padding=padding,)
        }
    )
}

@Composable
fun ScreenUser(
    padding: PaddingValues,
) {
    val context = LocalContext.current
    var db: UserDatabase
    var id        by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName  by remember { mutableStateOf("") }
    var dataUser  = remember { mutableStateOf("") }

    db = crearDatabase(context)

    val dao = db.userDao()

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Spacer(Modifier.height(50.dp))
        TextField(
            value = id,
            onValueChange = { id = it },
            label = { Text("ID (solo lectura)") },
            readOnly = true,
            singleLine = true
        )
        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name: ") },
            singleLine = true
        )
        TextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name:") },
            singleLine = true
        )
        Button(
            onClick = {
                val user = User(0,firstName, lastName)
                coroutineScope.launch {
                    AgregarUsuario(user = user, dao = dao)
                }
                firstName = ""
                lastName = ""
            }
        ) {
            Text("Agregar Usuario", fontSize=16.sp)
        }
        Button(
            onClick = {
                val user = User(0,firstName, lastName)
                coroutineScope.launch {
                    val data = getUsers( dao = dao)
                    dataUser.value = data
                }
            }
        ) {
            Text("Listar Usuarios", fontSize=16.sp)
        }
        Button(
            onClick = {
                val user = User(0,firstName, lastName)
                coroutineScope.launch {
                    deleteLastUser(dao)
                    val data = getUsers(dao = dao)
                    dataUser.value = data
                }
            }
        ) {
            Text("Eliminar ultimo usuario", fontSize=16.sp)
        }
        Text(
            text = dataUser.value, fontSize = 20.sp
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
    // Usamos TopAppBar de Material 3
    TopAppBar(
        title = { Text(text = "Gestión de Nombres", color = Color.White) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            // Icono para agregar un nuevo nombre
            IconButton(onClick = { onAddClick() }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Nombre", tint = Color.White)
            }
            // Icono para listar los nombres
            IconButton(onClick = { onListClick() }) {
                Icon(Icons.Default.List, contentDescription = "Listar Nombres", tint = Color.White)
            }
        }
    )
}