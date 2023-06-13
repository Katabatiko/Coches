package com.example.autos.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.autos.R
import com.example.autos.data.local.AutosDatabase
import com.example.autos.databinding.FragmentHomeBinding

import com.example.autos.repository.AutosRepository

private const val TAG = "xxHf"

private lateinit var viewModel: HomeViewModel

class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val app = requireNotNull(activity).application
        val db = AutosDatabase.getDatabase(app)
        val repository = AutosRepository(db, app)
        val viewModelFactory = HomeViewModelFactory(repository, app)
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        val binding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        val navController = this.findNavController()
        val newRefuelFab = binding.addRefueling
        newRefuelFab.setOnClickListener {
            viewModel.navigateToNewRefueling()
        }

        viewModel.navigateToRefueling.observe(viewLifecycleOwner){
            if (it){
                navController.navigate(
                    HomeFragmentDirections.actionNavVehicleToNavRefueling())
                viewModel.navigatedToRefueling()
            }
        }

        viewModel.navigateToNewCar.observe(viewLifecycleOwner){
            if (it){
                navController.navigate(HomeFragmentDirections.actionNavVehicleToNavNewCar())
                viewModel.navigatedToNewAuto()
//                Log.d(TAG,"navigateToNewCar: ${viewModel.car.value}")
            }
        }

        viewModel.car.observe(viewLifecycleOwner) {
            val stringClass = it.javaClass.simpleName
            Log.d(TAG,"observing car: $stringClass")
        }

        return binding.root
    }

    companion object{
        fun getInitialKms(): Int {
            return viewModel.car.value!!.initKms
        }
    }

}