package db

import models.Nave
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class StarUnix {
    private val registros = mutableListOf<Nave>()

    private val misiles: AtomicInteger = AtomicInteger(0)
    private val contadorId: AtomicInteger = AtomicInteger(0)

    private val lock = ReentrantLock()

    // Aplico el reentrantLock unicamente en la funcion critica, la de agregacion.
    fun add(item: Nave) {
        lock.withLock {
            contadorId.incrementAndGet()
            item.id = contadorId.toInt()

            println("Agregando $item")
            registros.add(item)

            misiles.addAndGet(item.misilesProtonicos)
            //println("Misiles registrados: $misiles")
        }
    }

    fun getAll(): List<Nave> {
        lock.withLock {
            println("Obteniendo registro completo")
            return registros
        }
    }

    fun getInfoMisil(): Int {
        lock.withLock {
            println("Obteniendo misiles")
            return misiles.toInt()
        }
    }

    fun getInfoTotalNaves(): Int {
        lock.withLock {
            println("Obteniendo total de naves")
            return registros.size
        }
    }
}