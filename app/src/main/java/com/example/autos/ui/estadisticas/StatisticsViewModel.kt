package com.example.autos.ui.estadisticas

import android.app.Application
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
import com.example.autos.autoId
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.CompoundPrice
import com.example.autos.data.local.DateRange
import com.example.autos.data.local.asGastoListDomainModel
import com.example.autos.data.local.asRefuelingDomainModel
import com.example.autos.domain.AverageRefueling
import com.example.autos.domain.DomainGasto
import com.example.autos.domain.DomainRefueling
import com.example.autos.domain.DatoByYear
import com.example.autos.domain.PricesByYear
import com.example.autos.repository.AutosRepository
import com.example.autos.util.redondeaDecimales
import kotlinx.coroutines.launch

private const val TAG = "xxSvm"

class StatisticsViewModel(
    val app: Application,
    private val repository: AutosRepository
    ) : AndroidViewModel(app) {

    private var yearsRange: DateRange? = null

    var totalKmsRecorridos: MutableState<Int> = mutableStateOf(0)
    var lastLitros: MutableState<Float> = mutableStateOf(0f)

    private val _costeTotalPetrol: MutableState<Float?> = mutableStateOf(0f)
    val costeTotalPetrol: MutableState<Float?>
        get() = _costeTotalPetrol

    private val _petrolTotal = MutableLiveData<Float?>()
    val petrolTotal: LiveData<Float?>
        get() = _petrolTotal

    val maxPrice: LiveData<CompoundPrice> = repository.getTotalMaxPrice(autoId)
    val minPrice: LiveData<CompoundPrice> = repository.getTotalMinPrice(autoId)

    val totalGastos: MutableState<Float?> = mutableStateOf(0f)

    private val fullRefuels: MutableState<List<DomainRefueling>> = mutableStateOf(listOf())
    val listAverage: MutableState<List<AverageRefueling>> = mutableStateOf(listOf())

    val frontTiresChanges: MutableState<List<DomainGasto>> = mutableStateOf(listOf())
    val backTiresChanges: MutableState<List<DomainGasto>> = mutableStateOf(listOf())

    val kmsByYear: MutableState<List<DatoByYear>> = mutableStateOf(listOf())

    val pricesByYear: MutableState<List<PricesByYear>> = mutableStateOf(listOf())

    val gastosByYear: MutableState<List<DatoByYear>> = mutableStateOf(listOf())

    init {
        viewModelScope.launch {
            getDateRange()
            _petrolTotal.value = repository.getTotalPetrol(autoId)
            _costeTotalPetrol.value = repository.getTotalCost(autoId)
            totalGastos.value  = repository.getTotalGastos(autoId)
            fullRefuels.value = repository.getLastFullRefuelingAndAmongThem(autoId).asRefuelingDomainModel()
            getKmsByYear()
            getLastAverages()
            getPricesByYear()
            getGastosByYear()
            getCambiosRuedas()
        }

    }

    /**
     * Función para la extracción de los años durante los cuales el auto
     * ha tenido registros. Se registran en un objeto DateRange, pero sólo
     * recoge el año (se ignora el mes y el día de mes) en formato String
     */
    private suspend fun getDateRange() {
        val dateRange = repository.getDateRange(autoId)
        val oldest = dateRange?.oldest?.split("/")?.get(0) ?: "0"
        val latest = dateRange?.latest?.split("/")?.get(0) ?: "0"
        yearsRange = DateRange(latest, oldest)
//        Log.d(TAG,"dateRange: $dateRange")
    }

    val totalAverage = petrolTotal.map { petrol ->
        if ( totalKmsRecorridos.value != 0 ){
            // el ultimo repostaje no se ha consumido por lo que se descuenta
            val ponderado = petrol?.minus(lastLitros.value) ?: 0f
            redondeaDecimales((ponderado.times(100)).div(totalKmsRecorridos.value), 2)
        } else {
            0f
        }
    }

    private fun getLastAverages() {
        val count = fullRefuels.value.size
        var initKms: Int
        var endKms: Int
        var litros = 0f
        var auxIndex: Int
//        Log.d(TAG,"fullRefuels count: $count")

        fullRefuels.value.forEachIndexed { index, actual ->
            // al estar ordenados por kms descendientes, se itera de mas a menos reciente
            val averageRefueling: AverageRefueling?
            if (index < (count -1)) {
                val next = fullRefuels.value[index + 1]

                if (next.lleno && actual.lleno) {
                    val recorrido = actual.recorrido
                    averageRefueling = AverageRefueling(
                        initFecha = next.fecha,
                        endFecha = actual.fecha,
                        kms = recorrido,
                        consumo = redondeaDecimales((actual.litros * 100) / recorrido, 2)
                    )
                    listAverage.value = listAverage.value.plus(averageRefueling)
                } else {
                    if (actual.lleno){
                        // si actual es full, por el else, next es no full
                        var nextNoFull: Boolean
                        // se añade los litros de actual
                        litros += actual.litros
                        endKms = actual.kms
                        auxIndex = index + 1
                        // se realiza un bucle con los repostajes no llenos entre repostajes llenos
                        do {
                            litros += fullRefuels.value[auxIndex].litros
                            nextNoFull = !fullRefuels.value[++auxIndex].lleno
                        } while (nextNoFull)

                        // al salir estamos en la posicion del primer lleno encontrado
                        // el primer full, marca la fecha y los kms iniciales
                        initKms = fullRefuels.value[auxIndex].kms
                        val recorrido = endKms - initKms

                        averageRefueling = AverageRefueling(
                            initFecha = fullRefuels.value[auxIndex].fecha,
                            endFecha = actual.fecha,
                            kms = recorrido,
                            consumo = redondeaDecimales((litros * 100) / recorrido, 2)
                        )
                        listAverage.value = listAverage.value.plus(averageRefueling)
                        litros = 0f
                    }
                }
            }
        }
    }

    private suspend fun getKmsByYear() {
        kmsByYear.value = repository.getKmsByYear(autoId, yearsRange)
    }

    private suspend fun getPricesByYear() {
        var priceList: List<PricesByYear> = listOf()
        val oldest = yearsRange?.oldest?.toInt()
        val latest = yearsRange?.latest?.toInt()

        if (latest != null && oldest != null) {
            for (year in latest downTo oldest){
                val compoundMin = repository.getMinPriceByYear(autoId, "${year}%")
                val compoundMax = repository.getlMaxPriceByYear(autoId, "${year}%")
                // por si hay años intermedios sin registros
                if (compoundMax.fecha != null && compoundMin.fecha != null) {
                    priceList = priceList.plus(
                        PricesByYear(
                            year = year.toString(),
                            min = compoundMin,
                            max = compoundMax
                        )
                    )
                }
            }
        }
//            Log.d(TAG,"pricesList: $priceList")
        pricesByYear.value = priceList
    }

    private suspend fun getGastosByYear() {
        gastosByYear.value = repository.getGastoByYear(autoId, yearsRange)
    }

    private suspend fun getCambiosRuedas() {
        frontTiresChanges.value = repository.getCambiosRueda(autoId, true).asGastoListDomainModel()
        backTiresChanges.value = repository.getCambiosRueda(autoId, false).asGastoListDomainModel()
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