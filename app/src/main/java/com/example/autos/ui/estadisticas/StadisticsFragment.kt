package com.example.autos.ui.estadisticas

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.autos.R
import com.example.autos.data.local.AutosDatabase
import com.example.autos.databinding.FragmentStatisticsBinding
import com.example.autos.repository.AutosRepository

private const val TAG = "xxSf"

class StadisticsFragment : Fragment() {

    /*companion object {
        fun newInstance() = StadisticsFragment()
    }*/

    private lateinit var viewModel: StatisticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentStatisticsBinding>(inflater, R.layout.fragment_statistics, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val app = requireActivity().application
        val db = AutosDatabase.getDatabase(app)
        val repository = AutosRepository(db, app)
        val viewModelFactory = StatisticsViewModelFactory(app, repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[StatisticsViewModel::class.java]

        binding.viewModel = viewModel

        viewModel.maxPrice.observe(viewLifecycleOwner){
            Log.d(TAG,"maxPrice: $it")
        }
        viewModel.minPrice.observe(viewLifecycleOwner){
            Log.d(TAG,"minPrice: $it")
        }

        return binding.root
    }

}