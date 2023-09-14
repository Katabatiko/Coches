package com.example.autos.ui.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autos.AutosStatus
import com.example.autos.R
import com.example.autos.domain.DomainGasto
import com.example.autos.ui.composables.Dato
import com.example.autos.util.flipDate
import com.example.autos.util.localFloatFormat
import com.example.autos.util.localNumberFormat

private const val TAG = "xxMs"

@Composable
fun MantenimientosScreen(
    list: State<List<DomainGasto>?>,
    status: State<AutosStatus?>,
    lastKms: State<Int?>,
    searching: (String) -> Unit
) {
    val buscar = rememberSaveable { mutableStateOf("") }
    val lastSearch = rememberSaveable { mutableStateOf("") }

    if (status.value == AutosStatus.LOADING){
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .padding(64.dp),
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.onTertiary,
            strokeWidth = 16.dp
        )
    } else {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.gastos),
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 20.sp
            )

            SearchBar(
                Modifier
                    .fillMaxWidth()
                    .heightIn(56.dp)
                    .padding(bottom = 8.dp),
                buscarString = buscar.value,
                onValueChange = { buscar.value = it }
            ) {
                searching(buscar.value)
                lastSearch.value = buscar.value.trim()
            }

            if (list.value.isNullOrEmpty()) {
                Text(
                    text = stringResource(id = R.string.no_gastos),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 52.dp),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    fontSize = 32.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (list.value!!.isNotEmpty() &&
                        buscar.value.isNotBlank() &&
                        buscar.value.trim() == lastSearch.value
                    ){
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = (
                                        stringResource(R.string.last_time)
                                            .format( buscar.value.trim(),
                                                localNumberFormat( (lastKms.value!!.minus(list.value!![0].kms)) ) )
                                        ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.tertiary),
                                color = MaterialTheme.colorScheme.onTertiary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                    items(list.value!!) { item ->
                        GastoItem(gasto = item)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GastoItem(gasto: DomainGasto) {
    val expanded = rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
    ){
        if (expanded.value) {
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(4.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Row (Modifier.clickable {
            expanded.value = !expanded.value
        }) {
            Text(
                text = flipDate(gasto.fecha),
                modifier = Modifier.weight(0.3f),
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Text(
                text = gasto.concepto,
                modifier = Modifier.weight(0.65f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            IconButton(
                onClick = { expanded.value = !expanded.value },
                modifier = Modifier
                    .weight(0.05f)
                    .height(24.dp)
            ) {
                if (!expanded.value){
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(id = R.string.mas)
                    )
                } else {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(id = R.string.menos)
                    )
                }
            }
        }

        if (expanded.value) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Dato(
                    value = localNumberFormat(gasto.kms),
                    label = stringResource(R.string.kms),
                    modifier = Modifier.weight(0.40f),
                    labelModifier = Modifier.padding(end = 4.dp)
                )
                Dato(
                    value = localFloatFormat(gasto.importe),
                    label = stringResource(id = R.string.euros),
                    modifier = Modifier.weight(0.3f),
                    labelModifier = Modifier.padding(start = 4.dp, end = 4.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.items),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
                Header()
                gasto.items.forEach{ domainItem -> 
                    WholeItem(item = domainItem)
                }
            }
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(4.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    buscarString: String,
    onValueChange: (String) -> Unit,
    onSearch: KeyboardActionScope.() -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    TextField(
        value = buscarString,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(text = stringResource(id = R.string.buscar))},
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(id = R.string.buscar)
            )
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch()
            }
        ),
        singleLine = true
    )
}
@Preview
@Composable
fun PreviewSearchBar() {
    SearchBar(
        Modifier
            .fillMaxWidth()
            .heightIn(56.dp)        // adaptable a cambios del usuario del tamaño de fuente en el sistema
            .padding(bottom = 4.dp),
        buscarString = "",
        onValueChange = {},
        onSearch = {}
    )
}
