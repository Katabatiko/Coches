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
    suspend fun getActualKms(autoId: Int): Int


    // REPOSTAJES
    @Insert(entity = DbRefueling::class)
    suspend fun insertRefueling(refuel: DbRefueling): Long

    @Query("select * from DbRefueling where cocheId= :carId order by kms desc limit 1")
    suspend fun getLastRefueling(carId: Int): DbRefueling?

    @Query("select * from DbRefueling where cocheId= :carId order by kms desc limit :offset, 10")
    suspend fun getRepostajes(carId: Int, offset: Int): List<DbRefueling>

    @Query("select * from DbRefueling")
    suspend fun getAllRepostajes(): List<DbRefueling>


    // ESTADISTICAS
    @Query("select max(fecha) as latest, min(fecha) as oldest from dbrefueling where cocheId = :carId")
    suspend fun getDateRange(carId: Int): DateRange?

    @Query("select sum(recorrido) from dbrefueling where cocheId= :carId and fecha like :year")
    suspend fun getKmsByYear(carId: Int, year: String): Int?

    @Query("select sum(euros) from DbRefueling where cocheId= :carId")
    suspend fun getTotalCost(carId: Int): Float?

    @Query("select sum(importe) from dbgasto where autoId= :carId and fecha like :year")
    suspend fun getGastosByYear(carId: Int, year: String): Float?

    @Query("select sum(litros) from DbRefueling where cocheId= :carId")
    suspend fun getTotalPetrol(carId: Int): Float?

    @Query("select max(eurosLitro) as price, fecha from DbRefueling where cocheId= :carId")
    fun getTotalMaxPrice(carId: Int): LiveData<CompoundPrice>

    @Query("select min(eurosLitro) as price, fecha from DbRefueling where cocheId= :carId")
    fun getTotalMinPrice(carId: Int): LiveData<CompoundPrice>

    @Query("select max(eurosLitro) as price, fecha from DbRefueling where cocheId= :carId and fecha like :year")
    suspend fun getMaxPriceByYear(carId: Int, year: String): CompoundPrice

    @Query("select min(eurosLitro) as price, fecha from DbRefueling where cocheId= :carId and fecha like :year")
    suspend fun getMinPriceByYear(carId: Int, year: String): CompoundPrice

    @Query("select * from dbrefueling where cocheId= :carId and lleno=1 order by kms desc")
    suspend fun getLastFullRefueling(carId: Int): List<DbRefueling>

    @Query("select * from dbrefueling where cocheId= :carId and kms between :init and :end order by kms desc")
    suspend fun getLastFullRefuelingAndAmongThen(carId: Int, init: Int, end: Int): List<DbRefueling>

    // MANTENIMIENTO
    @Insert(entity = DbGasto::class)
    suspend fun insertGasto(gasto: DbGasto): Long

    @Query("select * from DbGasto where autoId= :carId order by kms desc")
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

    @Query("select max(kms) from dbgasto where autoId= :autoId" +
            " and (concepto like :concepto or gastoId in" +
            " (select gastoId from dbitem where descripcion like :concepto ))")
    suspend fun getLastSpareChange(autoId: Int, concepto: String): Int?

    @Query("select * from dbgasto where autoId= :autoId" +
            " and (concepto like :concepto or gastoId in" +
            " (select gastoId from dbitem where descripcion like :concepto )) order by kms desc")
    suspend fun getCambiosRueda(autoId: Int, concepto: String): List<DbGasto>

}