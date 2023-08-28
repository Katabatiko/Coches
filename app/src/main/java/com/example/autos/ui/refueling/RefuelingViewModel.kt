package com.example.autos.ui.refueling

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.autos.AutosStatus
import com.example.autos.autoId
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.DbRefueling
import com.example.autos.data.local.asRefuelingListDomainModel
import com.example.autos.domain.DomainRefueling
import com.example.autos.repository.AutosRepository
import com.example.autos.util.flipDate
import com.example.autos.util.redondeaDecimales
import com.example.autos.util.standardizeDate
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

    val repostajes = MutableLiveData<List<DomainRefueling>>()


    val status: MutableState<AutosStatus> = mutableStateOf(AutosStatus.DONE)

    init {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) +1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        refuelingDate.value = standardizeDate("$day/$month/$year")
    }

    fun resetStatus() {
        status.value = AutosStatus.DONE
    }

    private fun resetData(){
        actualKms.value = ""
        precio.value = ""
        litros.value = ""
        coste.value = ""
        lleno.value = false
    }

    fun getRespostajes() {
        status.value = AutosStatus.LOADING
        viewModelScope.launch {
            repostajes.value = repository.getRepostajes(autoId).asRefuelingListDomainModel()
            setRecorridos()
            status.value = AutosStatus.DONE
        }
    }

    private fun setRecorridos() {
        val refuelings = repostajes.value
        if (!refuelings.isNullOrEmpty()) {
            refuelings.forEachIndexed { index, domainRefueling ->
                if (domainRefueling.recorrido == 0) {
                    val lastKms = if (index < (refuelings.size - 1)) {
                                        // al venir ordenados por kms decreciente
                                        // todos menos el primero (menor kms)
                                        refuelings[index + 1].kms
                                    } else {
                                        // refenencia del primero son los kms iniciales
                                        initKms
                                    }
                    domainRefueling.recorrido = (domainRefueling.kms - lastKms)
                }
            }
        } else  Log.d(TAG,"repostajes vacio o nulo")
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

    fun saveRefueling() {
        val refueling = DbRefueling(
            cocheId = autoId,
            euros = coste.value.replace(",",".").toFloat(),
            litros = litros.value.replace(",",".").toFloat(),
            eurosLitro = precio.value.replace(",",".").toFloat(),
            kms = actualKms.value.toInt(),
            lleno = lleno.value,
            fecha = flipDate(refuelingDate.value)
        )
        Log.d(TAG,"refueling last: $refueling")
//        updateAutoKms(autoId, refueling.kms)
        viewModelScope.launch {
            repository.insertRefueling(refueling)
        }
        resetData()
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