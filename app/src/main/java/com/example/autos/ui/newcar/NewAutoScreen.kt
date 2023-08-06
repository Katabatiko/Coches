package com.example.autos.ui.newcar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
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
import com.example.autos.ui.composables.DatePickerView
import com.example.autos.util.numeroValido
import com.example.autos.util.textEmpty
import com.example.autos.util.validacion
import kotlinx.coroutines.launch


@Composable
private fun ModeloInput(
    focusManager: FocusManager,
    modeloInput: String,
    onDataChange: (String) -> Unit
) {
    val error = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = TextFieldValue(modeloInput, TextRange(modeloInput.length)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .focusRequester(focusRequester),
        onValueChange = {
            onDataChange(it.text)
            error.value = textEmpty(it.text)
        },
        isError = error.value,
        label = { Text(stringResource(id = R.string.modelo)) },
        keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next ),
        keyboardActions = KeyboardActions(
            onNext = {
                error.value = textEmpty(modeloInput)
                focusManager.moveFocus(FocusDirection.Next)
            }
        )
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun MarcaInput(
    focusManager: FocusManager,
    marcaInput: String,
    onDataChange: (String) -> Unit
) {
    val error = remember { mutableStateOf(false) }

    OutlinedTextField(
        value = TextFieldValue(marcaInput, TextRange(marcaInput.length)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        onValueChange = {
            onDataChange(it.text)
            error.value = textEmpty(it.text)
        },
        isError = error.value,
        label = { Text(stringResource(id = R.string.marca)) },
        keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next ),
        keyboardActions = KeyboardActions(
            onNext = {
                error.value = textEmpty(marcaInput)
                focusManager.moveFocus(FocusDirection.Next)
            }
        )
    )
}

@Composable
private fun MatriculacionInput(
    focusManager: FocusManager,
    matriculacionInput: String,
    onDataChange: (String) -> Unit
) {
    val matriculacionFocus = rememberSaveable { mutableStateOf(false) }

    val error = remember { mutableStateOf(false) }

    if (matriculacionFocus.value){
        DatePickerView(
            stringResource(id = R.string.fecha_matriculacion),
            Modifier.fillMaxWidth(),
            fieldInput = onDataChange,
            changeDate = { matriculacionFocus.value = it},
        )
    } else {
        OutlinedTextField(
            value = matriculacionInput,
            onValueChange = {
                onDataChange(it)
                error.value = textEmpty(it)
            },
            label = { Text(stringResource(id = R.string.fecha_matriculacion)) },
            modifier = Modifier
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        matriculacionFocus.value = true
                    }
                }
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            isError = error.value,
            readOnly = true,
            keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next ),
            keyboardActions = KeyboardActions(
                onNext = {
                    error.value = textEmpty(matriculacionInput)
                    focusManager.moveFocus(FocusDirection.Next)
                }
            )
        )
    }
}

@Composable
private fun MatriculaInput(
    focusManager: FocusManager,
    matriculaInput: String,
    onDataChange: (String) -> Unit

) {
    val error = remember { mutableStateOf(false) }

    OutlinedTextField(
        value = TextFieldValue(matriculaInput, TextRange(matriculaInput.length)),
        onValueChange = {
            onDataChange(it.text)
            error.value = textEmpty(it.text)
        },
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        isError = error.value,
        label = { Text(stringResource(id = R.string.matricula)) },
        keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next ),
        keyboardActions = KeyboardActions(
            onNext = {
                error.value = textEmpty(matriculaInput)
                focusManager.moveFocus(FocusDirection.Next)
            }
        )
    )
}

@Composable
private fun BuyDateInput(
    focusManager: FocusManager,
    buyDateInput: String,
    onDataChange: (String) -> Unit
) {
    val buyDateFocus = rememberSaveable { mutableStateOf(false) }

    val error = remember { mutableStateOf(false) }

    if (buyDateFocus.value){
        DatePickerView(
            stringResource(id = R.string.fecha_compra), Modifier.fillMaxWidth(),
            fieldInput = onDataChange,
            changeDate = { buyDateFocus.value = it}
        )
    } else {
        OutlinedTextField(
            value = buyDateInput,
            onValueChange = {
                onDataChange(it)
                error.value = textEmpty(it)
            },
            label = { Text(stringResource(id = R.string.fecha_compra)) },
            isError = error.value,
            modifier = Modifier
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        buyDateFocus.value = true
                    }
                }
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next ),
            keyboardActions = KeyboardActions(
                onNext = {
                    error.value = textEmpty(buyDateInput)
                    focusManager.moveFocus(FocusDirection.Next)
                }
            ),
            readOnly = true
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InitKmsInput(
    focusManager: FocusManager,
    initKms: String,
    onDataChange: (String) -> Unit
) {
    val error = remember { mutableStateOf(false) }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    OutlinedTextField(
        value = TextFieldValue(initKms, TextRange(initKms.length)),
        onValueChange = {
            onDataChange(it.text)
            error.value = !numeroValido(it.text, NumberType.INT)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    coroutineScope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        label = { Text(stringResource(id = R.string.kms_iniciales)) },
        isError = error.value,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                error.value = textEmpty(initKms)
                focusManager.clearFocus()
            }
        )
    )
}

@Composable
fun NewAutoScreen(
    viewModel: NewCarViewModel,
    navController: NavController
) {
    val state = rememberScrollState()

    val modeloInput = viewModel.modelo.observeAsState()
    val marcaInput = viewModel.marca.observeAsState()
    val matriculaInput = viewModel.matricula.observeAsState()
    val matriculacionInput = viewModel.fechaMatriculacion.observeAsState()
    val buyDateInput = viewModel.fechaCompra.observeAsState()
    val initKmsInput = viewModel.initKms.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 48.dp, end = 48.dp)
            .verticalScroll(state)
    ) {
        val focusManager = LocalFocusManager.current

        ModeloInput(
            focusManager,
            modeloInput.value ?: "",
            onDataChange = { viewModel.modelo.postValue(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        MarcaInput(
            focusManager,
            marcaInput.value ?: "",
            onDataChange = { viewModel.marca.postValue(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        MatriculaInput(
            focusManager,
            matriculaInput.value ?: "",
            onDataChange = { viewModel.matricula.postValue(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        MatriculacionInput(
            focusManager,
            matriculacionInput.value ?: "",
            onDataChange = {
                viewModel.fechaMatriculacion.postValue(it)
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        BuyDateInput(
            focusManager,
            buyDateInput.value ?: "",
            onDataChange = { viewModel.fechaCompra.postValue(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        InitKmsInput(
            focusManager,
            initKmsInput.value ?: "",
            onDataChange = { viewModel.initKms.postValue(it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.saveAuto()
                navController.navigate("home")
                // si cambia el coche se elimina el stack
//                navController.popBackStack("home", true)
                      },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = validacion(
                listOf(
                    modeloInput.value!!,
                    marcaInput.value!!,
                    matriculacionInput.value!!,
                    matriculaInput.value!!,
                    buyDateInput.value!!
                )
            ) && numeroValido(initKmsInput.value!!, NumberType.INT)
        ) {
            Text(stringResource(id = R.string.save))
        }
    }
}