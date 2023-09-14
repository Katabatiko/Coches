package com.example.autos

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.DbAuto
import com.example.autos.data.local.DbGasto
import com.example.autos.data.local.DbItem
import com.example.autos.data.local.DbRefueling
import com.example.autos.data.local.asDomainModel
import com.example.autos.data.local.asDomainAuto
import com.example.autos.domain.DomainCoche
import com.example.autos.domain.DomainRefueling
import com.example.autos.repository.AutosRepository
import com.example.autos.util.Limits
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

private const val TAG = "xxMavm"

private const val AUTOS = "DbAuto"
private const val REPOSTAJES = "DbRefueling"
private const val GASTOS = "DbGasto"
private const val ITEMS = "DbItem"


class MainActivityViewModel(val app: Application, private val repository: AutosRepository): ViewModel() {

    val firstStart = mutableStateOf(false)
    private val restored = mutableStateOf(false)
    val status: MutableState<AutosStatus> = mutableStateOf(AutosStatus.DONE)

    val auto: MutableState<DomainCoche?> = mutableStateOf(null)

    var lastRefueling: MutableState<DomainRefueling?> = mutableStateOf(null)
    var lastOilChangeFrom: MutableState<Int> = mutableStateOf(0)
    var lastAirFilterChangeFrom: MutableState<Int> = mutableStateOf(0)
    var lastFrontTiresChangeFrom: MutableState<Int> = mutableStateOf(0)
    var lastBackTiresChangeFrom: MutableState<Int> = mutableStateOf(0)
    var lastKms: MutableState<Int> = mutableStateOf(0)

    lateinit var cars: List<DbAuto>
    private lateinit var refuelings: List<DbRefueling>
    private lateinit var gastos: List<DbGasto>
    lateinit var items: List<DbItem>

    val datosRecibidos = MutableLiveData(false)

    var restoredVehicles = 0

    init {
        if (autoId != -1) {
            viewModelScope.launch {
                auto.value = repository.getAuto(autoId).asDomainAuto()
                lastKms.value = auto.value!!.actualKms
                lastRefueling.value = repository.getLastRefueling(autoId)?.asDomainModel()

                setLastSpareChangeFrom()
            }
        } else {
            firstStart.value = true
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            // necesario copiar para que el estado note el cambio de un solo atributo (actualKms por refueling o gasto)
            // se pasan todos los atributos para el caso de que sea cambio de auto
            val (id, marca, modelo, matricula, year, initKms, actualKms, buyDate) = repository.getAuto(autoId).asDomainAuto()

            if (auto.value != null) {
                auto.value = auto.value!!.copy(
                    id = id,
                    marca = marca,
                    modelo = modelo,
                    matricula = matricula,
                    year = year,
                    initKms = initKms,
                    actualKms = actualKms,
                    buyDate = buyDate
                )
            } else {
                // para el caso de restauracion desde archivo
                auto.value = DomainCoche(
                    id = id,
                    marca = marca,
                    modelo = modelo,
                    matricula = matricula,
                    year = year,
                    initKms = initKms,
                    actualKms = actualKms,
                    buyDate = buyDate
                )
            }

            lastRefueling.value = repository.getLastRefueling(autoId)?.asDomainModel()
            lastKms.value = actualKms
            setLastSpareChangeFrom()
        }
    }

    private suspend fun setLastSpareChangeFrom() {
        lastOilChangeFrom.value = repository.getLastSpareChange(autoId, app.getString(R.string.search_oil)).let {
            if (it != null){
                lastKms.value - it
            } else {
                lastKms.value % Limits.OIL.highLimit
            }
        }
//        Log.d(TAG,"refresh lastOilChangeFrom: ${lastOilChangeFrom.value}")
        lastAirFilterChangeFrom.value = repository.getLastSpareChange(autoId, app.getString(R.string.search_air_filter)).let {
            if (it != null){
                lastKms.value - it
            } else {
                lastKms.value % Limits.AIR.highLimit
            }
        }
//        Log.d(TAG,"refresh lastAirChange: ${lastAirFilterChangeFrom.value}")
        lastFrontTiresChangeFrom.value = repository.getLastSpareChange(autoId, app.getString(R.string.search_tire_front)).let {
            if (it != null){
                lastKms.value - it
            } else {
                lastKms.value % Limits.TIRES.highLimit
            }
        }
//        Log.d(TAG,"refresh lastFrontTiresChange: ${lastFrontTiresChangeFrom.value}")
        lastBackTiresChangeFrom.value = repository.getLastSpareChange(autoId, app.getString(R.string.search_tire_back)).let {
            if (it != null){
                lastKms.value - it
            } else {
                lastKms.value % Limits.TIRES.highLimit
            }
        }
//        Log.d(TAG,"refresh lastBackTiresChange: ${lastBackTiresChangeFrom.value}")
    }

    fun getDataForBackup() = runBlocking {
        val success = retrieveDataAsync()
        datosRecibidos.value = success
    }

    private suspend fun retrieveDataAsync() = coroutineScope {
        val deferredCars = async { cars = repository.getAutos() }
        val deferredRefuels = async { refuelings = repository.getAllRepostajes() }
        val deferredGastos = async { gastos = repository.getAllGastos() }
        val deferredItems = async { items = repository.getAllItems() }

        deferredCars.await()
        deferredRefuels.await()
        deferredGastos.await()
        deferredItems.await()

        cars.isNotEmpty() /*&& refuelings.isNotEmpty()*/
    }


