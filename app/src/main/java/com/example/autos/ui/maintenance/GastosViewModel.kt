package com.example.autos.ui.maintenance

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.autos.AutosStatus
import com.example.autos.IVA
import com.example.autos.autoId
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.asGastoListDomainModel
import com.example.autos.data.local.asItemsListDomainModel
import com.example.autos.domain.DomainGasto
import com.example.autos.domain.DomainItem
import com.example.autos.domain.asDatabaseModel
import com.example.autos.repository.AutosRepository
import com.example.autos.util.flipDate
import com.example.autos.util.redondeaDecimales
import com.example.autos.util.standardizeDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.Calendar

private const val TAG = "xxGvm"
class GastosViewModel(
//    savedStateHandle: SavedStateHandle,
    private val repository: AutosRepository
): ViewModel() {
    val status: MutableState<AutosStatus> = mutableStateOf(AutosStatus.DONE)

    var gastoId = 0
    val gastoDate = MutableLiveData("")
    val concepto = MutableLiveData("")
    val actualKms = MutableLiveData("")
    val totalGasto = MutableLiveData(0f)

    var gasto: DomainGasto? = null

    val cantidad = MutableLiveData("1")
    val descripcion = MutableLiveData("")
    val marca = MutableLiveData("")
    val precio = MutableLiveData("")
    val newItemList = MutableLiveData<List<DomainItem>>(listOf())
    val masIva = MutableLiveData(false)
    private val iva = DomainItem(
        itemId = 0,
        gastoId = gastoId,
        descripcion = "iva $IVA%",
        cantidad = 1,
        precio = 0f,
        marca = ""
    )

    private val gastosList = MutableLiveData<List<DomainGasto>>(listOf())
    val search = MutableLiveData<List<DomainGasto>>(listOf())
    val lastKms: LiveData<Int> = repository.getActualKms(autoId)

    init {
//        Log.d(TAG,"inicializando")
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) +1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        gastoDate.value = standardizeDate("$day/$month/$year")
    }

    private fun cleanGastoInputs() {
        gastoDate.value = ""
        concepto.value = ""
        actualKms.value = ""
        gastoId = 0
        newItemList.value = listOf()
    }

    fun cleanItemInput() {
        cantidad.value = "1"
        descripcion.value = ""
        marca.value = ""
        precio.value = ""
    }

    fun addItem(){
        val item = DomainItem(
            itemId = 0,
            gastoId = gastoId,
            descripcion = descripcion.value!!,
            marca = marca.value,
            precio = precio.value!!.replace(",",".").toFloat(),
            cantidad = cantidad.value!!.toInt()
        )
        newItemList.value = newItemList.value!!.plus(item)
    }

    fun removeItem(item: DomainItem) {
        newItemList.value = newItemList.value!!.minus(item)
        if (item == iva){
            Log.d(TAG,"eliminando iva")
        }
    }

    fun subtotal(): Float{
        var subtotal = 0f
        newItemList.value?.forEach {
            subtotal += redondeaDecimales(it.precio * it.cantidad,2)
        }
        return subtotal
    }

    fun addIva() {
        iva.precio = redondeaDecimales(subtotal() * IVA /100, 2)
//        if (!newItemList.value!!.contains(iva))
            newItemList.value = newItemList.value!!.plus(iva)
    }

    fun removeIva() {
        if (newItemList.value!!.contains(iva))
            newItemList.value = newItemList.value!!.minus(iva)
    }

    fun makeGasto(): DomainGasto {
        gasto = DomainGasto(
            fecha = flipDate(gastoDate.value!!),
            concepto = concepto.value!!,
            autoId = autoId,
            kms = actualKms.value!!.toInt(),
            importe = subtotal(),
            items = newItemList.value!!
        )
        return gasto as DomainGasto
    }

    fun saveGasto(): String {
        var result = "inicial"
        var error = false
        status.value = AutosStatus.LOADING
        viewModelScope.launch {
            try {
                insertGasto()
            } catch (e: Exception){
                error = true
                status.value = AutosStatus.ERROR
                Log.e(TAG,"error de insercion: $e")
                result = "Error de inserción, pruebe de nuevo \n$e"
            }

            cleanGastoInputs()
            status.value = AutosStatus.DONE
        }
        if (!error) {
//            status.value = AutosStatus.DONE
            result = "Gasto guardado"
        }
        return result
    }

    suspend fun insertGasto() {
        val gastoId = repository.insertGasto(gasto!!.asDatabaseModel()).toInt()
        gasto!!.items.forEach{ item ->
            item.gastoId = gastoId
            repository.insertItem(item.asDatabaseModel())
        }
    }

    fun getGastosByAuto(autoId: Int) {
        status.value = AutosStatus.LOADING
        viewModelScope.launch {
            try {
                gastosList.value = getWholeGastosByAuto(autoId) ?: listOf()
                Log.d(TAG,"recibidos gastos")
            } catch (ce: CancellationException){
                throw ce
            } catch (e: Exception){
                Log.e(TAG,"error: $e")
                status.value = AutosStatus.ERROR
            }
            status.value = AutosStatus.DONE
        }
//            Log.d(TAG,"gettingGastosByAuto: ${status.value}")
    }

    suspend fun getWholeGastosByAuto(autoId: Int): List<DomainGasto> {
        var listDomainGasto: List<DomainGasto> = listOf()
        val gastosDb = repository.getGastosByAuto(autoId)

        gastosDb?.forEach { dbGasto ->
            val gastoDomain = dbGasto.asGastoListDomainModel()
            gastoDomain.items = repository.getItemsFromGasto(dbGasto.gastoId).asItemsListDomainModel()
            listDomainGasto = listDomainGasto.plus(gastoDomain)
        }
        search.value = listDomainGasto
        return listDomainGasto
    }

    fun getSearch(wanted: String){
        search.value = gastosList.value!!.filter {
                it.conceptoContains(wanted) || it.itemsContains(wanted)
            }
//        Log.d(TAG,"encontrados: ${search.value?.size ?: -1} -> ${search.value} ")
    }

    private fun DomainGasto.conceptoContains(wanted: String): Boolean {
        val words = wanted.split(" ")
        when(words.size) {
            1 -> if (this.concepto.contains(wanted, true))
                return true

            2 -> {
                if (this.concepto.contains(words[0], true)
                        && this.concepto.contains(words[1], true))
                    return true
            }

            else -> {
                if (this.concepto.contains(words[0], true)
                        && this.concepto.contains(words[1], true)
                        && this.concepto.contains(words[2], true))
                    return true
            }

        }
        return false
    }

    private fun DomainGasto.itemsContains(wanted: String): Boolean {
        val words = wanted.split(" ")
        when(words.size) {
            1 -> this.items.forEach {
                    if (it.descripcion.contains(wanted, true)) return true
                }

            2 -> this.items.forEach {
                if (
                    it.descripcion.contains(words[0], true)
                    && it.descripcion.contains(words[1])
                    ) return true
            }

            else -> this.items.forEach {
                if (
                    it.descripcion.contains(words[0], true)
                    && it.descripcion.contains(words[1])
                    && it.descripcion.contains(words[2])
                ) return true
            }
        }

        return false
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val repository = AutosRepository(AutosDatabase.getDatabase(application.applicationContext), application)
                GastosViewModel(repository)
            }
        }
    }
}