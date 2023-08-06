package com.example.autos.ui.refueling

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.autos.NumberType
import com.example.autos.R
import com.example.autos.domain.DomainRefueling
import com.example.autos.ui.composables.DatePickerView
import com.example.autos.util.numeroValido
import com.example.autos.util.textEmpty


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
fun KilometrajeInput(
    focusManager: FocusManager,
    kms: String,
    lastKms: Int?,
    onDataChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    val error = rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.kilometros),
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .align(Alignment.CenterVertically)
        )
        OutlinedTextField(
            value = TextFieldValue(kms, TextRange(kms.length)),
            onValueChange = {
                onDataChange(it.text)
                error.value = !numeroValido(it.text, NumberType.INT)
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .focusRequester(focusRequester),
            label = { Text(
                lastKms?.toString() ?: "100000"
            ) },
            isError = error.value,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    error.value = textEmpty(kms)
                    focusManager.moveFocus(FocusDirection.Next)
                }
            )
        )
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun PrecioInput(
    focusManager: FocusManager,
    eurosLitro: String,
    lastPrecio: Float?,
    onDataChange: (String) -> Unit
) {
    val error = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.euros_litro),
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .align(Alignment.CenterVertically)
        )
        OutlinedTextField(
            value = TextFieldValue(eurosLitro, TextRange(eurosLitro.length)),
            onValueChange = {
                onDataChange(it.text)
                error.value = !numeroValido(it.text, NumberType.FLOAT3)
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            label = { Text(
                lastPrecio?.toString() ?: "1.234"
            ) },
            isError = error.value,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    error.value = textEmpty(eurosLitro)
                    focusManager.moveFocus(FocusDirection.Next)
                }
            )
        )
    }

}

@Composable
fun CostoInput(
    focusManager: FocusManager,
    costo: String,
    lastCosto: Float?,
    onDataChange: (String) -> Unit,
    calcularLitros: () -> Unit
) {
    val error = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(id = R.string.euros),
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .align(Alignment.CenterVertically)
        )
        OutlinedTextField(
            value = TextFieldValue(costo, TextRange(costo.length)),
            onValueChange = {
                onDataChange(it.text)
                error.value = !numeroValido(it.text, NumberType.FLOAT2)
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            label = { Text(
                lastCosto?.toString() ?: "56.78"
            ) },
            isError = error.value,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    error.value = textEmpty(costo)
                    focusManager.moveFocus(FocusDirection.Next)
                    calcularLitros()
                }
            )
        )
    }

}

@Composable
fun LitrosInput(
    focusManager: FocusManager,
    modifier: Modifier,
    litros: String,
    lastLitros: Float?,
    onDataChange: (String) -> Unit
) {
    val error = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.litros),
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .align(Alignment.CenterVertically)
        )
        OutlinedTextField(
            value = TextFieldValue(litros, TextRange(litros.length)),
            onValueChange = {
                onDataChange(it.text)
                error.value = !numeroValido(it.text, NumberType.FLOAT2)
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            label = { Text(
                lastLitros?.toString() ?: "45.54"
            ) },
            isError = error.value,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    error.value = textEmpty(litros)
                    focusManager.clearFocus()
                }
            )
        )
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
    autoId: Int,
    lastRefueling: DomainRefueling?,
    navController: NavController,
    onNewRefueling: () -> Unit
){
    val actualDate = viewModel.refuelingDate.observeAsState()
    val kms = viewModel.actualKms.observeAsState()
    val precio = viewModel.precio.observeAsState()
    val costo = viewModel.coste.observeAsState()
    val litros = viewModel.litros.observeAsState()
    val lleno = viewModel.lleno.observeAsState()


    fun areAllInputs(): Boolean {
        if (!numeroValido(kms.value ?: "", NumberType.INT)) return false
        if (!numeroValido(precio.value ?: "", NumberType.FLOAT3)) return false
        if (!numeroValido(costo.value ?: "", NumberType.FLOAT2)) return false
        if (!numeroValido(litros.value ?: "", NumberType.FLOAT2)) return false
        return true
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, start = 8.dp, end = 8.dp)
    ){
        val focusManager = LocalFocusManager.current

        RefuelingDate(
            date = actualDate.value!!,
            onDateChanged = { viewModel.refuelingDate.postValue(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        KilometrajeInput(
            focusManager = focusManager,
            kms = kms.value ?: "",
            lastKms = lastRefueling?.kms ?: viewModel.initKms,
            onDataChange = {
                viewModel.actualKms.postValue(it)
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        PrecioInput(
            focusManager = focusManager,
            eurosLitro = precio.value ?: "",
            lastPrecio = lastRefueling?.eurosLitro ?: 1.234f,
            onDataChange = {
                viewModel.precio.postValue(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        CostoInput(
            focusManager = focusManager,
            costo = costo.value ?: "",
            lastCosto = lastRefueling?.euros ?: 45.67f,
            onDataChange = {
                viewModel.coste.postValue(it) },
            calcularLitros = { viewModel.calcularLitros() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        LitrosInput(
            focusManager = focusManager,
            Modifier.fillMaxWidth(),
            litros = litros.value ?: "",
            lastLitros = lastRefueling?.litros ?: 34.56f,
            onDataChange = {
                viewModel.litros.postValue(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Lleno(
            lleno = lleno.value!!,
            onDataChange = { viewModel.lleno.postValue(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.saveRefueling(autoId)
                navController.navigate("historico")
                onNewRefueling()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = areAllInputs()
        ) {
            Text(stringResource(id = R.string.save))
        }
    }
}