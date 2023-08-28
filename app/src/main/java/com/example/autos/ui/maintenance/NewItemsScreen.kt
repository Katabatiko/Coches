package com.example.autos.ui.maintenance

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.autos.NumberType
import com.example.autos.R
import com.example.autos.domain.DomainItem
import com.example.autos.util.numeroValido
import com.example.autos.util.redondeaDecimales
import com.example.autos.util.textEmpty

private const val TAG = "xxNis"
@Composable
fun NewItemsScreen(
    viewModel: GastosViewModel,
    navController: NavController
) {
    val list = viewModel.newItemList.observeAsState()

    val lastIndex = if (list.value!!.isEmpty()) 0
                    else list.value!!.size -1
    val state = rememberLazyListState(initialFirstVisibleItemIndex = lastIndex)

    val masIva = viewModel.masIva.observeAsState()
    val totalGasto = viewModel.totalGasto.observeAsState()

    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp)
//            .verticalScroll(state)
    ) {
        Text(
            text = stringResource(id = R.string.addItems),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        NewItem(
            viewModel,
//            Modifier.weight(4f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            state = state
//                .weight(3f)
        ){
            item {
                Header(/*modifier = Modifier.weight(0.1f)*/)
            }
            items(list.value!!){ item ->
                Item(
                    item = item,
                    onDelete = {
                        viewModel.removeItem(it)
                        viewModel.totalGasto.postValue(viewModel.subtotal())
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                /*.weight(0.5f)*/,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = stringResource(id = R.string.total),
                Modifier.padding(end = 8.dp),
                fontWeight = FontWeight.Bold
            )
            Text(text = "${redondeaDecimales(totalGasto.value ?: 0f,2)} €")
        }

        Row(
            Modifier
                .padding(end = 16.dp)
                .fillMaxWidth()
                /*.weight(0.5f)*/,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = stringResource(id = R.string.masIva),
                modifier = Modifier
//                    .fillMaxWidth(0.35f)
                    .align(Alignment.CenterVertically)
            )
//            Spacer(modifier = Modifier.width(32.dp))
            Checkbox(
                checked = masIva.value!!,
                onCheckedChange = {
                                    viewModel.masIva.postValue(it)
                                    if (it)     viewModel.addIva()
                                    else        viewModel.removeIva()
                                    viewModel.totalGasto.postValue(viewModel.subtotal())
                                  },
                enabled = list.value!!.isNotEmpty()
            )
        }
        Button(
            onClick = {
                navController.navigate("gasto")
//                viewModel.cleanItemInput()
//                viewModel.cleanGastoInputs()
                Log.d(TAG, viewModel.newItemList.value.toString())
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                /*.weight(1f)*/,
            enabled = list.value!!.isNotEmpty()
        ) {
            Text(stringResource(id = R.string.finalizar))
        }
    }
}

@Preview
@Composable
fun Header(
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = R.string.cant),
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Clip,
            maxLines = 1
        )
        Text(
            text = stringResource(id = R.string.descripcion),
            modifier = Modifier.weight(7f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(id = R.string.importe),
            modifier = Modifier.weight(1.5f),
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun NewItem(
    viewModel: GastosViewModel,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val cantidad = viewModel.cantidad.observeAsState()
    val descripcion = viewModel.descripcion.observeAsState()
    val marca = viewModel.marca.observeAsState()
    val precio = viewModel.precio.observeAsState()


    Column(
        modifier = modifier,
//        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        val focusRequester = remember { FocusRequester() }

        OutlinedTextField(
            value = TextFieldValue(descripcion.value!!, TextRange(descripcion.value!!.length)),
            onValueChange = { viewModel.descripcion.postValue(it.text) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .focusRequester(focusRequester),
            label = { Text(text = stringResource(id = R.string.descripcion))},
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            )
        )
        OutlinedTextField(
            value = TextFieldValue(marca.value!!, TextRange(marca.value!!.length)),
            onValueChange = { viewModel.marca.postValue(it.text) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            label = { Text(text = stringResource(id = R.string.detalles))},
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            )
        )

        Row(
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            val errorCantidad = rememberSaveable{ mutableStateOf(false) }
            val errorPrecio = rememberSaveable{ mutableStateOf(false) }

            OutlinedTextField(
                value = TextFieldValue(cantidad.value!!, TextRange(cantidad.value!!.length)),
                onValueChange = {
                    viewModel.cantidad.postValue(it.text)
                    errorCantidad.value = !numeroValido(it.text, NumberType.INT)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
                    .weight(0.3f),
//                    .onFocusChanged {
//                        if (it.isFocused) {
//                                            selec
//                        }
//                    },
                label = { Text(
                    text = stringResource(id = R.string.cantidad),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                ) },
                isError = errorCantidad.value,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        errorCantidad.value = textEmpty(cantidad.value!!)
                        focusManager.moveFocus(FocusDirection.Next)
                    }
                )
            )

            OutlinedTextField(
                value = TextFieldValue(precio.value!!, TextRange(precio.value!!.length)),
                onValueChange = {
                    viewModel.precio.postValue(it.text)
                    errorPrecio.value = !numeroValido(it.text, NumberType.FLOAT2)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
                    .weight(0.7f),
                label = { Text( stringResource(id = R.string.precio_unidad) ) },
                isError = errorPrecio.value,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        errorPrecio.value = textEmpty(precio.value!!)
                        focusManager.clearFocus()
                    }
                )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = {
                viewModel.addItem()
                viewModel.totalGasto.postValue(viewModel.subtotal())
                viewModel.cleanItemInput()
                focusManager.clearFocus()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !descripcion.value.isNullOrBlank() && !cantidad.value.isNullOrBlank() && !precio.value.isNullOrBlank()
        ) {
            Text(stringResource(id = R.string.save))
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun Item(
    item: DomainItem,
    onDelete: (DomainItem) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.cantidad.toString(),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = item.descripcion,
            modifier = Modifier.weight(6.5f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Icon(
            imageVector = Icons.Default.Delete,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    onDelete(item)
                },
            contentDescription = stringResource(id = R.string.borrar),
            tint = if (isSystemInDarkTheme())   MaterialTheme.colorScheme.errorContainer
                    else                        MaterialTheme.colorScheme.error
        )
        Text(
            text = redondeaDecimales((item.precio * item.cantidad),2).toString(),
            modifier = Modifier
                .weight(1.5f)
                .padding(end = 6.dp),
            textAlign = TextAlign.End
        )
    }
}

@Preview
@Composable
fun PreviewItem() {
    val item = DomainItem(
        0,
        0,
        "Artículo",
        "Marca",
        12.99f,
        1
    )
    Item(
        item = item,
        onDelete = {}
    )
}