package com.example.autos.ui.estadisticas

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autos.R
import com.example.autos.data.local.CompoundPrice
import com.example.autos.domain.AverageRefueling
import com.example.autos.domain.DatoByYear
import com.example.autos.domain.DomainGasto
import com.example.autos.domain.PricesByYear
import com.example.autos.ui.composables.Dato
import com.example.autos.util.flipAndSortDate
import com.example.autos.util.flipDate
import com.example.autos.util.localFloatFormat
import com.example.autos.util.localNumberFormat
import kotlin.math.ceil

private var columnNumber = 2
private const val TAG = "xxSs"

@Composable
fun StatisticsScreen (
    viewModel: StatisticsViewModel,
    autoModelo: String,
    autoInitKms: Int,
    autoLastKms: Int,
    lastRefuelingLitros: Float
) {
    val state = rememberScrollState()

    val (_, setLast) = rememberSaveable { viewModel.lastLitros }
    setLast(lastRefuelingLitros)

    val (totalTraveledKms, setTotalTraveledKms) = rememberSaveable { viewModel.totalKmsRecorridos }
    setTotalTraveledKms(autoLastKms - autoInitKms)

    val maxPrice = viewModel.maxPrice.observeAsState()
    val minPrice = viewModel.minPrice.observeAsState()
    val totalPetrolCost = viewModel.costeTotalPetrol
    val totalPetrol = viewModel.petrolTotal.observeAsState()
    val totalAverage = viewModel.totalAverage.observeAsState()
    val totalGastos = viewModel.totalGastos
    
    val kmsByYear = viewModel.kmsByYear
    val listAverages = viewModel.listAverage
    val pricesByYear = viewModel.pricesByYear
    val gastoByYear = viewModel.gastosByYear
    val frontTiresChanges = viewModel.frontTiresChanges
    val backTiresChanges = viewModel.backTiresChanges

    val orientacion = remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val configuracion = LocalConfiguration.current
    LaunchedEffect(configuracion) {
        snapshotFlow { configuracion.orientation }
            .collect { orientacion.value = it }
    }
    columnNumber = when (orientacion.value) {
                        Configuration.ORIENTATION_LANDSCAPE -> 4
                        else                                -> 2
                    }
//
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .verticalScroll(state)
    ) {
        Text(
            text = autoModelo,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary),
            color = MaterialTheme.colorScheme.onSecondary,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (totalPetrol.value == null){
            Text(
                text = stringResource(id = R.string.no_data),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 52.dp),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                fontSize = 32.sp
            )
        } else {
            Text(
                text = stringResource(id = R.string.totales),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                style = TextStyle(background = MaterialTheme.colorScheme.surfaceVariant)
            )
            Dato(
                value = "${localNumberFormat(totalTraveledKms)} ${stringResource(id = R.string.kms_)}",
                label = stringResource(id = R.string.recorrido),
                modifier = Modifier
                    .padding(end = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                Dato(
                    value = localFloatFormat(totalAverage.value ?: 0f),
                    label = stringResource(id = R.string.consumo_medio),
                    valueAtEnd = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    valueModifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.width(4.dp))
                Dato(
                    value = localFloatFormat(totalPetrol.value ?: 0f),
                    label = stringResource(id = R.string.total_litros),
                    valueAtEnd = true,
                    modifier = Modifier
                        .weight(0.9f),
                    valueModifier = Modifier.fillMaxWidth()
                )
            }

            Row(Modifier.fillMaxWidth()) {
                Dato(
                    value = localFloatFormat(maxPrice.value?.price ?: 0f),
                    label = stringResource(id = R.string.precio_max),
                    valueAtEnd = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    valueModifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.width(4.dp))
                Dato(
                    value = flipDate(maxPrice.value?.fecha),
                    label = stringResource(id = R.string.fecha),
                    valueAtEnd = true,
                    modifier = Modifier
                        .weight(0.9f),
                    valueModifier = Modifier.fillMaxWidth()
                )
            }

            Row(Modifier.fillMaxWidth()) {
                Dato(
                    value = localFloatFormat(minPrice.value?.price ?: 0f),
                    label = stringResource(id = R.string.precio_min),
                    valueAtEnd = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    valueModifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.width(4.dp))
                Dato(
                    value = flipDate(minPrice.value?.fecha),
                    label = stringResource(id = R.string.fecha),
                    valueAtEnd = true,
                    modifier = Modifier
                        .weight(0.9f),
                    valueModifier = Modifier.fillMaxWidth()
                )
            }
            Dato(
                value = "${localFloatFormat(totalPetrolCost.value ?: 0f)} ${stringResource(id = R.string.Eu)}",
                label = stringResource(id = R.string.gasto_petrol_total),
                modifier = Modifier
            )
            Spacer(modifier = Modifier.height(8.dp))
            Dato(
                value = "${localFloatFormat(totalGastos.value ?: 0f)} ${stringResource(id = R.string.Eu)}",
                label = stringResource(id = R.string.total_mantenimiento),
//                modifier = Modifier
//                    .weight(0.8f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(4.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))


            Text(
                text = stringResource(id = R.string.parciales),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                style = TextStyle(background = MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val expandedKms = rememberSaveable { mutableStateOf(false) }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expandedKms.value = !expandedKms.value }
            ) {
                Text(
                    text = stringResource(id = R.string.kilometros).replace(":", ""),
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    style = TextStyle(background = MaterialTheme.colorScheme.surfaceVariant)
                )
                IconButton(
                    onClick = { expandedKms.value = !expandedKms.value },
                    modifier = Modifier
                        .height(24.dp)
                ) {
                    if (!expandedKms.value) {
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
            Column(Modifier.fillMaxWidth()) {
                if (kmsByYear.value.isEmpty()){
                    Text(
                        text = stringResource(id = R.string.no_data),
                        modifier = Modifier
                            .fillMaxSize(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                } else {
                    val itemsCount = kmsByYear.value.size
                    val filas = ceil((itemsCount / columnNumber.toFloat()).toDouble()).toInt()
                    var count = 0

                    for (row in 1..filas) {
                        if (row < 4) {
                            count = makeDatoByYearRow(
                                count,
                                itemsCount,
                                kmsByYear
                            )
                        } else if (expandedKms.value){
                            count = makeDatoByYearRow(
                                count,
                                itemsCount,
                                kmsByYear
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val expandedConsumo = rememberSaveable { mutableStateOf(false) }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expandedConsumo.value = !expandedConsumo.value }
            ) {

                Text(
                    text = stringResource(id = R.string.petrol),
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    style = TextStyle(background = MaterialTheme.colorScheme.surfaceVariant)
                )
                IconButton(
                    onClick = { expandedConsumo.value = !expandedConsumo.value },
                    modifier = Modifier
                        .height(24.dp)
                ) {
                    if (!expandedConsumo.value) {
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
            Column(Modifier.fillMaxWidth()) {
                if (listAverages.value.isEmpty()){
                    Text(
                        text = stringResource(id = R.string.no_data),
                        modifier = Modifier
                            .fillMaxSize(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                } else {
                    listAverages.value.forEachIndexed { i, item ->
                        if (i < 4){
                            ParcialMedia(media = item)
                        } else if (expandedConsumo.value) {
                            ParcialMedia(media = item)
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(8.dp))
            val expandedPrecios = rememberSaveable { mutableStateOf(false) }

            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expandedPrecios.value = !expandedPrecios.value }
            ) {
                Text(
                    text = stringResource(id = R.string.precios),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    style = TextStyle(background = MaterialTheme.colorScheme.surfaceVariant)
                )
                IconButton(
                    onClick = { expandedPrecios.value = !expandedPrecios.value },
                    modifier = Modifier
                        .height(24.dp)
                ) {
                    if (!expandedPrecios.value){
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
            Column(Modifier.fillMaxWidth()) {
                if (pricesByYear.value.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.no_registers),
                        modifier = Modifier
                            .fillMaxSize(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                } else {
                    pricesByYear.value.forEachIndexed { i, price ->
                        if (i < 3) {
                            AnualMinAndMaxPrice(prices = price)
                            Spacer(modifier = Modifier.height(2.dp))
                        } else {
                            if (expandedPrecios.value) {
                                AnualMinAndMaxPrice(prices = price)
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val expandedGastos = rememberSaveable { mutableStateOf(false) }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expandedGastos.value = !expandedGastos.value }
            ) {
                Text(
                    text = stringResource(id = R.string.gastos),
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    style = TextStyle(background = MaterialTheme.colorScheme.surfaceVariant)
                )
                IconButton(
                    onClick = { expandedGastos.value = !expandedGastos.value },
                    modifier = Modifier
                        .height(24.dp)
                ) {
                    if (!expandedGastos.value) {
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
            Column(Modifier.fillMaxWidth()) {
                if (gastoByYear.value.isEmpty()){
                    Text(
                        text = stringResource(id = R.string.no_registers),
                        modifier = Modifier
                            .fillMaxSize(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                } else {
                    val itemsCount = gastoByYear.value.size
                    val filas = ceil((itemsCount / columnNumber.toFloat()).toDouble()).toInt()
                    var count = 0

                    for (row in 1..filas) {
                        if (row < 4) {
                            count = makeDatoByYearRow(count, itemsCount, gastoByYear, "€")
                        } else if (expandedGastos.value){
                            count = makeDatoByYearRow(count, itemsCount, gastoByYear, "€")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.front_tires),
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    style = TextStyle(background = MaterialTheme.colorScheme.surfaceVariant)
                )

                val count = frontTiresChanges.value.size
                if (count == 0){
                    Text(
                        text = stringResource(id = R.string.no_registers),
                        modifier = Modifier
                            .fillMaxSize(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                } else {
                    MakeTireRows(
                        itemList = frontTiresChanges,
                        autoLastKms = autoLastKms
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.back_tires),
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    style = TextStyle(background = MaterialTheme.colorScheme.surfaceVariant)
                )

                val count = backTiresChanges.value.size
                if (count == 0){
                    Text(
                        text = stringResource(id = R.string.no_registers),
                        modifier = Modifier
                            .fillMaxSize(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                } else {
                    MakeTireRows(
                        itemList = backTiresChanges,
                        autoLastKms = autoLastKms
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(4.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun makeDatoByYearRow(
    count: Int,
    itemsCount: Int,
    datoByYear: MutableState<List<DatoByYear>>,
    sufix: String = ""
): Int {
    var counter = count
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if ((counter + columnNumber) < itemsCount) {
            for (i in 1..columnNumber) {
                val year = datoByYear.value[counter++]
                Dato(
                    value = "${localNumberFormat(year.dato)} $sufix",
                    label = "${year.year} -> ",
                    valueAtEnd = true,
                    valueModifier = Modifier.width(55.dp)
                )
            }
        } else {
            for (i in counter until itemsCount) {
                val year = datoByYear.value[counter++]
                Dato(
                    value = "${localNumberFormat(year.dato)} $sufix",
                    label = "${year.year} -> ",
                    valueAtEnd = true,
                    valueModifier = Modifier.width(55.dp)
                )
            }
        }
    }
    return counter
}


@Composable
fun AnualMinAndMaxPrice(prices: PricesByYear) {
    val meses: Array<String> = stringArrayResource(id = R.array.meses)
    val minDate = prices.min.fecha?.split("/")
    val maxDate = prices.max.fecha?.split("/")
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${prices.year}: ",
            modifier = Modifier.weight(0.15f),
            fontWeight = FontWeight.Bold
        )
        Column(
            Modifier
                .fillMaxWidth()
                .weight(0.85f)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.min),
                    modifier = Modifier.weight(0.15f)
                )
                Text(
                    text = "${localFloatFormat(prices.min.price)}${stringResource(id = R.string.Eu)}",
                    modifier = Modifier.weight(0.25f),
                )
                Text(
                    text = stringResource(id = R.string.dia_mes_template).format(minDate!![2]),
                    modifier = Modifier.weight(0.3f),
                )
                Text(
                    text = meses[(minDate[1].toInt() -1)],
                    modifier = Modifier.weight(0.3f),
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.max),
                    modifier = Modifier.weight(0.15f)
                )
                Text(
                    text = "${localFloatFormat(prices.max.price)}${stringResource(id = R.string.Eu)}",
                    modifier = Modifier.weight(0.25f),
                )
                Text(
                    text = stringResource(id = R.string.dia_mes_template).format(maxDate!![2]),
                    modifier = Modifier.weight(0.3f),
                )
                Text(
                    text = meses[(maxDate[1].toInt() -1)],
                    modifier = Modifier.weight(0.3f),
                )
            }
        }
    }
}

@Composable
fun ParcialMedia(media: AverageRefueling) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Dato(
            value = flipAndSortDate(media.initFecha),
            label = stringResource(id = R.string.init),
            modifier = Modifier.weight(0.22f),
            labelModifier = Modifier.padding(end = 2.dp)
        )
        Dato(
            value = flipAndSortDate(media.endFecha),
            label = stringResource(id = R.string.end),
            modifier = Modifier.weight(0.2f),
            labelModifier = Modifier.padding(end = 2.dp)
        )
        Dato(
            value = localNumberFormat(media.kms),
            label = stringResource(id = R.string.kms),
//                                valueAtEnd = true,
            modifier = Modifier
                .weight(0.22f)
                .padding(end = 2.dp)
        )
        Dato(
            value = "${localFloatFormat(media.consumo)} ${stringResource(id = R.string.l)}",
            label = stringResource(id = R.string.media),
            valueAtEnd = true,
            modifier = Modifier
                .weight(0.3f)
                .padding(start = 2.dp),
            valueModifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun AnualPreview() {
    val minPrice = CompoundPrice(
        price = 0.123f,
        fecha = "2023/11/04"
    )
    val maxPrice = CompoundPrice(
        price = 1.234f,
        fecha = "2023/04/11"
    )
    val prices = PricesByYear(
        year = "2023",
        min = minPrice,
        max = maxPrice
    )

    AnualMinAndMaxPrice(prices = prices)
}

@Composable
private fun MakeTireRows(
    itemList: State<List<DomainGasto>>,
    autoLastKms: Int
) {
    val count = itemList.value.size

    if (count > 0){
        val last = itemList.value[0]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Dato(
                value = flipDate(last.fecha),
                label = stringResource(id = R.string.fecha),
                modifier = Modifier.weight(1f)
            )
            Dato(
                value = "${localNumberFormat(autoLastKms - last.kms)}${stringResource( id = R.string.kms_ )}",
                label = stringResource(id = R.string.recorrido),
                modifier = Modifier.weight(1f),
                labelModifier = Modifier.padding(end = 2.dp)
            )
        }
    }
    itemList.value.forEachIndexed { i, gasto ->
        if (i < count - 1) {
            val next = itemList.value[i + 1]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Dato(
                    value = flipDate(next.fecha),
                    label = stringResource(id = R.string.fecha),
                    modifier = Modifier.weight(1f)
                )
                Dato(
                    value = "${localNumberFormat(gasto.kms - next.kms)}${stringResource( id = R.string.kms_ )}",
                    label = stringResource(id = R.string.recorrido),
//                    valueAtEnd = true,
                    modifier = Modifier.weight(1f),
                    labelModifier = Modifier.padding(end = 2.dp)
                )
            }
        }
    }
}