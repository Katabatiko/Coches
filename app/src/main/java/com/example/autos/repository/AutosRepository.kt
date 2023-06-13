package com.example.autos.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.CompoundPrice
import com.example.autos.data.local.DbAuto
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

    suspend fun  insertAuto(auto: DbAuto): Long {
        return database.autosDao.insertAuto(auto)
    }

    fun getAllAutos() : LiveData<List<DbAuto>> {
        return database.autosDao.getAllAutos()
    }

    suspend fun getAutos(): List<DbAuto> {
        return database.autosDao.getAutos()
    }

    fun getAuto(autoId: Int): LiveData<DbAuto> {
        return database.autosDao.getAuto(autoId)
    }

    fun updateAuto(auto: DbAuto){
        database.autosDao.updateAuto(auto)
    }

    suspend fun updateAutoKms(cardId: Int, lastKms: Int){
        database.autosDao.updateAutoKms(cardId, lastKms)
    }

    fun getAutoInitialKms(carId: Int): LiveData<Int>{
        return database.autosDao.getAutoInitialKms(carId)
    }

    suspend fun insertRefueling(refuel: DbRefueling) {
        database.autosDao.insertRefueling(refuel)
    }

    fun getLastRefueling(carId: Int): LiveData<DbRefueling?> {
        return database.autosDao.getLastRefueling(carId)
    }

    fun getRepostajes(carId: Int, offset: Int = 0): LiveData<List<DbRefueling>> {
        return database.autosDao.getRepostajes(carId)
    }

    suspend fun getAllRepostajes(): List<DbRefueling> {
        return database.autosDao.getAllRepostajes()
    }

    suspend fun getTotalPetrol(carId: Int): Float {
        return database.autosDao.getTotalPetrol(carId)
    }

    suspend fun getTotalCost(carId: Int): Float {
        return database.autosDao.getTotalCost(carId)
    }

    fun getMaxPrice(carId: Int): LiveData<CompoundPrice> {
        return database.autosDao.getMaxPrice(carId)
    }

    fun getMinPrice(carId: Int): LiveData<CompoundPrice> {
        return database.autosDao.getMinPrice(carId)
    }
}