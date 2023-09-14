package com.example.autos.ui.refueling

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.autos.AutosStatus
import com.example.autos.autoId
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.DbRefueling
import com.example.autos.domain.DomainRefueling
import com.example.autos.repository.AutosRepository
import com.example.autos.repository.RefuelingsPagingSource
import com.example.autos.util.flipDate
import com.example.autos.util.redondeaDecimales
import com.example.autos.util.standardizeDate
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar

private const val TAG = "xxRvm"

class RefuelingViewModel(
    private val repository: AutosRepository
) : ViewModel() {

    val refuelingDate: MutableState<String> = mutableStateOf("")
    val actualKms: MutableState<String> = mutableStateOf("")
    val precio: MutableState<String> = mutableStateOf("")
    val coste: MutableState<String> = mutableStateOf("")
    val litros: MutableState<String> = mutableStateOf("")
    val lleno: MutableState<Boolean> = mutableStateOf(false)

    var initKms = 0
    val lastKms: MutableState<Int> = mutableStateOf(0)
    var lastRefuelKms = 0

    val pagingRefuels: Flow<PagingData<DomainRefueling>>

    init {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) +1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        refuelingDate.value = standardizeDate("$day/$month/$year")
        pagingRefuels =
            Pager(
                config = PagingConfig(pageSize = 10),
                initialKey = 1,
                pagingSourceFactory = {
                    RefuelingsPagingSource(repository, autoId)
                }
            ).flow.cachedIn(viewModelScope)

        viewModelScope.launch {
            lastKms.value  = repository.getActualKms(autoId)
        }
    }

    private fun resetData(){
        actualKms.value = ""
        precio.value = ""
        litros.value = ""
        coste.value = ""
        lleno.value = false
    }

    fun calcularLitros(){
        if (precio.value.isNotBlank() && precio.value != "0")
            litros.value =
                redondeaDecimales(
                    (coste.value
                        .replace(",",".")
                        .toFloat()
                            /
                    precio.value
                        .replace(",",".")
                        .toFloat()
                    ), 2).toString()
//            )
    }

    fun saveRefueling(): Deferred<Boolean> {
        val refueling = DbRefueling(
            cocheId = autoId,
            euros = coste.value.replace(",",".").toFloat(),
            litros = litros.value.replace(",",".").toFloat(),
            eurosLitro = precio.value.replace(",",".").toFloat(),
            kms = actualKms.value.toInt(),
            lleno = lleno.value,
            fecha = flipDate(refuelingDate.value),
            recorrido = actualKms.value.toInt() - lastRefuelKms
        )

        return viewModelScope.async {
            return@async insertRefueling(refueling)
        }
    }

    private suspend fun insertRefueling(refueling: DbRefueling): Boolean {
        val deferredResult = viewModelScope.async {
            val id: Int
            try {
                id = repository.insertRefueling(refueling, lastKms.value < refueling.kms)
            } catch (e: Exception) {
                Log.e(TAG,"Error de insercion: $e")
                return@async false
            }
            resetData()
            (id != -1)
        }

        return try {
            deferredResult.await()
        } catch (e: Exception) {
            false
        }
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val repository = AutosRepository(AutosDatabase.getDatabase(application.applicationContext), application)
                RefuelingViewModel(repository)
            }
        }
    }
}