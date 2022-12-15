package server

import db.StarUnix
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Nave
import models.mensajes.Request
import models.mensajes.Response
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import javax.net.ssl.SSLSocket

private val json = Json
private lateinit var response: Response<String>

class GestorCliente(private val s: SSLSocket, private val su: StarUnix) : Runnable {

    // Preparamos los canales de entrada-salida
    private val sendResponse = DataOutputStream(s.outputStream)
    private val receiveRequest = DataInputStream(s.inputStream)

    override fun run() {
        // Leemos el dato del cliente y actuamos segun el tipo de request
        val request = json.decodeFromString<Request<Nave>>(receiveRequest.readUTF())

        when (request.type) {
            Request.Type.ADD -> {
                val registro = request.content as Nave
                println("Registro recibido, agregando")
                su.add(registro)
                response = Response(
                    "$registro agregado",
                    Response.Type.OK
                )
                sendResponse.writeUTF(json.encodeToString(response) + "\n")
            }

            Request.Type.GETLUKE -> {
                println("Luke recibido")
                val listado = su.getAll()
                response = Response(
                    listado.toString(),
                    Response.Type.OK
                )
                sendResponse.writeUTF(json.encodeToString(response) + "\n")
            }

            Request.Type.GETBB8 -> {
                println("BB8 recibido")
                val misiles = su.getInfoMisil()
                val totalRegistros = su.getInfoTotalNaves()
                response = Response(
                    "Misiles: $misiles | Total naves: $totalRegistros",
                    Response.Type.OK
                )
                sendResponse.writeUTF(json.encodeToString(response) + "\n")
            }

            else -> {
                println("tipo no reconocido")
                response = Response(
                    "Error",
                    Response.Type.ERROR
                )
                // Enviamos la respuesta
                sendResponse.writeUTF(json.encodeToString(response) + "\n")
            }
        }
        // Importante cerrar el socket para evitar problemas.
        s.close()
    }
}