package com.example.autos.ui.refueling

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.autos.AutosStatus
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.DbRefueling
import com.example.autos.data.local.asListDomainModel
import com.example.autos.domain.DomainRefueling
import com.example.autos.repository.AutosRepository
import kotlinx.coroutines.launch

private const val TAG = "xxRvm"

class RefuelingViewModel(
    private val repository: AutosRepository,
    application: Application
) : AndroidViewModel(application) {

    var carId = repository.getActualAutoId()

    var lastRefueling: LiveData<DbRefueling?> = repository.getLastRefueling(carId)

    var repostajes: LiveData<List<DomainRefueling>> = repository.getRepostajes(carId).asListDomainModel()

    private val _status = MutableLiveData<AutosStatus>()
    val status: LiveData<AutosStatus>
        get() = _status

    init {
        _status.value = AutosStatus.LOADING
    }

    fun resetStatus() {
        _status.value = AutosStatus.DONE
    }

    fun reloadData(autoId: Int){
        carId = autoId
        lastRefueling = repository.getLastRefueling(carId)
        repostajes = repository.getRepostajes(carId).asListDomainModel()
    }


    fun saveRefueling(refueling: DbRefueling) {
//            Log.d(TAG,"refueling lastKms: ${refueling.kms}")
        viewModelScope.launch {
            repository.insertRefueling(refueling)
        }
        updateAutoKms(refueling.kms)
    }

    fun updateAutoKms(autoKms: Int){
        viewModelScope.launch {
            repository.updateAutoKms(carId, autoKms)
        }
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val repository = AutosRepository(AutosDatabase.getDatabase(application.applicationContext), application)
                RefuelingViewModel(repository, application)
            }
        }
    }
}