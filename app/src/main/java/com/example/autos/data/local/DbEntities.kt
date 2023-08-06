package com.example.autos.data.local

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.autos.domain.DomainCoche
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

fun DbAuto.asDomainModel(): DomainCoche{
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

fun LiveData<DbAuto>.asDomainModel(): LiveData<DomainCoche> {
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

fun List<DbAuto>.asDomainModel(): List<DomainCoche> {
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
    val lleno: Boolean
)

fun DbRefueling.asDomainModel(): DomainRefueling {
    return DomainRefueling(
        refuelId = this.refuelId,
        cocheId = this.cocheId,
        fecha = this.fecha,
        kms = this.kms,
        litros = this.litros,
        eurosLitro = this.eurosLitro,
        euros = this.euros,
        lleno = this.lleno
    )
}

fun LiveData<DbRefueling?>.asLiveDataDomainModel(): LiveData<DomainRefueling?> {
    return map {
        if (it != null) {
            DomainRefueling(
                refuelId = it.refuelId,
                cocheId = it.cocheId,
                fecha = it.fecha,
                kms = it.kms,
                litros = it.litros,
                eurosLitro = it.eurosLitro,
                euros = it.euros,
                lleno = it.lleno
            )
        }
        else null
    }
}

fun LiveData<List<DbRefueling>>.asListDomainModel(): LiveData<List<DomainRefueling>> {
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
                lleno = it.lleno
            )
        }
    }
}

fun List<DbRefueling>.asDomainModelList(): List<DomainRefueling> {
    return map {
        DomainRefueling(
            refuelId = it.refuelId,
            cocheId = it.cocheId,
            fecha = it.fecha,
            kms = it.kms,
            litros = it.litros,
            eurosLitro = it.eurosLitro,
            euros = it.euros,
            lleno = it.lleno
        )
    }
}

data class CompoundPrice(
    val price: Float = 0f,
    val fecha: String? = "//"
)