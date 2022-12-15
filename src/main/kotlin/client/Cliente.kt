package client

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Nave
import models.mensajes.Request
import models.mensajes.Response
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.net.InetAddress
import java.nio.file.Paths
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

private val json = Json

private lateinit var request: Request<Nave>

fun main() {
    // Informacion del cliente y la conexion a realizar
    var direccion: InetAddress
    var clienteFactory: SSLSocketFactory
    var servidor: SSLSocket
    val puerto = 6969

    var salida = false

    // Si queremos que el cliente se desconecte de la app cuando quiera
    while (!salida) {
        println(
            """
            1. Piloto
            2. BB8
            3. Luke Skywalker
            4. Salir
        """.trimIndent()
        )

        // Leemos la opcion escogida y enviamos el request, indicando el tipo y el contenido
        val opcion = readln().toIntOrNull()
        when (opcion) {
            1 -> {
                println("Conectado como piloto")
                val nave = CreadorNavesRandom.init()
                println("\tNave preparada: $nave")

                request = Request(nave, Request.Type.ADD)
                println("\t--$request enviada, esperando respuesta...")
            }

            2 -> {
                println("Conectado como BB8")
                request = Request(null, Request.Type.GETBB8)
                println("\t--$request enviada, esperando respuesta...")
            }

            3 -> {
                println("Conectado como Luke Skywalker")
                request = Request(null, Request.Type.GETLUKE)
                println("\t--$request enviada, esperando respuesta...")
            }

            4 -> {
                println("Saliendo del programa...")
                salida = true
            }

            null -> {
                println("OPCION DESCONOCIDA...")
            }
        }

        // Evitamos la conexion con el servidor si la opcion no lo necesita
        try {
            if (opcion == null || opcion <= 0 || opcion >= 4) {
                println("---")
            } else {
                // Conectamos con el servidor, y segun la opcion seleccionada, le enviamos un aviso (un Int por ejemplo)
                prepararConexion()

                direccion = InetAddress.getLocalHost()
                clienteFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
                servidor = clienteFactory.createSocket(direccion, puerto) as SSLSocket

                // Canales de entrada-salida
                val sendRequest = DataOutputStream(servidor.outputStream)
                val receiveResponse = DataInputStream(servidor.inputStream)

                // Enviamos el request
                sendRequest.writeUTF(json.encodeToString(request) + "\n")

                // Esperamos la respuesta del servidor
                val response = json.decodeFromString<Response<String>>(receiveResponse.readUTF())

                println("Respuesta del servidor:\t${response.content}\n")
                //println("Respuesta del servidor:\t$response\n")

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun prepararConexion() {
    // Fichero de donde se sacan los datos
    val workingDir = System.getProperty("user.dir")
    val fichero = Paths.get(workingDir + File.separator + "certClient" + File.separator + "truststore.jks").toString()

    // Para depurar y ver el dialogo y handshake
    System.setProperty("javax.net.debug", "ssl, keymanager, handshake")

    System.setProperty("javax.net.ssl.trustStore", fichero)
    System.setProperty("javax.net.ssl.trustStorePassword", "654321")
}
