package com.example.autos.ui.refueling

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.autos.NumberType
import com.example.autos.R
import com.example.autos.domain.DomainRefueling
import com.example.autos.ui.composables.DatePickerView
import com.example.autos.ui.composables.DatoNumericoInput
import com.example.autos.util.numeroValido


@Composable
fun RefuelingDate(
    date: String,
    onDateChanged: (String) -> Unit
) {
    val changeDate = rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { changeDate.value = true }
    ) {
        Text(
            text = stringResource(id = R.string.fecha),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxWidth(0.35f)
        )
        if (changeDate.value){
            DatePickerView(
                head = stringResource(id = R.string.date_adjust),
                fieldInput = onDateChanged,
                changeDate = { changeDate.value = it },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            TextField(
                value = date,
                onValueChange = onDateChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically),
                readOnly = true,
                trailingIcon = @Composable {
                    IconButton(onClick = { changeDate.value = true }) {
                        Icon(
                            Icons.Outlined.DateRange,
                            contentDescription = stringResource(id = R.string.date_adjust)
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun Lleno(
    lleno: Boolean,
    onDataChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.full),
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .align(Alignment.CenterVertically)
        )
//            Spacer(modifier = Modifier.width(32.dp))
        Checkbox(
            checked = lleno,
            onCheckedChange = onDataChange
        )
    }

}

@Composable
fun RefuelingScreen(
    viewModel: RefuelingViewModel,
    lastRefueling: DomainRefueling?,
    onNewRefueling: () -> Unit
){
    val actualDate = viewModel.refuelingDate
    val kms = viewModel.actualKms
    val precio = viewModel.precio
    val costo = viewModel.coste
    val litros = viewModel.litros
    val lleno = viewModel.lleno


    fun areAllInputs(): Boolean {
        if (!numeroValido(kms.value, NumberType.INT)) return false
//        if (!numeroValido(kms.value ?: "", NumberType.INT, lastRefueling?.kms ?: viewModel.initKms)) return false
        if (!numeroValido(precio.value, NumberType.FLOAT3)) return false
        if (!numeroValido(costo.value, NumberType.FLOAT2)) return false
        if (!numeroValido(litros.value, NumberType.FLOAT2)) return false
        return true
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 8.dp, end = 8.dp)
    ){
        val focusManager = LocalFocusManager.current

        RefuelingDate(
            date = actualDate.value,
            onDateChanged = { viewModel.refuelingDate.value = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        DatoNumericoInput(
            label = stringResource(id = R.string.kms),
            numberType = NumberType.INT,
            focusManager = focusManager,
            focusRequest = true,
            value = kms.value,
            lastValue = (lastRefueling?.kms ?: viewModel.initKms).toString(),
            onDataChange = {
                viewModel.actualKms.value = it
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        DatoNumericoInput(
            label = stringResource(id = R.string.euros_litro),
            numberType = NumberType.FLOAT3,
            focusManager = focusManager,
            focusRequest = false,
            value = precio.value,
            lastValue = (lastRefueling?.eurosLitro ?: 1.234f).toString(),
            onDataChange = {
                viewModel.precio.value = it
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        DatoNumericoInput(
            label = stringResource(id = R.string.euros),
            numberType = NumberType.FLOAT2,
            focusManager = focusManager,
            value = costo.value,
            lastValue = (lastRefueling?.euros ?: 45.67f).toString(),
            auxFunc = {  viewModel.calcularLitros() },
            onDataChange = {
                viewModel.coste.value = it
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        DatoNumericoInput(
            label = stringResource(id = R.string.litros),
            numberType = NumberType.FLOAT2,
            focusManager = focusManager,
            value = litros.value,
            lastValue = (lastRefueling?.litros ?: 34.56f).toString(),
            onDataChange = {
                viewModel.litros.value = it
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Lleno(
            lleno = lleno.value,
            onDataChange = { viewModel.lleno.value = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.saveRefueling()
                onNewRefueling()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = areAllInputs()
        ) {
            Text(stringResource(id = R.string.save))
        }
    }
}