package com.example.autos.ui.maintenance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autos.IVA
import com.example.autos.R
import com.example.autos.domain.DomainGasto
import com.example.autos.domain.DomainItem
import com.example.autos.ui.composables.Dato
import com.example.autos.util.flipDate
import com.example.autos.util.localNumberFormat
import com.example.autos.util.redondeaDecimales


@Composable
fun GastoScreen(
    gasto: DomainGasto,
    onSave: (Boolean) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        Text(
            text = gasto.concepto,
            fontSize = 24.sp,
            textDecoration = TextDecoration.Underline
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Dato(
                value = flipDate(gasto.fecha),
                label = stringResource(id = R.string.fecha),
                modifier = Modifier.weight(1f)
            )
            Dato(
                value = localNumberFormat(gasto.kms),
                label = stringResource(id = R.string.kms),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.items),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Header()
        Column() {
            gasto.items.forEach { item ->
                WholeItem(
                    item = item
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(onClick = { onSave(false) }) {
                Text(stringResource(id = android.R.string.cancel))
            }
            Button(onClick = { onSave(true) }) {
                Text(stringResource(id = R.string.save))
            }
        }
    }

}

@Composable
fun WholeItem(item: DomainItem) {
    Column() {
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
            Text(
                text = redondeaDecimales((item.precio * item.cantidad),2).toString(),
                modifier = Modifier
                    .weight(1.5f)
                    .padding(end = 6.dp),
                textAlign = TextAlign.End
            )
        }
        if (item.descripcion != "iva $IVA%") {
            Row(
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = item.marca ?: "",
                    modifier = Modifier.weight(6.5f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                if (item.cantidad != 1) {
                    Dato(
                        value = item.precio.toString(),
                        label = stringResource(id = R.string.euros_u),
                        modifier = Modifier
                            .weight(2.5f)
                            .padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewGastoScreen() {
    val item1 = DomainItem(
        itemId = 0,
        gastoId = 9,
        descripcion = "Aceite",
        marca = "BP",
        precio = 15.99f,
        cantidad = 1
    )
    val item2 = DomainItem(
        itemId = 0,
        gastoId = 9,
        descripcion = "Filtro de Aceite",
        marca = "Mecafilter",
        precio = 5.70f,
        cantidad = 1
    )
    val item3 = DomainItem(
        itemId = 0,
        gastoId = 9,
        descripcion = "Filtro de Aire",
        marca = "Knecht",
        precio = 21.42f,
        cantidad = 1
    )
    val gasto = DomainGasto(
        gastoId = 9,
        fecha = "2023/08/22",
        concepto = "Cambio de aceite",
        autoId = 1,
        kms = 283002,
        importe = 123.49f,
        items = listOf(item1, item2, item3)
    )

    GastoScreen(
        gasto = gasto,
        onSave = {}
    )
}