    fun editFile(uri: Uri): Boolean{

        var jsonString = """{"$AUTOS":"""
        jsonString = jsonString.plus(dataToJson(cars as List<Any>))

        jsonString = jsonString.plus(""","$REPOSTAJES":""")
        jsonString = jsonString.plus(dataToJson(refuelings as List<Any>))

        jsonString = jsonString.plus(""","$GASTOS":""")
        jsonString = jsonString.plus(dataToJson(gastos as List<Any>))

        jsonString = jsonString.plus(""","$ITEMS":""")
        jsonString = jsonString.plus(dataToJson(items as List<Any>))

        jsonString = jsonString.plus("}")

        val contentResolver = app.contentResolver

        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { fos ->
                    fos.write(jsonString.toByteArray())
                }
            }
        }catch (e: FileNotFoundException){
            e.printStackTrace()
            return false
        }catch (e: IOException){
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun <Any> dataToJson(data: List<Any>): String {
        val gson = Gson()

        return gson.toJsonTree(data).toString()
    }

    suspend fun rebuildDataAsync(uri: Uri): Boolean {
        status.value = AutosStatus.LOADING
        var finished = false
        var read = ""

        val contentResolver = app.contentResolver
        val gson = GsonBuilder().create()

        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(inputStream.reader()).use {
                    read = it.readText()
            }
        }

        try {
            val jsonObject = JSONObject(read)

            val jsonAutos = jsonObject.getJSONArray(AUTOS)
            val numAutos = jsonAutos.length()
            val jsonRepos = jsonObject.getJSONArray(REPOSTAJES)
            val numRepos = jsonRepos.length()
            val jsonGastos = jsonObject.getJSONArray(GASTOS)
            val numGastos = jsonGastos.length()
            val jsonItems = jsonObject.getJSONArray(ITEMS)
            val numItems = jsonItems.length()

            val deferredRestore: Deferred<Boolean>

            // si es el primer arranque no hay datos en la BD, por lo que se mantienen los Id de los elementos
            if (firstStart.value) {
                deferredRestore = viewModelScope.async {
                    var latestId = 1
                    var latestFecha = ""
                    for (i in 0 until numAutos) {
                        val auto = gson.fromJson(jsonAutos.getString(i), DbAuto::class.java)
                        val id = repository.insertAuto(auto).toInt()
                        if (id != -1)   restoredVehicles ++

                        // se busca el ultimo auto registrado
                        if (auto.buyDate > latestFecha) {
                            latestId = auto.id
                            latestFecha = auto.buyDate
                        }
                    }

                    for (i in 0 until numRepos) {
                        val repo = gson.fromJson(jsonRepos.getString(i), DbRefueling::class.java)
                        repository.insertRefueling(repo)
                    }

                    for (i in 0 until numGastos) {
                        val gasto = gson.fromJson(jsonGastos.getString(i), DbGasto::class.java)
                        repository.insertGasto(gasto)
                    }

                    for (i in 0 until numItems) {
                        val item = gson.fromJson(jsonItems.getString(i), DbItem::class.java)
                        repository.insertItem(item)
                    }

                    status.value = AutosStatus.DONE

                    if (restoredVehicles > 0) {
                        firstStart.value = false
                        restored.value = true
                        repository.setActualAutoId(latestId)
                        return@async true
                    }
                    return@async false
                }
            } else {
                // si ya hay datos en la BD, es necesario reasignar el Id de los autos (y sus repostajes y gastos) con nuevos Id
                var repos: List<DbRefueling> = listOf()
                var gastos: List<DbGasto> = listOf()
                var items: List<DbItem> = listOf()

                for (i in 0 until numRepos) {
                    val repo = gson.fromJson(jsonRepos.getString(i), DbRefueling::class.java)
                    repos = repos.plus(repo)
                }

                for (i in 0 until numGastos) {
                    val gasto = gson.fromJson(jsonGastos.getString(i), DbGasto::class.java)
                    gastos = gastos.plus(gasto)
                }

                for (i in 0 until numItems) {
                    val item = gson.fromJson(jsonItems.getString(i), DbItem::class.java)
                    items = items.plus(item)
                }

                deferredRestore = viewModelScope.async {
                    for (i in 0 until numAutos) {
                        val auto = gson.fromJson(jsonAutos.getString(i), DbAuto::class.java)
                        // insercion y registro de los Id viejos y nuevos
                        val oldId = auto.id
                        auto.id = 0
                        val newId = repository.insertAuto(auto).toInt()
                        if ( newId != -1 ) {
                            restoredVehicles++

                            repos.forEach {
                                it.refuelId = 0
                                if (it.cocheId == oldId) it.cocheId = newId
                            }
                            gastos.forEach {
                                it.gastoId = 0
                                if (it.autoId == oldId) it.autoId = newId
                            }
                        }
                    }

                    repos.forEach { repository.insertRefueling(it) }

                    gastos.forEach { gasto ->
                        val oldId = gasto.gastoId
                        gasto.gastoId = 0
                        val newId = repository.insertGasto(gasto).toInt()
                        items.forEach {
                            it.itemId = 0
                            if (it.gastoId == oldId)    it.gastoId = newId
                        }
                    }
                    items.forEach { repository.insertItem(it) }

                    status.value = AutosStatus.DONE

                    if (restoredVehicles > 0) {
                        firstStart.value = false
                        restored.value = true
                        return@async true
                    }
                    return@async false
                }
            }
            finished = deferredRestore.await()

        } catch (e: Exception){
            Log.d(TAG,"Error de recuperacion: $e")
            Toast.makeText(app, "${app.getText(R.string.error_restore)} $e", Toast.LENGTH_LONG).show()
            status.value = AutosStatus.ERROR
        }
        return finished
    }


    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val repository = AutosRepository(AutosDatabase.getDatabase(application.applicationContext), application)
                MainActivityViewModel(application, repository)
            }
        }
    }
}