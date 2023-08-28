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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
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
import com.example.autos.ui.composables.DateInput
import com.example.autos.ui.composables.StringInput
import com.example.autos.util.numeroValido
import com.example.autos.util.textEmpty
import com.example.autos.util.validacion
import kotlinx.coroutines.launch


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

    val modeloInput = viewModel.modelo
    val marcaInput = viewModel.marca
    val matriculaInput = viewModel.matricula
    val matriculacionInput = viewModel.fechaMatriculacion
    val buyDateInput = viewModel.fechaCompra
    val initKmsInput = viewModel.initKms

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 48.dp, end = 48.dp)
            .verticalScroll(state)
//            .imePadding()
    ) {
        val focusManager = LocalFocusManager.current

        StringInput(
            label = stringResource(id = R.string.modelo),
            focusManager = focusManager,
            focusRequest = true,
            stringInput = modeloInput.value,
            onDataChange = { viewModel.modelo.value = it }
        )

        Spacer(modifier = Modifier.height(8.dp))
        StringInput(
            label = stringResource(id = R.string.marca),
            focusManager = focusManager,
            stringInput = marcaInput.value,
            onDataChange = { viewModel.marca.value = it }
        )

        Spacer(modifier = Modifier.height(8.dp))
        StringInput(
            label = stringResource(id = R.string.matricula),
            focusManager = focusManager,
            stringInput = matriculaInput.value,
            onDataChange = { viewModel.matricula.value = it }
        )

        Spacer(modifier = Modifier.height(8.dp))
        DateInput(
            label = stringResource(id = R.string.fecha_matriculacion),
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 8.dp),
            focusManager = focusManager,
            dateInput = matriculacionInput.value,
            onDataChange = { viewModel.fechaMatriculacion.value = it }
        )

        Spacer(modifier = Modifier.height(8.dp))
        DateInput(
            label = stringResource(id = R.string.fecha_compra),
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 8.dp),
            focusManager = focusManager,
            dateInput = buyDateInput.value,
            onDataChange = { viewModel.fechaCompra.value = it }
        )

        Spacer(modifier = Modifier.height(8.dp))
        InitKmsInput(
            focusManager,
            initKmsInput.value,
            onDataChange = { viewModel.initKms.value = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.saveAuto()
                navController.navigate("home") {
                    // si cambia el coche se elimina el stack
                    popUpTo("home")
                }
                      },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = validacion(
                listOf(
                    modeloInput.value,
                    marcaInput.value,
                    matriculacionInput.value,
                    matriculaInput.value,
                    buyDateInput.value
                )
            ) && numeroValido(initKmsInput.value, NumberType.INT)
        ) {
            Text(stringResource(id = R.string.save))
        }
    }
}