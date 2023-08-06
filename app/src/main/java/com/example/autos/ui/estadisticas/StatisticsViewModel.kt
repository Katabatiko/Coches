package com.example.autos.ui.estadisticas

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.CompoundPrice
import com.example.autos.repository.AutosRepository
import com.example.autos.util.redondeaDecimales
import kotlinx.coroutines.launch

private const val TAG = "xxSvm"

class StatisticsViewModel(
    val app: Application,
    private val repository: AutosRepository
    ) : AndroidViewModel(app) {

    private val autoId = repository.getActualAutoId()

    var totalKmsRecorridos: MutableState<Int> = mutableStateOf(0)
    var lastLitros: MutableState<Float> = mutableStateOf(0f)

    private val _costeTotal = MutableLiveData<Float?>()
    val costeTotal: LiveData<Float?>
        get() = _costeTotal

    private val _petrolTotal = MutableLiveData<Float?>()
    val petrolTotal: LiveData<Float?>
        get() = _petrolTotal

    val maxPrice: LiveData<CompoundPrice?> = repository.getMaxPrice(autoId)
    val minPrice: LiveData<CompoundPrice?> = repository.getMinPrice(autoId)


    init {

        viewModelScope.launch {
            _petrolTotal.value = repository.getTotalPetrol(autoId)
            _costeTotal.value = repository.getTotalCost(autoId)
        }

    }


    val totalAverage = petrolTotal.map { petrol ->
        if ( totalKmsRecorridos.value != 0 ){
            val ponderado = petrol?.minus(lastLitros.value) ?: 0f
            redondeaDecimales((ponderado.times(100)).div(totalKmsRecorridos.value), 2)
        } else {
            0f
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val repository = AutosRepository(AutosDatabase.getDatabase(application.applicationContext), application)
                StatisticsViewModel(application, repository)
            }
        }
    }
}