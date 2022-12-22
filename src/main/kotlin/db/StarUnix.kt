package db

import models.Nave
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class StarUnix {
    private val registros = mutableListOf<Nave>()

    private val misiles: AtomicInteger = AtomicInteger(0)
    private val contadorId: AtomicInteger = AtomicInteger(0)

    private val lock = ReentrantLock()
    private val obtenerNaves: Condition = lock.newCondition()
    private val depositarNaves: Condition = lock.newCondition()

    private var escritor = false
    private var lector = AtomicInteger(0)

    fun add(item: Nave) {
        lock.withLock {
            while (lector.toInt() > 0) {
                depositarNaves.await()
            }
            escritor = true

            contadorId.incrementAndGet()
            item.id = contadorId.toInt()

            println("Agregando $item")
            registros.add(item)

            misiles.addAndGet(item.misilesProtonicos)

            escritor = false
            obtenerNaves.signalAll()
        }
    }

    fun getAll(): List<Nave> {
        lock.withLock {
            while (escritor) {
                obtenerNaves.await()
            }
            lector.incrementAndGet()
            println("Obteniendo registro completo")

            lector.decrementAndGet()
            depositarNaves.signalAll()
            return registros
        }
    }

    fun getInfoMisil(): Int {
        lock.withLock {
            while (escritor) {
                obtenerNaves.await()
            }
            lector.incrementAndGet()
            println("Obteniendo misiles")

            lector.decrementAndGet()
            depositarNaves.signalAll()
            return misiles.toInt()
        }
    }

    fun getInfoTotalNaves(): Int {
        lock.withLock {
            while (escritor) {
                obtenerNaves.await()
            }
            lector.incrementAndGet()
            println("Obteniendo total de naves")

            lector.decrementAndGet()
            depositarNaves.signalAll()
            return registros.size
        }
    }
}
