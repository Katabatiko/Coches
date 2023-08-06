package com.example.autos

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autos.ui.composables.RestoreDialog
import com.example.autos.ui.estadisticas.StatisticsScreen
import com.example.autos.ui.estadisticas.StatisticsViewModel
import com.example.autos.ui.home.HomeScreen
import com.example.autos.ui.newcar.NewAutoScreen
import com.example.autos.ui.newcar.NewCarViewModel
import com.example.autos.ui.refueling.RefuelingScreen
import com.example.autos.ui.refueling.RefuelingViewModel
import com.example.autos.ui.refueling.RepostajesScreen
import com.example.autos.ui.theme.AutosTheme
import com.example.autos.ui.vehiculos.VehiclesScreen
import com.example.autos.ui.vehiculos.VehiculosViewModel
import kotlinx.coroutines.launch


enum class AutosStatus { LOADING, ERROR, DONE }
enum class NumberType { INT, FLOAT2, FLOAT3}

private const val FILE_NAME = "AutosDataBackup.json"
private const val MIME_TYPE = "application/json"

private const val TAG = "xxMa"

class MainActivity : ComponentActivity(),
        OnSharedPreferenceChangeListener
{

    private lateinit var preferences: SharedPreferences

    var autoId = -1
    lateinit var viewModel: MainActivityViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = getSharedPreferences("preferences",Context.MODE_PRIVATE)
        preferences.registerOnSharedPreferenceChangeListener(this)

        viewModel = ViewModelProvider(this, MainActivityViewModel.Factory)[MainActivityViewModel::class.java]

        autoId = preferences.getInt("auto_id", -1)

        setContent {
            AutosTheme() {
                Main()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!viewModel.firstStart.value) {
            super.onBackPressed()
        }
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "auto_id"){
            if (sharedPreferences != null) {
                val actualAutoId = sharedPreferences.getInt("auto_id", -1)
                if (actualAutoId != -1){
//                    Log.d(TAG,"preferences change autoId: ${actualAutoId}")
                    autoId = actualAutoId
                    viewModel.getActualAutoId()
                    viewModel.refreshData()
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun Main(
    context: Context = LocalContext.current,
    navController: NavHostController = rememberNavController(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    viewModel: MainActivityViewModel = viewModel()
) {
    val appBarTitle = rememberSaveable { mutableStateOf("Autos") }

    val (auto, _) = rememberSaveable { viewModel.auto }

    val (lastRefueling, _) = rememberSaveable { viewModel.lastRefueling }

    val restoreFromBackUpFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            if (it != null) {
                if (viewModel.rebuildData(it)){
                    val msg = context.getString(R.string.restored_cars).format(viewModel.restoredVehicles)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
//                    Log.d(TAG,"going to vehiculos")
//                    navController.navigate("vehiculos")
                }
            }
        }
    )

    val backupToFile = rememberLauncherForActivityResult(
        contract = CreateFileContract(),
        onResult = { uri ->

            var jsonString = """{"Autos":"""
            jsonString = jsonString.plus(viewModel.dataToJson(viewModel.cars as List<Any>))
            jsonString = jsonString.plus(""","Refueling":""")
            jsonString = jsonString.plus(viewModel.dataToJson(viewModel.refuelings as List<Any>))
            jsonString = jsonString.plus("}")

            val ok = viewModel.editFile(uri!!, jsonString)
            if (ok) {
                val msg = context.getString(R.string.saved_cars).format(viewModel.cars.size)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(context, R.string.saving_error, Toast.LENGTH_LONG).show()
            }
        }
    )

    AutosTheme {
        val coroutineScope = rememberCoroutineScope()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text( text = appBarTitle.value )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                                    coroutineScope.launch {
                                        if (drawerState.isClosed)
                                            drawerState.open()
                                        else
                                            drawerState.close()
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = if (drawerState.isClosed)     Icons.Default.Menu
                                                else                        Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) {
                Surface {
                    ModalNavigationDrawer(
                        drawerContent = {
                            val lifecycleOwner = LocalLifecycleOwner.current
                            Column(Modifier.fillMaxWidth(0.55f)) {
                                DrawerHeader()
                                DrawerBody(
                                    firstStart = viewModel.firstStart.value,
                                    navController = navController,
                                    onBackUp = {
                                        viewModel.getData()
                                        viewModel.datosRecibidos.observe(lifecycleOwner){
                                            if (it) {
                                                backupToFile.launch(MIME_TYPE)
                                            } else {
                                                Toast.makeText(context, R.string.retrieving_error, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                               },
                                    onRestore = { restoreFromBackUpFile.launch(MIME_TYPE) }
                                ) {
                                    coroutineScope.launch {  drawerState.close()  }
                                }
                            }
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(it),
                        drawerState = drawerState,
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination =  if (viewModel.firstStart.value) {
                                                    RestoreDialog(
                                                        show = true,
                                                        onAccept = { restoreFromBackUpFile.launch(MIME_TYPE) }
                                                    )
                                                    "newCar"
                                                } else {
                                                    "home"
                                                }
                        ) {
                            composable("home") {
                                HomeScreen(auto, navController)
                                appBarTitle.value = stringResource(id = R.string.vehicle)
                            }
                            composable("newCar") {
                                val newCarViewModel: NewCarViewModel = viewModel(factory = NewCarViewModel.Factory)
                                NewAutoScreen(
                                    newCarViewModel,
                                    navController
                                )
                                appBarTitle.value = stringResource(id = R.string.new_car)
                            }
                            composable("vehiculos") {
                                val vehiculosViewModel: VehiculosViewModel = viewModel(factory = VehiculosViewModel.Factory)
                                VehiclesScreen(
                                    list = vehiculosViewModel.vehicles.observeAsState(),
                                    navController = navController,
                                    setAutoId = { autoId ->
                                        vehiculosViewModel.setAutoId(autoId)
                                    }
                                )
                                appBarTitle.value = stringResource(id = R.string.vehicles)
                            }
                            composable("refueling") {
                                val refuelingViewModel: RefuelingViewModel = viewModel(factory = RefuelingViewModel.Factory)
                                refuelingViewModel.initKms = auto?.initKms ?: 0
                                RefuelingScreen(
                                    viewModel = refuelingViewModel,
                                    autoId = auto?.id ?: 0,
                                    lastRefueling = lastRefueling,
                                    navController = navController,
                                    onNewRefueling = { viewModel.refreshData() }
                                )
                                appBarTitle.value = stringResource(id = R.string.new_repostaje)
                            }
                            composable("historico") {
                                val refuelingViewModel: RefuelingViewModel = viewModel(factory = RefuelingViewModel.Factory)
                                refuelingViewModel.initKms = auto ?.initKms ?: 0
                                RepostajesScreen(
                                    list = refuelingViewModel.repostajes.observeAsState(),
                                    onListReceived = {
                                        refuelingViewModel.setRecorridos()
                                    }
                                )
                                appBarTitle.value = stringResource(id = R.string.historico)
                            }
                            composable("statistics") {
                                val statisticsViewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory)
                                StatisticsScreen(
                                    viewModel = statisticsViewModel,
                                    autoModelo = auto?.modelo ?: "",
                                    autoInitKms = auto?.initKms ?: 0,
                                    autoLastKms = auto?.actualKms ?: 0,
                                    lastRefuelingLitros = lastRefueling?.litros ?: 0f
                                )
                                appBarTitle.value = stringResource(id = R.string.statistics)
                            }
                        }
                    }
                }
        }
    }
}

@Preview
@Composable
fun DrawerHeader(){
    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(vertical = 8.dp)
            .background(MaterialTheme.colorScheme.tertiary),
    ) {
        Icon(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = stringResource( id = R.string.nav_header_desc ),
            tint = MaterialTheme.colorScheme.onTertiary
        )
        Text(
            text = stringResource(id = R.string.nav_header_title),
            modifier = Modifier.padding(start = 8.dp),
            color = MaterialTheme.colorScheme.onTertiary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = stringResource(id = R.string.nav_header_subtitle),
            modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onTertiary,
            fontSize = 18.sp,
        )
    }
}

@Composable
private fun DrawerMenuItem(
    enable: Boolean,
    iconDrawableId: Int,
    text: String,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enable) { onItemClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ){
        Icon(
            painter = painterResource(iconDrawableId),
            contentDescription = text,
            tint =  if (enable)     MaterialTheme.colorScheme.onBackground
                    else            MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = if (enable)     MaterialTheme.colorScheme.onBackground
                    else            MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun DrawerBody(
    navController: NavController,
    firstStart: Boolean,
    onBackUp: () -> Unit,
    onRestore: () -> Unit,
    closeNavDrawer: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxHeight()
            .verticalScroll(scrollState)
    ) {
        DrawerMenuItem(
            enable = !firstStart,
            iconDrawableId = R.drawable.twotone_selected_car_24,
            text = stringResource(id = R.string.vehicle),
            onItemClick = {
                navController.navigate(route = "home")
                closeNavDrawer()
            }
        )
        DrawerMenuItem(
            enable = true,
            iconDrawableId = R.drawable.baseline_new_car_24,
            text = stringResource(id = R.string.new_car),
            onItemClick = {
                closeNavDrawer()
                navController.navigate(route = "newCar")
            }
        )
        DrawerMenuItem(
            enable = !firstStart,
            iconDrawableId = R.drawable.twotone_commute_24,
            text = stringResource(id = R.string.vehicles),
            onItemClick = {
                closeNavDrawer()
                navController.navigate(route = "vehiculos")
            }
        )
        DrawerMenuItem(
            enable = !firstStart,
            iconDrawableId = R.drawable.twotone_local_gas_station_24,
            text = stringResource(id = R.string.new_repostaje),
            onItemClick = {
                navController.navigate(route = "refueling")
                closeNavDrawer()
            }
        )
        DrawerMenuItem(
            enable = !firstStart,
            iconDrawableId = R.drawable.twotone_local_gas_station_24,
            text = stringResource(id = R.string.historico),
            onItemClick = {
                closeNavDrawer()
                navController.navigate(route = "historico")
            }
        )
        DrawerMenuItem(
            enable = !firstStart,
            iconDrawableId = R.drawable.twotone_data_exploration_24,
            text = stringResource(id = R.string.statistics),
            onItemClick = {
                closeNavDrawer()
                navController.navigate(route = "statistics")
            }
        )
        DrawerMenuItem(
            enable = !firstStart,
            iconDrawableId = android.R.drawable.ic_menu_manage,
            text = stringResource(id = R.string.mantenimiento),
            onItemClick = {
                closeNavDrawer()
//                navController.navigate(route = "statistics")
            }
        )
        Text(
            text = "----------------------------",
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.Start)
        )
        DrawerMenuItem(
            enable = !firstStart,
            iconDrawableId = R.drawable.twotone_backup_24,
            text = stringResource(id = R.string.backup),
            onItemClick = {
                closeNavDrawer()
                onBackUp()
            }
        )
        DrawerMenuItem(
            enable = true,
            iconDrawableId = R.drawable.twotone_restore_page_24,
            text = stringResource(id = R.string.restore),
            onItemClick = {
                closeNavDrawer()
                onRestore()
            }
        )
    }
}

class CreateFileContract: ActivityResultContracts.CreateDocument(MIME_TYPE) {
    override fun createIntent(context: Context, input: String): Intent {
        val intent = super.createIntent(context, input)
        intent.putExtra(Intent.EXTRA_TITLE, FILE_NAME)
        return intent
    }
}