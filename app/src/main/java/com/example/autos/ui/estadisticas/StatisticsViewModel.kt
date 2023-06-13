package com.example.autos.ui.estadisticas

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.autos.data.local.CompoundPrice
import com.example.autos.data.local.DbRefueling
import com.example.autos.data.local.asDomainModel
import com.example.autos.domain.DomainCoche
import com.example.autos.repository.AutosRepository
import com.example.autos.util.redondeaDecimales
import kotlinx.coroutines.launch

private const val TAG = "xxSvm"

class StatisticsViewModel(
    val app: Application,
    val repository: AutosRepository
    ) : AndroidViewModel(app) {

    private val autoId = repository.getActualAutoId()

    val car: LiveData<DomainCoche> = repository.getAuto(autoId).map { it.asDomainModel() }
    val lastRefueling: LiveData<DbRefueling?> = repository.getLastRefueling(autoId)

    private val _costeTotal = MutableLiveData<Float>()
    val costeTotal: LiveData<Float>
        get() = _costeTotal

    private val _petrolTotal = MutableLiveData<Float>()
    val petrolTotal: LiveData<Float>
        get() = _petrolTotal

    val maxPrice: LiveData<CompoundPrice> = repository.getMaxPrice(autoId)
    val minPrice: LiveData<CompoundPrice> = repository.getMinPrice(autoId)


    init {
        getTotalCost()
        getTotalPetrol()
    }

    private fun getTotalPetrol(){
        viewModelScope.launch {
            _petrolTotal.value = repository.getTotalPetrol(autoId)
            Log.d(TAG,"coste total: ${costeTotal.value}")
        }
    }

    private fun getTotalCost() {
        viewModelScope.launch {
            _costeTotal.value = repository.getTotalCost(autoId)
            Log.d(TAG,"consumo total: ${petrolTotal.value}")
        }
    }

//    private fun getMaxAndMinPrice() {
//        viewModelScope.launch {
//            _maxPrice.value = repository.getMaxPrice(autoId).value
//            _minPrice.value = repository.getMinPrice(autoId).value
//        }
//    }

    val totalKmsRecorridos = car.map {
        (car.value?.actualKms ?: 0) - (car.value?.initKms ?: 0)
    }

    val totalAverage = _petrolTotal.map { petrol ->
        car.map { car ->
            val totalRecorridos = car.actualKms - car.initKms
            totalKmsRecorridos.map { kms ->
                // hay que restar el último abastecimiento porque no se ha consumido todavía
                lastRefueling.map {
                    return@map if (it != null && totalRecorridos != 0){
                        val ponderado = petrol - it.litros
                        redondeaDecimales((ponderado * 100).div(kms), 2)
                    }else {
                        0f
                    }
                }
            }
        }
    }

}

class StatisticsViewModelFactory(
    val app: Application,
    val repository: AutosRepository
): ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StatisticsViewModel(app, repository) as T
    }
}