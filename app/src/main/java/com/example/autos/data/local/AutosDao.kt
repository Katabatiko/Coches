package com.example.autos.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AutosDao {

    // AUTOS
    @Insert(entity = DbAuto::class)
    suspend fun  insertAuto(auto: DbAuto): Long

    @Query("select * from DbAuto order by buyDate desc")
    fun getAllAutos() : LiveData<List<DbAuto>>

    @Query("select * from DbAuto order by id asc")
    suspend fun getAutos() : List<DbAuto>

    @Query("select * from DbAuto where id = :autoId")
    suspend fun getAuto(autoId: Int): DbAuto

    @Query("select count(*) from DbAuto")
    fun countAutos(): LiveData<Int>

    @Query("select initKms from DbAuto where id= :autoId")
    suspend fun getAutoInitialKms(autoId: Int): Int

    @Update(DbAuto::class, onConflict = OnConflictStrategy.REPLACE)
    fun updateAuto(car: DbAuto)

    @Query("update dbauto set lastKms= :lastKms where id= :carId")
    suspend fun updateAutoKms(carId: Int, lastKms: Int)

    @Query("select lastKms from DbAuto where id= :autoId")
    fun getActualKms(autoId: Int): LiveData<Int>


    // REPOSTAJES
    @Insert(entity = DbRefueling::class)
    suspend fun insertRefueling(refuel: DbRefueling)

    @Query("select * from DbRefueling where cocheId= :carId order by kms desc limit 1")
    suspend fun getLastRefueling(carId: Int): DbRefueling?

    @Query("select * from DbRefueling where cocheId= :carId order by kms desc")
//    fun getRepostajes(carId: Int): LiveData<List<DbRefueling>>
    suspend fun getRepostajes(carId: Int): List<DbRefueling>

    @Query("select * from DbRefueling")
    suspend fun getAllRepostajes(): List<DbRefueling>

    // ESTADISTICAS
    @Query("select sum(euros) from DbRefueling where cocheId= :carId")
    suspend fun getTotalCost(carId: Int): Float?

    @Query("select sum(litros) from DbRefueling where cocheId= :carId")
    suspend fun getTotalPetrol(carId: Int): Float?

    @Query("select max(eurosLitro) as price, fecha from DbRefueling where cocheId= :carId")
    fun getMaxPrice(carId: Int): LiveData<CompoundPrice?>

    @Query("select min(eurosLitro) as price, fecha from DbRefueling where cocheId= :carId")
    fun getMinPrice(carId: Int): LiveData<CompoundPrice?>

    // MANTENIMIENTO
    @Insert(entity = DbGasto::class)
    suspend fun insertGasto(gasto: DbGasto): Long

    @Query("select * from DbGasto where autoId= :carId order by fecha desc")
    suspend fun getGastosByAuto(carId: Int): List<DbGasto>?

    @Query("select * from DbGasto")
    suspend fun getAllGastos(): List<DbGasto>

    @Query("select sum(importe) from dbgasto where autoId= :autoId")
    suspend fun getTotalGastos(autoId: Int): Float?

    @Insert(entity = DbItem::class)
    suspend fun insertItem(item: DbItem)

    @Query("select * from DbItem where gastoId = :gastoId")
    suspend fun getItemsFromGasto(gastoId: Int): List<DbItem>

    @Query("select * from DbItem")
    suspend fun getAllItems(): List<DbItem>
}