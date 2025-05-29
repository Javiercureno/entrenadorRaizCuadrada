package com.ebc.entrenadorraizcuadrada

import android.Manifest
import androidx.compose.ui.platform.LocalContext
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import com.ebc.entrenadorraizcuadrada.db.AppDatabase
import com.ebc.entrenadorraizcuadrada.db.Resultado
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.ebc.entrenadorraizcuadrada.ui.theme.EntrenadorRaizCuadradaTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {

    // Solicitud de permiso para notificaciones
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                "logros",
                "Logros",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de logros"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crearCanalNotificaciones()
        solicitarPermisoNotificaciones() // <- Agregado aquí
        enableEdgeToEdge()
        setContent {
            EntrenadorRaizCuadradaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var numeroActual by remember { mutableStateOf(0) }
    var raizCorrecta by remember { mutableStateOf(0) }
    var respuestaUsuario by remember { mutableStateOf("") }
    var mensajeFeedback by remember { mutableStateOf("Presiona 'Jugar' para comenzar") }
    var juegoIniciado by remember { mutableStateOf(false) }
    var respuestaVerificada by remember { mutableStateOf(false) }
    var resultadosRecientes by remember { mutableStateOf(listOf<String>()) }
    var pista by remember { mutableStateOf("") }
    var mostrarAnimacion by remember { mutableStateOf(false) }
    var esCorrecto by remember { mutableStateOf(false) }
    var rachaCorrectas by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun generarNuevoDesafio() {
        val base = Random.nextInt(2, 12)
        numeroActual = base * base
        raizCorrecta = base
        respuestaUsuario = ""
        mensajeFeedback = "Ingresa la raíz cuadrada"
        juegoIniciado = true
        respuestaVerificada = false
        pista = ""
        mostrarAnimacion = false
    }

    fun verificarRespuesta() {
        val respuesta = respuestaUsuario.toIntOrNull()
        val resultado: String
        if (respuesta == raizCorrecta) {
            mensajeFeedback = "¡Correcto!"
            resultado = "✔ $numeroActual → $respuestaUsuario (Correcto)"
            pista = ""
            esCorrecto = true
            rachaCorrectas += 1
            if (rachaCorrectas == 3) {
                mostrarNotificacionLogro(context)
            }
        } else {
            mensajeFeedback = "Incorrecto. La raíz de $numeroActual es $raizCorrecta."
            resultado = "✘ $numeroActual → $respuestaUsuario (Correcto: $raizCorrecta)"
            val menor = raizCorrecta - 1
            val mayor = raizCorrecta + 1
            val menorCuadrado = menor * menor
            val mayorCuadrado = mayor * mayor
            pista = "Pista: la raíz está entre la de $menorCuadrado (= $menor) y la de $mayorCuadrado (= $mayor)"
            esCorrecto = false
            rachaCorrectas = 0
        }
        resultadosRecientes = (listOf(resultado) + resultadosRecientes).take(10)
        respuestaVerificada = true
        mostrarAnimacion = true

        // Guardar en la base de datos
        scope.launch {
            val db = AppDatabase.getDatabase(context.applicationContext)
            db.resultadoDao().insertar(
                Resultado(
                    numero = numeroActual,
                    respuesta = respuesta ?: -1,
                    correcto = (respuesta == raizCorrecta),
                    fechaHora = System.currentTimeMillis()
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (juegoIniciado) "Calcula la raíz cuadrada de: $numeroActual" else "Bienvenido",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            value = respuestaUsuario,
            onValueChange = { if (it.all { c -> c.isDigit() }) respuestaUsuario = it },
            label = { Text("Tu respuesta") },
            enabled = juegoIniciado && !respuestaVerificada,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Button(
            onClick = { verificarRespuesta() },
            enabled = juegoIniciado && !respuestaVerificada && respuestaUsuario.isNotEmpty()
        ) {
            Text("Comprobar")
        }
        AnimatedVisibility(
            visible = mostrarAnimacion,
            enter = scaleIn(animationSpec = tween(500)),
            exit = scaleOut(animationSpec = tween(500))
        ) {
            if (esCorrecto) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Correcto", tint = Color.White, modifier = Modifier.scale(1.5f))
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFF44336), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Incorrecto", tint = Color.White, modifier = Modifier.scale(1.5f))
                }
            }
        }
        Text(text = mensajeFeedback)
        if (pista.isNotEmpty()) {
            Text(text = pista, color = MaterialTheme.colorScheme.primary)
        }
        Button(
            onClick = { generarNuevoDesafio() },
            enabled = !juegoIniciado || respuestaVerificada
        ) {
            Text(if (!juegoIniciado) "Jugar" else "Siguiente Desafío")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (resultadosRecientes.isNotEmpty()) {
            Text("Últimos resultados:", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                resultadosRecientes.forEach { resultado ->
                    Text(resultado)
                }
            }
        }
    }
}

fun mostrarNotificacionLogro(context: Context) {
    // Verifica el permiso antes de notificar (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }
    val builder = NotificationCompat.Builder(context, "logros")
        .setSmallIcon(android.R.drawable.star_on)
        .setContentTitle("¡Logro desbloqueado!")
        .setContentText("¡Más de 3 respuestas correctas seguidas! 🎉")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(1, builder.build())
}