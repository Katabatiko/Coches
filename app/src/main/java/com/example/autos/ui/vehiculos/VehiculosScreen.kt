package com.example.autos.ui.vehiculos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.autos.R
import com.example.autos.domain.DomainCoche
import com.example.autos.ui.composables.Dato
import com.example.autos.util.localNumberFormat


@Composable
fun AutoItem(
    auto: DomainCoche,
    setAutoId: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable {
                setAutoId(auto.id)
            }
    ) {
        Text(
            text = auto.modelo,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Dato(
                label = stringResource(id = R.string.matricula),
                value = auto.matricula,
                modifier = Modifier.weight(1f),
                labelModifier = Modifier.padding(end = 6.dp)
            )
            Dato(
                value = localNumberFormat(auto.actualKms),
                label = stringResource(id = R.string.kilometros),
                modifier = Modifier.weight(1f),
                labelModifier = Modifier.padding(end = 6.dp)
            )
        }
    }
}

@Composable
fun VehiclesScreen(
    list: State<List<DomainCoche>?>,
    goToNewCar: () -> Unit,
    setAutoId: (Int) -> Unit
) {

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    goToNewCar()
                }
            ){
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_input_add),
                    contentDescription = stringResource(R.string.add_vehicle)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        content = {
            it.calculateBottomPadding()
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ){
                if (!list.value.isNullOrEmpty()){
                    items (list.value!!) { item ->
                        AutoItem(
                            auto = item,
                            setAutoId = { autoId -> setAutoId(autoId) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun PreviewItem() {
    val auto = DomainCoche(
        initKms = 221423,
        buyDate = "2013/09/26",
        year = "2001/10/04",
        matricula = "9395BNM",
        marca = "Land Rover",
        modelo = "Freelander",
        id = 1,
        actualKms = 224556
    )

    AutoItem(
        auto = auto,
        setAutoId = {}
    )
}
