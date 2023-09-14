package com.example.autos.ui.newcar

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.DbAuto
import com.example.autos.repository.AutosRepository
import com.example.autos.util.flipDate
import kotlinx.coroutines.launch

private const val TAG = "xxNcvm"

class NewCarViewModel(private val repository: AutosRepository) : ViewModel() {

    val modelo: MutableState<String> = mutableStateOf("")
    val marca: MutableState<String> = mutableStateOf("")
    val matricula: MutableState<String> = mutableStateOf("")
    val fechaMatriculacion: MutableState<String> = mutableStateOf("")
    val fechaCompra: MutableState<String> = mutableStateOf("")
    val initKms: MutableState<String> = mutableStateOf("")


    fun saveAuto(){
        val auto = DbAuto(
            modelo = modelo.value,
            marca = marca.value,
            matricula = matricula.value.uppercase(),
            year = fechaMatriculacion.value,
            buyDate = flipDate(fechaCompra.value),
            initKms = (initKms.value).toInt(),
            lastKms = (initKms.value).toInt()
        )

        viewModelScope.launch {
            val newId = repository.insertAuto(auto).toInt()
            repository.setActualAutoId(newId)
        }
    }

    companion object{
        val Factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val repository = AutosRepository(AutosDatabase.getDatabase(application.applicationContext), application)
                NewCarViewModel(repository)
            }
        }
    }
}
