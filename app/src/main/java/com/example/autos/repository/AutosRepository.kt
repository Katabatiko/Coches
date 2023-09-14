package com.example.autos.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import com.example.autos.R
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.CompoundPrice
import com.example.autos.data.local.DateRange
import com.example.autos.data.local.DbAuto
import com.example.autos.data.local.DbGasto
import com.example.autos.data.local.DbItem
import com.example.autos.data.local.DbRefueling
import com.example.autos.data.local.asRefuelingDomainModel
import com.example.autos.domain.DomainRefueling
import com.example.autos.domain.KmsByYear

private const val TAG = "xxArepo"

class AutosRepository(private val database: AutosDatabase, val application: Application) {

    private val preferences = application.getSharedPreferences(application.getString(R.string.preference_file_key),Context.MODE_PRIVATE)

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

    suspend fun getActualKms(autoId: Int): Int {
        return database.autosDao.getActualKms(autoId)
    }

    // REPOSTAJES   ************************************
    suspend fun insertRefueling(refuel: DbRefueling, updateKms: Boolean = false): Int {
        val id = database.autosDao.insertRefueling(refuel)
        if (updateKms)  database.autosDao.updateAutoKms(refuel.cocheId, refuel.kms)
        return id.toInt()
    }

    suspend fun getLastRefueling(carId: Int): DbRefueling? {
        return database.autosDao.getLastRefueling(carId)
    }

    suspend fun getRepostajes(carId: Int, offset: Int = 0): List<DomainRefueling>  {
        return database.autosDao.getRepostajes(carId, offset).asRefuelingDomainModel()
    }

    suspend fun getAllRepostajes(): List<DbRefueling> {
        return database.autosDao.getAllRepostajes()
    }

    suspend fun getLastFullRefuelingAndAmongThem(carId: Int): List<DbRefueling> {
        val full = database.autosDao.getLastFullRefueling(carId)
        return  if (full.isNotEmpty()) {
                    val init = full[full.size - 1].kms
                    val end = full[0].kms
                    database.autosDao.getLastFullRefuelingAndAmongThen(carId, init, end)
                } else listOf()
    }

    // ESTADISTICAS ************************************
    suspend fun getTotalPetrol(carId: Int): Float? {
        return database.autosDao.getTotalPetrol(carId)
    }

    suspend fun getTotalCost(carId: Int): Float? {
        return database.autosDao.getTotalCost(carId)
    }

    fun getTotalMaxPrice(carId: Int): LiveData<CompoundPrice> {
        return database.autosDao.getTotalMaxPrice(carId)
    }

    fun getTotalMinPrice(carId: Int): LiveData<CompoundPrice> {
        return database.autosDao.getTotalMinPrice(carId)
    }

    suspend fun getlMaxPriceByYear(carId: Int, year: String): CompoundPrice {
        return database.autosDao.getMaxPriceByYear(carId, year)
    }

    suspend fun getMinPriceByYear(carId: Int, year: String): CompoundPrice {
        return database.autosDao.getMinPriceByYear(carId, year)
    }

    suspend fun getDateRange(carId: Int): DateRange?{
        return database.autosDao.getDateRange(carId)
    }

    suspend fun getCambiosRueda(autoId: Int, delanteras: Boolean): List<DbGasto> {
        return  if (delanteras)
                    database.autosDao.getCambiosRueda(autoId, application.getString(R.string.search_tire_front))
                else
                    database.autosDao.getCambiosRueda(autoId, application.getString(R.string.search_tire_back))
    }

    suspend fun getKmsByYear(carId: Int): List<KmsByYear> {
        var kmsByYear = listOf<KmsByYear>()
        val rangeDate = database.autosDao.getDateRange(carId)
        if (rangeDate != null) {
            val oldest = rangeDate.oldest?.split("/")?.get(0)?.toInt()
            val latest = rangeDate.latest?.split("/")?.get(0)?.toInt()

            if (latest != null && oldest != null) {
                for (year in latest downTo oldest) {
                    val kms = database.autosDao.getKmsByYear(carId, "$year%")
                    // por si hay años sin registros
                    if (kms != null) {
                        val anno = KmsByYear(year = year.toString(), kms = kms)
                        kmsByYear = kmsByYear.plus(anno)
                    }
                }
            }
        }
        return kmsByYear
    }

    // MANTENIMIENTO ******************************************
    suspend fun insertGasto(gasto: DbGasto, updateKms: Boolean = false): Long {
        val gastoId = database.autosDao.insertGasto(gasto)
        if (gastoId.toInt() != -1 && updateKms)
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

    suspend fun getLastSpareChange(autoId: Int, concepto: String): Int? {
        return database.autosDao.getLastSpareChange(autoId, concepto)
    }
}