package com.example.autos.ui.refueling

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.example.autos.R
import com.example.autos.data.local.DbRefueling
import com.example.autos.databinding.FragmentRefuelingBinding
import com.example.autos.repository.AutosRepository
import com.example.autos.util.redondeaDecimales
import com.example.autos.util.validacion
import com.google.android.material.textfield.TextInputLayout

private const val TAG = "xxRf"

class RefuelingFragment : Fragment() {

    private var _binding: FragmentRefuelingBinding? = null

    private lateinit var datePicker: DatePicker
    private lateinit var kms: EditText
    private lateinit var litros: EditText
    private lateinit var lleno: CheckBox
    private lateinit var eurosLitro: EditText
    private lateinit var euros: EditText


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: RefuelingViewModel by activityViewModels { RefuelingViewModel.Factory }
    private lateinit var preferences: SharedPreferences

//    private lateinit var repository: AutosRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = requireContext().applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val actualAutoId = preferences.getInt("auto_id", -1)
        if (viewModel.carId != actualAutoId)
            viewModel.reloadData(actualAutoId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRefuelingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        datePicker = binding.datePicker
        kms = binding.editTextKms
        lleno = binding.lleno
        eurosLitro = binding.editTextEurosLitro
        litros = binding.editTextLitros
        euros = binding.editTextEuros
        euros.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!validacion( listOf(eurosLitro, euros) )){
                   Toast.makeText(this.context, R.string.error_faltan_datos, Toast.LENGTH_LONG).show()
                } else {
                    val precioLitro = eurosLitro.text.toString().toFloat()
                    val total = euros.text.toString().toFloat()
                    litros.setText(calcularLitros(precioLitro, total).toString())
                }
            }
        }


//        Log.d(TAG,"cardId: ${vehicleId}")

        viewModel.lastRefueling.observe(viewLifecycleOwner){
//            Log.d(TAG,"lastRefuel: $it")
            (kms.parent.parent as TextInputLayout).hint = (viewModel.lastRefueling.value?.kms ?: 100000).toString()
            (eurosLitro.parent.parent as TextInputLayout).hint = (viewModel.lastRefueling.value?.eurosLitro ?: 1.234).toString()
            (litros.parent.parent as TextInputLayout).hint = (viewModel.lastRefueling.value?.litros ?: 44.32).toString()
            (euros.parent.parent as TextInputLayout).hint = (viewModel.lastRefueling.value?.euros ?: 50.67).toString()
        }

        val navController = this.findNavController()
        val addRefuelFab = binding.saveRefuel
        addRefuelFab.setOnClickListener{
            if (saveData())
                navController.navigate(RefuelingFragmentDirections.actionNavRefuelingToNavRepostajes())
        }



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun calcularLitros( precioLitro: Float, euros: Float ): Float{
        return redondeaDecimales((euros / precioLitro), 2)
    }

    private fun cleanTexts(){
        kms.text.clear()
        litros.text.clear()
        eurosLitro.text.clear()
        euros.text.clear()
    }

    private fun saveData(): Boolean {
        var ok = true
        if (validacion( listOf(kms, litros, eurosLitro, euros) )) {
            val fechaTemplate = "%s/%s/%s"
            val mes = datePicker.month +1
            var formatedMes = mes.toString()
            if (mes < 10)   formatedMes = "0$mes"
            val dia = datePicker.dayOfMonth
            var formatedDia = dia.toString()
            if (dia < 10)   formatedDia = "0$dia"


            val refueling = DbRefueling(
//                cocheId = repository.getActualAutoId(),
                cocheId = viewModel.carId,
                fecha = fechaTemplate.format(
                    datePicker.year,
                    formatedMes,
                    formatedDia
                ),
                kms = Integer.parseInt(kms.text.toString()),
                litros = litros.text.toString().toFloat(),
                eurosLitro = eurosLitro.text.toString().toFloat(),
                euros = euros.text.toString().toFloat(),
                lleno = lleno.isChecked
            )
            viewModel.saveRefueling(refueling)
            cleanTexts()
            Log.d(TAG, "repostaje: $refueling")
        } else {
            ok = false
            Toast.makeText(this.context, R.string.error_faltan_datos, Toast.LENGTH_LONG).show()
        }
        return ok
    }
}
