package com.example.autos.ui.estadisticas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autos.R
import com.example.autos.ui.composables.Dato
import com.example.autos.util.flipDate
import com.example.autos.util.localFloatFormat
import com.example.autos.util.localNumberFormat


@Composable
fun StatisticsScreen (
    viewModel: StatisticsViewModel,
    autoModelo: String,
    autoInitKms: Int,
    autoLastKms: Int,
    lastRefuelingLitros: Float
) {

    val (_, setLast) = rememberSaveable { viewModel.lastLitros }
    setLast(lastRefuelingLitros)

    val (totalTraveledKms, setTotalTraveledKms) = rememberSaveable { viewModel.totalKmsRecorridos }
    setTotalTraveledKms(autoLastKms - autoInitKms)

    val maxPrice = viewModel.maxPrice.observeAsState()
    val minPrice = viewModel.minPrice.observeAsState()
    val totalCost = viewModel.costeTotal.observeAsState()
    val totalPetrol = viewModel.petrolTotal.observeAsState()
    val totalAverage = viewModel.totalAverage.observeAsState()

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
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
            Row(Modifier.fillMaxWidth()) {
                Dato(
                    value = localNumberFormat(totalTraveledKms),
                    label = stringResource(id = R.string.kms_recorridos),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                )
                Dato(
                    value = localFloatFormat(totalPetrol.value ?: 0f),
                    label = stringResource(id = R.string.total_litros),
                    modifier = Modifier
                        .weight(0.8f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                Dato(
                    value = localFloatFormat(totalAverage.value ?: 0f),
                    label = stringResource(id = R.string.consumo_medio),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                )
                Dato(
                    value = localFloatFormat(totalCost.value ?: 0f),
                    label = stringResource(id = R.string.gasto_total),
                    modifier = Modifier
                        .weight(0.8f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                Dato(
                    value = localFloatFormat(maxPrice.value?.price ?: 0f),
                    label = stringResource(id = R.string.precio_max),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                )
                Dato(
                    value = flipDate((maxPrice.value?.fecha ?: "//")),
                    label = stringResource(id = R.string.fecha),
                    modifier = Modifier
                        .weight(0.8f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                Dato(
                    value = localFloatFormat(minPrice.value?.price ?: 0f),
                    label = stringResource(id = R.string.precio_min),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                )
                Dato(
                    value = flipDate((minPrice.value?.fecha ?: "//")),
                    label = stringResource(id = R.string.fecha),
                    modifier = Modifier
                        .weight(0.8f)
                )
            }
        }
    }
}