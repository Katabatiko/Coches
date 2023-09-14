package com.example.autos.ui.maintenance

import android.app.Application
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import com.example.autos.data.local.asGastoDomainModel
import com.example.autos.data.local.asItemsListDomainModel
import com.example.autos.domain.DomainGasto
import com.example.autos.domain.DomainItem
import com.example.autos.domain.asDatabaseModel
import com.example.autos.repository.AutosRepository
import com.example.autos.util.flipDate
import com.example.autos.util.redondeaDecimales
import com.example.autos.util.standardizeDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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
    val detalle = MutableLiveData("")
    val precio = MutableLiveData("")
    val newItemList = MutableLiveData<List<DomainItem>>(listOf())
    val masIva = MutableLiveData(false)
    private val iva = DomainItem(
        itemId = 0,
        gastoId = gastoId,
        descripcion = "iva $IVA%",
        cantidad = 1,
        precio = 0f,
        detalle = ""
    )

    private val gastosList = MutableLiveData<List<DomainGasto>>(listOf())
    val search = MutableLiveData<List<DomainGasto>>(listOf())
    val lastKms: MutableState<Int> = mutableStateOf(0)

    init {
        gastoDate.value = standardizeDate(getTodayDate())
        viewModelScope.launch {
            lastKms.value  = repository.getActualKms(autoId)
        }
    }

    private fun getTodayDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) +1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$day/$month/$year"
    }

    fun cleanGastoInputs() {
        gastoDate.value = standardizeDate(getTodayDate())
        concepto.value = ""
        actualKms.value = ""
        gastoId = 0
        newItemList.value = listOf()
    }

    fun cleanItemInput() {
        cantidad.value = "1"
        descripcion.value = ""
        detalle.value = ""
        precio.value = ""
        totalGasto.value = 0f
    }

    fun addItem(){
        val item = DomainItem(
            itemId = 0,
            gastoId = gastoId,
            descripcion = descripcion.value!!,
            detalle = detalle.value,
            precio = precio.value!!.replace(",",".").toFloat(),
            cantidad = cantidad.value!!.toInt()
        )
        newItemList.value = newItemList.value!!.plus(item)
    }

    fun removeItem(item: DomainItem) {
        newItemList.value = newItemList.value!!.minus(item)
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
        newItemList.value = newItemList.value!!.plus(iva)
    }

    fun removeIva() {
        if (newItemList.value!!.contains(iva))
            newItemList.value = newItemList.value!!.minus(iva)
    }

    fun makeGasto() {
        gasto = DomainGasto(
            fecha = flipDate(gastoDate.value!!),
            concepto = concepto.value!!,
            autoId = autoId,
            kms = actualKms.value!!.toInt(),
            importe = subtotal(),
            items = newItemList.value!!
        )
    }

    fun saveGastoAsync(): Deferred<Boolean> {
        status.value = AutosStatus.LOADING

        val deferredInsert = viewModelScope.async {
            try {
                insertGasto()
            } catch (e: Exception){
                status.value = AutosStatus.ERROR
                Log.e(TAG,"error de insercion: $e")
                return@async false
            }

            status.value = AutosStatus.DONE
            return@async true
        }
        return deferredInsert
    }

    private suspend fun insertGasto() {
        val gastoId = repository.insertGasto(gasto!!.asDatabaseModel(), gasto!!.kms > lastKms.value)
        gasto!!.items.forEach{ item ->
            item.gastoId = gastoId.toInt()
            repository.insertItem(item.asDatabaseModel())
        }
    }

    fun getGastosByAuto(autoId: Int) {
        status.value = AutosStatus.LOADING
        viewModelScope.launch {
            try {
                gastosList.value = getWholeGastosByAuto(autoId) ?: listOf()
            } catch (ce: CancellationException){
                throw ce
            } catch (e: Exception){
                Log.e(TAG,"error: $e")
                status.value = AutosStatus.ERROR
            }
            status.value = AutosStatus.DONE
        }
    }

    private suspend fun getWholeGastosByAuto(autoId: Int): List<DomainGasto> {
        var listDomainGasto: List<DomainGasto> = listOf()
        val gastosDb = repository.getGastosByAuto(autoId)

        gastosDb?.forEach { dbGasto ->
            val gastoDomain = dbGasto.asGastoDomainModel()
            gastoDomain.items = repository.getItemsFromGasto(dbGasto.gastoId).asItemsListDomainModel()
            listDomainGasto = listDomainGasto.plus(gastoDomain)
        }
        search.value = listDomainGasto
        return listDomainGasto
    }

    fun getSearch(wanted: String){
        search.value = gastosList.value!!.filter {
                it.contains(wanted.split(" "))
            }
    }

    /**
     * Extension function para comprobar si un gasto o sus articulos
     * contienen todas la palabras de busqueda suministradas
     * @param wanted lista de palabras a comprobar
     */
    private fun DomainGasto.contains(wanted: List<String>): Boolean {
        var matches = true
        wanted.forEach {
            matches = matches && this.concepto.contains(it, true)
        }
        if (matches)    return true
        else {
            this.items.forEach{ item ->
                matches = true
                wanted.forEach {
                    matches = matches && item.descripcion.contains(it, true)
                }
                if (matches)    return true
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