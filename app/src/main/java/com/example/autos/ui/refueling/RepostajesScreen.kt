package com.example.autos.ui.refueling

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autos.R
import com.example.autos.domain.DomainRefueling
import com.example.autos.ui.composables.Dato
import com.example.autos.util.flipDate
import com.example.autos.util.localFloatFormat
import com.example.autos.util.localNumberFormat


@Composable
fun RefuelingItem(repostaje: DomainRefueling){
    val expanded = rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
    ){
        Row (Modifier.clickable {
            expanded.value = !expanded.value
        }) {
            Text(
                text = flipDate(repostaje.fecha),
                modifier = Modifier.weight(0.25f),
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Dato(
                value = localNumberFormat(repostaje.kms),
                label = stringResource(R.string.kms),
                modifier = Modifier.weight(0.40f),
                labelModifier = Modifier.padding(end = 4.dp)
            )
            Dato(
                value = localNumberFormat(repostaje.recorrido),
                label = stringResource(id = R.string.parcial),
                modifier = Modifier.weight(0.30f),
                labelModifier = Modifier.padding(end = 4.dp)
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
            Row {
                Dato(
                    value = localFloatFormat(repostaje.euros),
                    label = stringResource(id = R.string.euros),
                    modifier = Modifier.weight(0.3f),
                    labelModifier = Modifier.padding(start = 4.dp, end = 4.dp)
                )
                Dato(
                    value = localFloatFormat(repostaje.eurosLitro),
                    label = stringResource(id = R.string.euros_litro),
                    modifier = Modifier.weight(0.25f),
                    labelModifier = Modifier.padding(end = 4.dp)
                )
                Dato(
                    value = localFloatFormat(repostaje.litros),
                    label = stringResource(id = R.string.litros),
                    modifier = Modifier.weight(0.30f),
                    labelModifier = Modifier.padding(end = 4.dp)
                )
                if (repostaje.lleno) {
                    Text(
                        text = stringResource(id = R.string.full),
                        modifier = Modifier.weight(0.15f)
                    )
                }
            }
        }
    }
}

@Composable
fun RepostajesScreen(
    list: State<List<DomainRefueling>?>,
    onListReceived: () -> Unit
) {

    if (list.value.isNullOrEmpty()){
        Text(
            text = stringResource(id = R.string.no_registers),
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 52.dp),
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            fontSize = 32.sp
        )
    } else {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            onListReceived()
            items(list.value!!) { item ->
                RefuelingItem(item)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview
@Composable
fun ItemList() {
    val dr = DomainRefueling(
        recorrido = 591,
        kms = 224556,
        fecha = "11/07/2023",
        lleno = true,
        eurosLitro = 0.963f,
        litros = 46.26f,
        euros = 44.55f,
        cocheId = 1,
        refuelId = 12
    )
    RefuelingItem(repostaje = dr)
}