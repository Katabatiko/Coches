package com.example.autos.ui.vehiculos

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.asListDomainAutoModel
import com.example.autos.domain.DomainCoche
import com.example.autos.repository.AutosRepository

private const val TAG = "xxVvm"
class VehiculosViewModel(private val repository: AutosRepository) : ViewModel() {

    val vehicles: LiveData<List<DomainCoche>> = repository.getAllAutos().asListDomainAutoModel()


    fun setAutoId(autoId: Int) {
        repository.setActualAutoId(autoId)
    }


    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val repository = AutosRepository(AutosDatabase.getDatabase(application.applicationContext), application)
                VehiculosViewModel(repository)
            }
        }
    }

}