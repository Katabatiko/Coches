package com.example.autos.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.CompoundPrice
import com.example.autos.data.local.DbAuto
import com.example.autos.data.local.DbGasto
import com.example.autos.data.local.DbItem
import com.example.autos.data.local.DbRefueling

private const val TAG = "xxArepo"

class AutosRepository(private val database: AutosDatabase, application: Application) {

    private val preferences = application.getSharedPreferences("preferences",Context.MODE_PRIVATE)

    fun getActualAutoId(): Int{
        return preferences.getInt("auto_id", -1)
    }

    fun setActualAutoId(autoId: Int){
        with(preferences.edit()) {
            putInt("auto_id", autoId)
            apply()
        }
    }

    // AUTOS    ************************************
    suspend fun  insertAuto(auto: DbAuto): Long {
        return database.autosDao.insertAuto(auto)
    }

    fun getAllAutos() : LiveData<List<DbAuto>> {
        return database.autosDao.getAllAutos()
    }

    suspend fun getAutos(): List<DbAuto> {
        return database.autosDao.getAutos()
    }

    suspend fun getAuto(autoId: Int): DbAuto {
        return database.autosDao.getAuto(autoId)
    }

    fun countAutos(): LiveData<Int> {
        return database.autosDao.countAutos()
    }

    fun updateAuto(auto: DbAuto){
        database.autosDao.updateAuto(auto)
    }

    suspend fun updateAutoKms(cardId: Int, lastKms: Int){
        database.autosDao.updateAutoKms(cardId, lastKms)
    }

    suspend fun getAutoInitialKms(carId: Int): Int{
        return database.autosDao.getAutoInitialKms(carId)
    }

    fun getActualKms(autoId: Int): LiveData<Int> {
        return database.autosDao.getActualKms(autoId)
    }

    // REPOSTAJES   ************************************
    suspend fun insertRefueling(refuel: DbRefueling) {
        database.autosDao.insertRefueling(refuel)
        database.autosDao.updateAutoKms(refuel.cocheId, refuel.kms)
    }

    suspend fun getLastRefueling(carId: Int): DbRefueling? {
        return database.autosDao.getLastRefueling(carId)
    }

    suspend fun getRepostajes(carId: Int, offset: Int = 0): List<DbRefueling> {
        return database.autosDao.getRepostajes(carId)
    }

    suspend fun getAllRepostajes(): List<DbRefueling> {
        return database.autosDao.getAllRepostajes()
    }

    // ESTADISTICAS ************************************
    suspend fun getTotalPetrol(carId: Int): Float? {
        return database.autosDao.getTotalPetrol(carId)
    }

    suspend fun getTotalCost(carId: Int): Float? {
        return database.autosDao.getTotalCost(carId)
    }

    fun getMaxPrice(carId: Int): LiveData<CompoundPrice?> {
        return database.autosDao.getMaxPrice(carId)
    }

    fun getMinPrice(carId: Int): LiveData<CompoundPrice?> {
        return database.autosDao.getMinPrice(carId)
    }

    // MANTENIMIENTO
    suspend fun insertGasto(gasto: DbGasto): Long {
        val gastoId = database.autosDao.insertGasto(gasto)
        if (gastoId.toInt() != -1)
            database.autosDao.updateAutoKms(gasto.autoId, gasto.kms)
        return gastoId
    }

    suspend fun getGastosByAuto(carId: Int): List<DbGasto>? {
        return database.autosDao.getGastosByAuto(carId)
    }

    suspend fun getAllGastos(): List<DbGasto> {
        return database.autosDao.getAllGastos()
    }

    suspend fun getTotalGastos(autoId: Int): Float? {
        return database.autosDao.getTotalGastos(autoId)
    }

    suspend fun insertItem(item: DbItem) {
        database.autosDao.insertItem(item)
    }

    suspend fun getItemsFromGasto(gastoId: Int): List<DbItem> {
        return database.autosDao.getItemsFromGasto(gastoId)
    }

    suspend fun getAllItems(): List<DbItem> {
        return database.autosDao.getAllItems()
    }
}