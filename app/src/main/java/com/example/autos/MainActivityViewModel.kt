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
import com.example.autos.data.local.DbRefueling
import com.example.autos.data.local.asDomainModel
import com.example.autos.domain.DomainCoche
import com.example.autos.domain.DomainRefueling
import com.example.autos.repository.AutosRepository
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

private const val TAG = "xxMavm"

class MainActivityViewModel(val app: Application, val repository: AutosRepository): ViewModel() {


    var autoId = mutableStateOf(-1)
    var firstStart = mutableStateOf(false)

    var auto: MutableState<DomainCoche?> = mutableStateOf(null)
    var lastRefueling: MutableState<DomainRefueling?> = mutableStateOf(null)

    lateinit var cars: List<DbAuto>
    lateinit var refuelings: List<DbRefueling>

    val datosRecibidos = MutableLiveData(false)

    var restoredVehicles = 0

    init {
//        Log.d(TAG,"init autoId: ${autoId.value}")
        getActualAutoId()
        if (autoId.value != -1) {
            viewModelScope.launch {
                auto.value = repository.getAuto(autoId.value).asDomainModel()
                lastRefueling.value = repository.getLastRefueling(autoId.value)?.asDomainModel()
            }
        } else {
            firstStart.value = true
        }
    }

    fun getActualAutoId() {
        autoId.value = repository.getActualAutoId()
    }

    fun refreshData() {
//        Log.d(TAG,"refresh data autoId: ${autoId.value}")
        viewModelScope.launch {
            // necesario copiar para que el estado note el cambio de un solo atributo (actualKms por refueling)
            // se pasan todos los atributos para el caso de que cambie el auto
            val (id, marca, modelo, matricula, year, initKms, actualKms, buyDate) = repository.getAuto(autoId.value).asDomainModel()
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
                auto.value = repository.getAuto(autoId.value).asDomainModel()
            }
            lastRefueling.value = repository.getLastRefueling(autoId.value)?.asDomainModel()
        }


    }

    fun getData() = runBlocking {
        val success = retrieveDataAsync()
//        Log.d(TAG,"deferred get data: $success")
        datosRecibidos.value = success
    }

    private suspend fun retrieveDataAsync() = coroutineScope {
        val deferredCars = async { cars = repository.getAutos() }
        val deferredRefuels = async { refuelings = repository.getAllRepostajes() }
        deferredCars.await()
//        Log.d(TAG,"deferredCars: $deferredCars")
        deferredRefuels.await()
//        Log.d(TAG,"deferredRefuels: $deferredRefuels")
        cars.isNotEmpty() /*&& refuelings.isNotEmpty()*/
    }


    fun editFile(uri: Uri, jsonString: String): Boolean{
//        Log.d(TAG,"editFile uri: $uri")
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

    fun <Any> dataToJson(data: List<Any>): String {
        val gson = Gson()

        return gson.toJsonTree(data).toString()
    }

    fun rebuildData(uri: Uri): Boolean {
        var success = false
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

            val jsonAutos = jsonObject.getJSONArray("Autos")
            val numAutos = jsonAutos.length()
//            Log.d(TAG, "numAutos: $numAutos")
            val jsonRepos = jsonObject.getJSONArray("Refueling")
            val numRepos = jsonRepos.length()
//            Log.d(TAG, "numRepos: $numRepos")

            // si es el primer arranque no hay datos en la BD, por lo que se mantienen los Id de los autos
            if (firstStart.value) {
                viewModelScope.launch {
                    for (i in 0 until numAutos) {
                        val auto = gson.fromJson(jsonAutos.getString(i), DbAuto::class.java)
                        repository.insertAuto(auto)
                    }
                    repository.setActualAutoId(1)

                    for (i in 0 until numRepos) {
                        val repo = gson.fromJson(jsonRepos.getString(i), DbRefueling::class.java)
                        repository.insertRefueling(repo)
                    }
                }
            } else {
                // si ya hay datos en la BD, es necesario reasignar el Id de los autos (y sus repostajes) con nuevos Id
                // lista de parejas con <oldAutoId, newAutoId>
                var autosIdList: Map<Int, Int> = mapOf()

                viewModelScope.launch {
                    for (i in 0 until numAutos) {
                        val auto = gson.fromJson(jsonAutos.getString(i), DbAuto::class.java)
                        val oldId = auto.id
                        auto.id = 0
                        // insercion y registro de los Id viejos y nuevos
                        autosIdList =
                            autosIdList.plus(Pair(oldId, repository.insertAuto(auto).toInt()))
                    }
//                    Log.d(TAG, "autosIdList: $autosIdList")

                    for (i in 0 until numRepos) {
                        val repo = gson.fromJson(jsonRepos.getString(i), DbRefueling::class.java)
                        repo.refuelId = 0
                        repo.cocheId = autosIdList[repo.cocheId]!!
                        repository.insertRefueling(repo)
                    }
                }
            }
            restoredVehicles = numAutos
            if (restoredVehicles > 0) {
                firstStart.value = false
                success = true
            }
        } catch (e: Exception){
            Log.d(TAG,"Error de recuperacion: $e")
            Toast.makeText(app, "${app.getText(R.string.error_restore)} $e", Toast.LENGTH_LONG).show()
        }
        return success
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