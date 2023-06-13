package com.example.autos.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import com.example.autos.data.local.DbRefueling
import com.example.autos.data.local.asDomainModel
import com.example.autos.domain.DomainCoche
import com.example.autos.repository.AutosRepository

private const val TAG = "xxHvm"

class HomeViewModel(repository: AutosRepository, application: Application) : AndroidViewModel(application) {


    val autoId = repository.getActualAutoId()

    val car: LiveData<DomainCoche> = repository.getAuto(autoId).asDomainModel()

    private val _navigateToNewCar = MutableLiveData<Boolean>()
    val navigateToNewCar: LiveData<Boolean>
        get() = _navigateToNewCar

    private val _navigateToRefueling = MutableLiveData<Boolean>()
    val navigateToRefueling: LiveData<Boolean>
        get() = _navigateToRefueling

    /*val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    val CAR_ID = intPreferencesKey("car_id")

    val carIdFlow: Flow<Int> = MainActivity().application.dataStore.data
        .map{
            it[CAR_ID] ?: -1
        }
*/

    init {
        Log.d(TAG,"Coche id: ${autoId}")
        if (autoId == -1) {
            _navigateToNewCar.value = true
        }
    }


    fun navigateToNewAuto(){
        _navigateToNewCar.value = true
    }
    fun navigatedToNewAuto(){
        _navigateToNewCar.value = false
    }

    fun navigateToNewRefueling(){
        _navigateToRefueling.value = true
    }
    fun navigatedToRefueling() {
        _navigateToRefueling.value = false
    }
}

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val repository: AutosRepository,
    private val application: Application
    ) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository, application) as T
    }
}