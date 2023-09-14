package com.example.autos.ui.maintenance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.autos.NumberType
import com.example.autos.R
import com.example.autos.ui.composables.DateInput
import com.example.autos.ui.composables.DatoNumericoInput
import com.example.autos.ui.composables.StringInput
import com.example.autos.util.numeroValido
import com.example.autos.util.textEmpty

private const val TAG = "xxNgs"

@Composable
fun NewGastoScreen(
    modifier: Modifier = Modifier,
    viewModel: GastosViewModel,
    lastKms: State<Int>,
    onNewGasto: () -> Unit
){
    val focusManager = LocalFocusManager.current

    val date = viewModel.gastoDate.observeAsState()
    val concepto = viewModel.concepto.observeAsState()
    val kms = viewModel.actualKms.observeAsState()
    
    val suggestions = listOf(
        stringResource(id = R.string.suggestion_aceite),
        stringResource(id = R.string.suggestion_aire),
        stringResource(id = R.string.suggestion_revision),
        stringResource(id = R.string.suggestion_ruedas_del),
        stringResource(id = R.string.suggestion_ruedas_tras)
    )
    val dropDownMenuExpanded = rememberSaveable { mutableStateOf(false) }

    fun areAllInputs(): Boolean {
        if (!numeroValido(kms.value ?: "", NumberType.INT))                return false
        if (textEmpty(concepto.value ?: ""))                            return false
        if (textEmpty(date.value ?: ""))                                return false
        return true
    }

    Column(
        modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        StringInput(
            label = stringResource(id = R.string.concepto),
            modifier = Modifier.onFocusChanged { 
                if (it.isFocused) {
                    dropDownMenuExpanded.value = true
                }
            },
            focusManager = focusManager,
            focusRequest = true,
            stringInput = concepto.value ?: "",
            onDataChange = { viewModel.concepto.postValue(it) }
        )
        DropdownMenu(
            expanded = dropDownMenuExpanded.value,
            onDismissRequest = { dropDownMenuExpanded.value = false },
            properties = PopupProperties(
                focusable = false    // para que aparezca el teclado a la vez que el popup
            )
        ) {
            suggestions.forEach { label ->
                DropdownMenuItem(
                    text = { Text(text = label) },
                    onClick = {
                        dropDownMenuExpanded.value = false
                        viewModel.concepto.postValue(label)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        DatoNumericoInput(
            label = stringResource(id = R.string.kms),
            modifier= Modifier.padding(horizontal = 8.dp),
            numberType = NumberType.INT,
            focusManager = focusManager,
            value = kms.value ?: "",
            lastValue = lastKms.value.toString(),
            lastInput = true,
            auxFunc = null,
            onDataChange = {
                viewModel.actualKms.postValue(it)
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        DateInput(
            label = stringResource(id = R.string.fecha),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            focusManager = focusManager,
            dateInput = date.value!!,
            onDataChange = { viewModel.gastoDate.postValue(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                onNewGasto()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = areAllInputs()
        ) {
            Text(stringResource(id = R.string.next))
        }

    }
}