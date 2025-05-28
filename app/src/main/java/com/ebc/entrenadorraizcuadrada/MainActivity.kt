package com.ebc.entrenadorraizcuadrada

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ebc.entrenadorraizcuadrada.ui.theme.EntrenadorRaizCuadradaTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    fun generarNuevoDesafio() {
        val base = Random.nextInt(2, 21)
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
        } else {
            mensajeFeedback = "Incorrecto. La raíz de $numeroActual es $raizCorrecta."
            resultado = "✘ $numeroActual → $respuestaUsuario (Correcto: $raizCorrecta)"
            val menor = raizCorrecta - 1
            val mayor = raizCorrecta + 1
            val menorCuadrado = menor * menor
            val mayorCuadrado = mayor * mayor
            pista = "Pista: la raíz está entre la de $menorCuadrado (= $menor) y la de $mayorCuadrado (= $mayor)"
            esCorrecto = false
        }
        resultadosRecientes = (listOf(resultado) + resultadosRecientes).take(10)
        respuestaVerificada = true
        mostrarAnimacion = true
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