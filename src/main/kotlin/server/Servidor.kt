package server

import db.StarUnix
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Executors
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLServerSocketFactory
import javax.net.ssl.SSLSocket

/*
R2D2 crea una seccion critica llamada StarUnix, y se pueden enviar tres tipos de mensajes:

    - Por parte de un piloto, envia un registro de una nave
    - Por parte de BB8, busca el numero de misiles totales y el total de naves
    - Por parte de Luke, el quiere el listado de registros/naves al completo.

Nave: Id, tipo de Nave(X-WIND, T-FIGHTER), salto de hiper espacio (boolean), misiles protonicos (entre 10..20), fecha creacion

-- Aplicacion de certificados y SSLServerSocket (usando .jks)

Lo unico que ha cambiado respecto al proyecto original son las clases:
    - Servidor
    - Cliente

-- GUIA (keytool) --
    1.Creamos el certificado en cuestion; abrimos la terminal:
        keytool -genkey -keyalg RSA -alias <alias> -keystore <keystore.jks> -validity <days> -keysize 2048
    2.Introducimos los datos que se soliciten.
    3.Ese archivo.jks es el que usara el servidor; lo guardamos en la raiz del proyecto (por ejemplo)
    4.Exportamos el certificado; nos desplazamos con cd hasta el directorio donde se encuentre el jks; abrimos terminal
        keytool -export -alias <alias> -storepass <clave> -file <archivo.cer> -keystore <archivo.jks>
    5.Ese certificado lo importamos a un nuevo jks, que sera el que usara el cliente; lo mismo que en el punto 4
        keytool -import -alias <alias> -file <archivo.cer> -keystore <archivo.jks>
    6.Introducimos los datos que se soliciten
    7.FIN

-- PD: Comando util para ver la informacion del keystore
        keytool -list -v -keystore <archivo.jks>

FUENTES USADAS:
https://community.pivotal.io/s/article/Generating-a-self-signed-SSL-certificate-using-the-Java-keytool-command?language=en_US

https://docs.oracle.com/cd/E19798-01/821-1841/gjrgy/

https://www.youtube.com/watch?v=MA4VO5n4RYI&ab_channel=Learner%27sCapital
*/

private const val PUERTO = 6969

private lateinit var serverFactory: SSLServerSocketFactory
private lateinit var s: SSLServerSocket

fun main() {
    var cliente: SSLSocket

    val starUnix = StarUnix()

    val pool = Executors.newFixedThreadPool(10)

    println("Iniciando servidor...")

    prepararConexion()

    while (true) {
        println("Servidor esperando...")

        cliente = s.accept() as SSLSocket
        println("CLiente -> ${cliente.inetAddress} conectado")

        val gc = GestorCliente(cliente, starUnix)
        pool.execute(gc)

        println("Cliente desconectado")
    }
    //s.close()
}

private fun prepararConexion() {
    // Fichero de donde se sacan los datos
    val workingDir = System.getProperty("user.dir")
    val fichero = Paths.get(workingDir + File.separator + "cert" + File.separator + "llaveropsp.jks").toString()
    System.setProperty("javax.net.ssl.keyStore", fichero)
    System.setProperty("javax.net.ssl.keyStorePassword", "123456")

    serverFactory = SSLServerSocketFactory.getDefault() as SSLServerSocketFactory
    s = serverFactory.createServerSocket(PUERTO) as SSLServerSocket
}
