package com.example.autos.ui.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.autos.R
import com.example.autos.domain.DomainCoche
import com.example.autos.util.flipDate
import com.example.autos.util.localNumberFormat

private const val TAG = "xxHs"
@Composable
fun Modelo(
    modelo: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = modelo,
        modifier.background(MaterialTheme.colorScheme.secondary)
            .padding(start = 6.dp),
        color = MaterialTheme.colorScheme.onSecondary,
        fontSize = 34.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 40.sp
    )
}

@Composable
fun Marca(
    marca: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = marca,
        modifier.padding(start = 6.dp),
        fontSize = 22.sp,
        fontStyle = FontStyle.Italic
    )
}

@Composable
fun Matricula(
    matricula: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.padding(vertical = 6.dp)
    ) {
        Text(
            text = stringResource(R.string.matricula),
            Modifier.fillMaxWidth(1f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = matricula,
            Modifier.fillMaxWidth(1f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FechaMatricula(
    fecha: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.padding(vertical = 6.dp)
    ) {
        Text(
            text = stringResource(R.string.fecha_matriculacion),
            Modifier.fillMaxWidth(1f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = fecha,
            Modifier.fillMaxWidth(1f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BuyDate(
    fecha: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.padding(vertical = 6.dp)
    ) {
        Text(
            text = stringResource(R.string.fecha_compra),
            Modifier.fillMaxWidth(1f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = flipDate(fecha),
            Modifier.fillMaxWidth(1f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun InitKms(
    initKms: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.padding(vertical = 6.dp)
    ) {
        Text(
            text = stringResource(R.string.kms_iniciales),
            Modifier.fillMaxWidth(1f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = localNumberFormat(initKms),
            Modifier.fillMaxWidth(1f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun Kilometraje(
    kms: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.padding(vertical = 6.dp)
    ) {
        Text(
            text = stringResource(R.string.kilometraje),
            Modifier.fillMaxWidth(1f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = localNumberFormat(kms),
            Modifier.fillMaxWidth(1f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun KmsRecorridos(
    kms: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.padding(vertical = 6.dp)
    ) {
        Text(
            text = stringResource(R.string.kms_recorridos),
            Modifier.fillMaxWidth(1f),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = localNumberFormat(kms),
            Modifier.fillMaxWidth(1f),
            textAlign = TextAlign.Center
        )
    }
}

//@Preview
@Composable
private fun VehicleScreen(car: DomainCoche?) {

    Column(
        Modifier.padding(vertical = 24.dp)
    ){
        car.let {
            Modelo(
                modelo = it?.modelo ?: "",
                Modifier
                    .fillMaxWidth(1f)
                    .padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Marca(
                marca = it?.marca ?: "",
                Modifier
                    .fillMaxWidth(1f)
                    .size(28.dp)
                    .padding(horizontal = 8.dp)
            )
        }

        Row(
            Modifier.padding(horizontal = 6.dp, vertical = 16.dp)
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(8.dp, 0.dp)
            ) {
                car.let {
                    Matricula(
                        matricula = it?.matricula ?: ""
                    )
                    BuyDate(
                        fecha = it?.buyDate ?: "//"
                    )
                    Kilometraje(
                        kms = it?.actualKms ?: 0
                    )
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(8.dp, 0.dp)
            ) {
                car.let {
                    FechaMatricula(
                        fecha = it?.year ?: "//"
                    )
                    InitKms(
                        initKms = it?.initKms ?: 0
                    )
                }
                KmsRecorridos(
                    kms = (car?.actualKms ?: 0) - (car?.initKms ?: 0)
                )
            }
        }
    }
}

//    @Preview
@Composable
fun HomeFabs(navController: NavController) {
    Row(
        modifier = Modifier
            .padding(vertical = 24.dp)
            .fillMaxWidth(0.92f),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FloatingActionButton(
            onClick = {  },
            contentColor = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_maintenance),
                contentDescription = stringResource(R.string.add_cost)
            )
        }
//            Spacer(modifier = Modifier.width(IntrinsicSize.Max))
        FloatingActionButton(
            onClick = { navController.navigate("refueling") },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.twotone_local_gas_station_24),
                contentDescription = stringResource(R.string.new_repostaje)
            )
        }
    }
}


@Composable
fun HomeScreen(
    auto: DomainCoche?,
    navController: NavController
){
    Scaffold (
        floatingActionButton = { HomeFabs(navController) },
        floatingActionButtonPosition = FabPosition.End,
        content = {
            it.calculateBottomPadding()
            VehicleScreen(auto)
        }
    )
}