package com.example.autos.domain

import android.os.Parcelable
import com.example.autos.data.local.CompoundPrice
import com.example.autos.data.local.DbAuto
import com.example.autos.data.local.DbGasto
import com.example.autos.data.local.DbItem
import kotlinx.parcelize.Parcelize

//       AUTOS
@Parcelize
data class DomainCoche(
    val id: Int = 0,
    val marca: String,
    val modelo: String,
    val matricula: String,
    val year: String,
    val initKms: Int,
    var actualKms: Int,
    val buyDate: String
): Parcelable

fun DomainCoche.asDatabaseModel(): DbAuto {
    return DbAuto(
        id = this.id,
        marca = this.marca,
        modelo = this.modelo,
        matricula = this.matricula,
        year = this.year,
        initKms = this.initKms,
        lastKms = this.actualKms,
        buyDate = this.buyDate
    )
}

//      REPOSTAJES
@Parcelize
data class DomainRefueling(
    val refuelId: Int,
    val cocheId: Int,
    val fecha: String,
    val kms: Int,
    val litros: Float,
    val eurosLitro: Float,
    val euros: Float,
    val lleno: Boolean,
    var recorrido: Int = 0
): Parcelable

//      ESTADISTICAS
data class AverageRefueling(
    val initFecha: String,
    val endFecha: String,
    val kms: Int,
    val consumo: Float
)

data class KmsByYear(
    val kms: Int,
    val year: String
)

data class PricesByYear(
    val year: String,
    val min: CompoundPrice,
    val max: CompoundPrice
)

//      GASTOS
//@Parcelize
data class DomainGasto(
    val gastoId: Int = 0,
    val fecha: String,
    val concepto: String,
    val autoId: Int,
    val kms: Int,
    var importe: Float,
    var items: List<DomainItem>
)

fun DomainGasto.asDatabaseModel(): DbGasto {
    return DbGasto(
                gastoId = this.gastoId,
                fecha = this.fecha,
                concepto = this.concepto,
                autoId = this.autoId,
                kms = this.kms,
                importe = this.importe
            )
}

//@Parcelize
data class DomainItem(
    val itemId: Int = 0,
    var gastoId: Int,
    val descripcion: String,
    val detalle: String?,
    var precio: Float,
    val cantidad: Int
)

fun DomainItem.asDatabaseModel(): DbItem {
    return DbItem(
        itemId = this.itemId,
        gastoId = this.gastoId,
        descripcion = this.descripcion,
        detalle = this.detalle,
        precio = this.precio,
        cantidad = this.cantidad
    )
}