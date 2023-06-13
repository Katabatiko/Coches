package com.example.autos.domain

import android.os.Parcelable
import com.example.autos.data.local.DbAuto
import kotlinx.parcelize.Parcelize

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
    var recorrido: Int = 0,
    var expand: Boolean = false
): Parcelable