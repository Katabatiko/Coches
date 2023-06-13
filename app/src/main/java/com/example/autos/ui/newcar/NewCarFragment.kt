package com.example.autos.ui.newcar

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.autos.MainActivity
import com.example.autos.R
import com.example.autos.data.local.AutosDatabase
import com.example.autos.data.local.DbAuto
import com.example.autos.databinding.FragmentNewCarBinding
import com.example.autos.repository.AutosRepository
import com.example.autos.util.flipDate
import com.example.autos.util.standardizeDate
import com.example.autos.util.validacion

private const val TAG = "xxNcf"

class NewCarFragment: Fragment() {

    private lateinit var viewModel: NewCarViewModel

    private lateinit var marca: EditText
    private lateinit var modelo: EditText
    private lateinit var year: EditText
    private lateinit var matricula: EditText
    private lateinit var initialKms: EditText
    private lateinit var buyDate: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<FragmentNewCarBinding>(inflater, R.layout.fragment_new_car, container,false)
        binding.lifecycleOwner = viewLifecycleOwner

        marca = binding.marcaInput
        marca.requestFocus()
        modelo = binding.modeloInput
        year = binding.yearInput
        matricula = binding.matriculaInput
        initialKms = binding.initialKmsInput
        buyDate = binding.buyDateInput

        val navController = this.findNavController()
        val saveCarButton = binding.saveCar
        saveCarButton.setOnClickListener{
//            Log.d(TAG,"click save car button")
            recogerDatos()
        }

        val app = requireNotNull(activity).application
        val db = AutosDatabase.getDatabase(app)
        val repository = AutosRepository(db, app)
        val viewModelFactory = NewCarViewModelFactory(repository, app)
        viewModel = ViewModelProvider(this, viewModelFactory)[NewCarViewModel::class.java]

        viewModel.navigateToActualAuto.observe(viewLifecycleOwner){
            if (it != null){
                navController.navigate(NewCarFragmentDirections.actionNavNewCarToNavVehicle())
            }
        }

//        viewModel.firstStart.observe(viewLifecycleOwner){
//            if (it) {
//                val view = requireActivity().currentFocus
//                Log.d(TAG,"currentFocus: ${view.toString()}")
//                if (view != null) {
////                    hideKeyboard(view)
//                    Log.d(TAG,"hidden keyboard")
//                }
//            }
//        }


//        viewModel.showRestoreDialog.observe(viewLifecycleOwner) {
//            if (it) {
//                showRestoreDialog()
////                setFragmentResult("actual_vehicle", bundleOf("vehicle_id" to it))
//            }
//        }

        modelo.requestFocus()

        return binding.root
    }

    private fun recogerDatos() {
        if (validacion( listOf(marca, modelo, year, matricula, initialKms, buyDate) )) {
            val newAuto = DbAuto(
                marca = marca.text.toString().trim(),
                modelo = modelo.text.toString().trim(),
                year = year.text.toString().trim(),
                matricula = matricula.text.toString().trim(),
                initKms = Integer.parseInt(initialKms.text.toString().trim()),
                lastKms = Integer.parseInt(initialKms.text.toString().trim()),
                buyDate = flipDate(standardizeDate(buyDate.text.toString().trim()))
            )
            viewModel.saveAuto(newAuto)
//            setFragmentResult("auto", bundleOf("newCarId" to true) )
        } else {
            Toast.makeText(this.context, R.string.error_faltan_datos, Toast.LENGTH_LONG).show()
        }
    }

    private fun showKeyboard(view: View){
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}