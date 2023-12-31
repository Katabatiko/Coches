package com.example.autos.data.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.autos.domain.DomainCoche
import com.example.autos.domain.DomainGasto
import com.example.autos.domain.DomainItem
import com.example.autos.domain.DomainRefueling

@Entity
data class DbAuto(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val marca: String,
    val modelo: String,
    val matricula: String,
    val year: String,
    val initKms: Int,
    var lastKms: Int,
    val buyDate: String
)

fun DbAuto.asDomainAuto(): DomainCoche{
    return DomainCoche(
        id = this.id,
        marca = this.marca,
        modelo = this.modelo,
        matricula = this.matricula,
        year = this.year,
        initKms = this.initKms,
        actualKms = this.lastKms,
        buyDate = this.buyDate
    )
}

fun LiveData<DbAuto>.asLiveDataDomainAuto(): LiveData<DomainCoche> {
    return map {
        DomainCoche(
            id = it.id,
            marca = it.marca,
            modelo = it.modelo,
            matricula = it.matricula,
            year = it.year,
            initKms = it.initKms,
            actualKms = it.lastKms,
            buyDate = it.buyDate
        )
    }
}

fun LiveData<List<DbAuto>>.asListDomainAutoModel(): LiveData<List<DomainCoche>>{
    return map { list ->
        list.map {
            DomainCoche(
                id = it.id,
                marca = it.marca,
                modelo = it.modelo,
                matricula = it.matricula,
                year = it.year,
                initKms = it.initKms,
                actualKms = it.lastKms,
                buyDate = it.buyDate
            )
        }
    }
}

@Entity
data class DbRefueling(
//    @ForeignKey(Coche::class, ["id"])
    @PrimaryKey(autoGenerate = true)
    var refuelId: Int = 0,
    var cocheId: Int,
    val fecha: String,
    val kms: Int,
    val litros: Float,
    val eurosLitro: Float,
    val euros: Float,
    val lleno: Boolean,
    @ColumnInfo(name = "recorrido", defaultValue = "0")
    var recorrido: Int = 0
)

fun DbRefueling.asDomainModel(): DomainRefueling {
    return  DomainRefueling(
        refuelId = this.refuelId,
        cocheId = this.cocheId,
        fecha = this.fecha,
        kms = this.kms,
        litros = this.litros,
        eurosLitro = this.eurosLitro,
        euros = this.euros,
        lleno = this.lleno,
        recorrido = this.recorrido
    )
}

fun LiveData<List<DbRefueling>>.asLiveDataListDomainModel(): LiveData<List<DomainRefueling>> {
    return map { list ->
        list.map {
            DomainRefueling(
                refuelId = it.refuelId,
                cocheId = it.cocheId,
                fecha = it.fecha,
                kms = it.kms,
                litros = it.litros,
                eurosLitro = it.eurosLitro,
                euros = it.euros,
                lleno = it.lleno,
                recorrido = it.recorrido
            )
        }
    }
}

fun List<DbRefueling>.asRefuelingDomainModel(): List<DomainRefueling> {
    return map {
        DomainRefueling(
            refuelId = it.refuelId,
            cocheId = it.cocheId,
            fecha = it.fecha,
            kms = it.kms,
            litros = it.litros,
            eurosLitro = it.eurosLitro,
            euros = it.euros,
            lleno = it.lleno,
            recorrido = it.recorrido
        )
    }
}

data class DateRange(
    val latest: String?,
    val oldest: String?
)

@Entity
data class DbGasto (
    @PrimaryKey(autoGenerate = true)
    var gastoId: Int = 0,
    val fecha: String,
    val concepto: String,
    var autoId: Int,
    val kms: Int,
    val importe: Float
)

fun DbGasto.asGastoDomainModel(): DomainGasto {
    return DomainGasto(
        gastoId = this.gastoId,
        fecha = this.fecha,
        concepto = this.concepto,
        autoId = this.autoId,
        kms = this.kms,
        importe = this.importe,
        items = listOf()
    )
}

fun List<DbGasto>.asGastoListDomainModel(): List<DomainGasto> {
    return map {
        DomainGasto(
            gastoId = it.gastoId,
            fecha = it.fecha,
            concepto = it.concepto,
            autoId = it.autoId,
            kms = it.kms,
            importe = it.importe,
            items = listOf()
        )
    }
}

@Entity
data class DbItem (
    @PrimaryKey(autoGenerate = true)
    var itemId: Int = 0,
    var gastoId: Int,
    val descripcion: String,
    val detalle: String?,
    val precio: Float,
    val cantidad: Int
)

fun DbItem.asDomainModel(): DomainItem {
    return DomainItem(
        itemId = this.itemId,
        gastoId = this.gastoId,
        descripcion = this.descripcion,
        detalle = this.detalle,
        precio = this.precio,
        cantidad = this.cantidad
    )
}

fun List<DbItem>.asItemsListDomainModel(): List<DomainItem> {
    return map { dbItem ->
        dbItem.asDomainModel()
    }
}

data class CompoundPrice(
    val price: Float = 0f,
    val fecha: String? = "//"
)