package com.example.autos

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.autos.data.local.DbAuto
import com.example.autos.data.local.DbRefueling
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

private const val TAG = "xxMavm"

class MainActivityViewModel(val app: Application, val repository: AutosRepository): AndroidViewModel(app) {

    lateinit var cars: List<DbAuto>
    lateinit var refuelings: List<DbRefueling>

    val datosRecibidos = MutableLiveData<Boolean>().apply { this.value = false }

    var restoredVehicles = 0

    fun getData() = runBlocking {
        val success = retrieveDataAsync()
        Log.d(TAG,"deferred get data: $success")
        datosRecibidos.value = success
    }

    suspend fun retrieveDataAsync() = coroutineScope {
        val deferredCars = async { cars = repository.getAutos() }
        val deferredRefuels = async { refuelings = repository.getAllRepostajes() }
        deferredCars.await()
        Log.d(TAG,"deferredCars: $deferredCars")
        deferredRefuels.await()
        Log.d(TAG,"deferredRefuels: $deferredRefuels")
        cars.isNotEmpty() && refuelings.isNotEmpty()

    }


    fun editFile(uri: Uri, jsonString: String): Boolean{
        Log.d(TAG,"editFile uri: $uri")
//        Log.d(TAG,"editFile jsonString: $jsonString")
        val contentResolver = app.contentResolver

        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use {
                    it.write(jsonString.toByteArray())
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

    fun <Any> dataToJson(data: List<Any>, clase: String): String{
        val gson = Gson()
        var jsonString = gson.toJsonTree(data).toString()
        jsonString = "$clase·$jsonString"

//        Log.d(TAG, "json out: $jsonString")
        return jsonString
    }

    fun rebuildData(uri: Uri) {
        Log.d(TAG, "making json de ${uri.path}")
        val contentResolver = app.contentResolver

        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(inputStream.reader()).use {
                    distributeDataFromJsonString(it.readText())
            }
        }
    }

    private fun distributeDataFromJsonString(datos: String){

        var autosJsonString = ""
        var refuelingsJsonString = ""

        val leido = datos.split("|")
        leido.forEach { item ->
            val clase = item.split("·")[0]
            Log.d(TAG,"clase: $clase")
            when(clase){
                "Autos" -> {
                    autosJsonString = leido[0].split("·")[1]
                    Log.d(TAG,"autosJsonString: $autosJsonString")
                }

                "Refuelings" -> {
                    refuelingsJsonString = leido[1].split("·")[1]
                    Log.d(TAG,"refuelingsJsonString: $refuelingsJsonString")
                }
            }
        }


        viewModelScope.launch {
            val autosIds = insertAutosFromJsonString(autosJsonString)

            insertRefuelingsFromJsonString(refuelingsJsonString,autosIds)
        }

    }

    private suspend fun insertAutosFromJsonString(autosJsonString: String): Map<Int,Int>{
        val gson = GsonBuilder().create()

        // lista de parejas con <oldAutoId, newAutoId>
        var autosIdList: Map<Int,Int> = mapOf()

        var oldAutoIds: List<Int> = listOf()
        var newAutosId: List<Int> = listOf()

        val autosObjects: List<DbAuto> = gson.fromJson(autosJsonString, Array<DbAuto>::class.java).toList()
        restoredVehicles = autosObjects.size
        // reasignacion del id del auto por si ya existen autos en la bd
        autosObjects.map {
            oldAutoIds = oldAutoIds.plus(it.id)
            it.id = 0
        }

        for (auto in autosObjects){
            newAutosId = newAutosId.plus(repository.insertAuto(auto).toInt())
        }
//        Log.d(TAG,"newAutoIds: $newAutosId")

        oldAutoIds.forEachIndexed { i, oldId ->
            autosIdList = autosIdList.plus(Pair(oldId, newAutosId[i]))
        }
//        Log.d(TAG,"autosIdPairs: $autosIdList")

        return autosIdList
    }

    private suspend fun insertRefuelingsFromJsonString(refuelingsJsonString: String, autosIdMap: Map<Int,Int>){
        val gson = GsonBuilder().create()

        val refuelingsObjects: List<DbRefueling> = gson.fromJson(refuelingsJsonString, Array<DbRefueling>::class.java).toList()
        refuelingsObjects.map {
            it.refuelId = 0
            Log.d(TAG,"newCocheId: ${autosIdMap[it.cocheId]!!}")
            it.cocheId = autosIdMap[it.cocheId]!!
        }

        for (refueling in refuelingsObjects){
            repository.insertRefueling(refueling)
        }
    }
}

class MainActivityViewModelFactory(val app: Application, val repository: AutosRepository): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainActivityViewModel(app, repository) as T
    }
}