package com.example.autos.ui.vehiculos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.autos.data.local.asListDomainAutoModel
import com.example.autos.domain.DomainCoche
import com.example.autos.repository.AutosRepository

class VehiculosViewModel(private val repository: AutosRepository, application: Application) : AndroidViewModel(application) {

//    private val _vehicles = MutableLiveData<List<DbAuto>>()
    val vehicles: LiveData<List<DomainCoche>> = repository.getAllAutos().asListDomainAutoModel()

}

class VehiculosViewModelFactory(
    private val repository: AutosRepository,
    private val application: Application
    ): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VehiculosViewModel(repository, application) as T
    }
}