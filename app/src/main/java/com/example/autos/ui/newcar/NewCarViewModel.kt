package com.example.autos.ui.newcar

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.autos.data.local.DbAuto
import com.example.autos.data.local.asDomainModel
import com.example.autos.domain.DomainCoche
import com.example.autos.repository.AutosRepository
import com.google.android.material.internal.ViewUtils.hideKeyboard
import kotlinx.coroutines.launch

private const val TAG = "xxNcvm"

class NewCarViewModel(private val repository: AutosRepository, private val app: Application) : AndroidViewModel(app) {


//    private val _showRestoreDialog = MutableLiveData<Boolean>()
//    val showRestoreDialog: LiveData<Boolean>
//        get() = _showRestoreDialog

    private val _navigateToActualAuto = MutableLiveData<DomainCoche>()
    val navigateToActualAuto: LiveData<DomainCoche>
        get() = _navigateToActualAuto

//    private val _firstStart = MutableLiveData<Boolean>()
//    val firstStart: LiveData<Boolean>
//        get() = _firstStart


    init {
//        if (repository.getActualAutoId() == -1){
//            _showRestoreDialog.value = true
//        }
    }

    fun saveAuto(auto: DbAuto){
        viewModelScope.launch {
            val newId = repository.insertAuto(auto).toInt()
            Log.d(TAG,"saving newCarId: $newId")
            repository.setActualAutoId(newId)
            _navigateToActualAuto.value = auto.asDomainModel()
        }
    }
}

@Suppress("UNCHECKED_CAST")
class NewCarViewModelFactory(
    private val repository: AutosRepository,
    private val app: Application
    ): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewCarViewModel(repository, app) as T
    }
}