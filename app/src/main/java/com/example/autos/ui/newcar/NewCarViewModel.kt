package com.example.autos.ui.newcar

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.DbAuto
import com.example.autos.domain.DomainCoche
import com.example.autos.repository.AutosRepository
import com.example.autos.util.flipDate
import kotlinx.coroutines.launch

private const val TAG = "xxNcvm"

class NewCarViewModel(private val repository: AutosRepository, private val app: Application) : AndroidViewModel(app) {


    val modelo = MutableLiveData("")
    val marca = MutableLiveData("")
    val matricula = MutableLiveData("")
    val fechaMatriculacion = MutableLiveData("")
    val fechaCompra = MutableLiveData("")
    val initKms = MutableLiveData("")


    fun saveAuto(){
        val auto = DbAuto(
            modelo = modelo.value!!,
            marca = marca.value!!,
            matricula = matricula.value!!,
            year = fechaMatriculacion.value!!,
            buyDate = flipDate(fechaCompra.value!!),
            initKms = (initKms.value!!).toInt(),
            lastKms = (initKms.value!!).toInt()
        )
//        Log.d(TAG,"new auto: $auto")
        viewModelScope.launch {
            val newId = repository.insertAuto(auto).toInt()
            Log.d(TAG,"saving newCarId: $newId")
            repository.setActualAutoId(newId)
        }
    }

    companion object{
        val Factory : ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val repository = AutosRepository(AutosDatabase.getDatabase(application.applicationContext), application)
                NewCarViewModel(repository, application)
            }
        }
    }
}
