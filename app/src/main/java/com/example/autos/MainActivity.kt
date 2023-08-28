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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.autos.ui.maintenance.GastoScreen
import com.example.autos.ui.maintenance.GastosViewModel
import com.example.autos.ui.maintenance.MantenimientosScreen
import com.example.autos.ui.maintenance.NewGastoScreen
import com.example.autos.ui.maintenance.NewItemsScreen
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

const val IVA = 21

private const val TAG = "xxMa"

var autoId = -1

class MainActivity : ComponentActivity(),
        OnSharedPreferenceChangeListener
{

    private lateinit var preferences: SharedPreferences

    private lateinit var viewModel: MainActivityViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = getSharedPreferences("preferences",Context.MODE_PRIVATE)
        preferences.registerOnSharedPreferenceChangeListener(this)
        autoId = preferences.getInt("auto_id", -1)

        viewModel = ViewModelProvider(this, MainActivityViewModel.Factory)[MainActivityViewModel::class.java]


        setContent {
            AutosTheme {
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
//                    Log.d(TAG,"preferences change autoId: $actualAutoId")
                    autoId = actualAutoId
                    Log.d(TAG,"new auto Id: $autoId")
//                    viewModel.getActualAutoId()
                    viewModel.refreshData()
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    val routes = listOf("home","newCar","vehiculos","refueling","historico","statistics","mantenimiento","newGasto")
    val selectedRoute = rememberSaveable { mutableStateOf(routes[0]) }

    val restoreFromBackUpFile = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            if (it != null) {
                if (viewModel.rebuildData(it)){
                    val msg = context.getString(R.string.restored_cars).format(viewModel.restoredVehicles)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    navController.navigate("home")
                    selectedRoute.value = routes[0]
//                    Log.d(TAG,"going to vehiculos")
//                    navController.navigate("vehiculos")
                }
            }
        }
    )

    val backupToFile = rememberLauncherForActivityResult(
        contract = CreateFileContract(),
        onResult = { uri ->

            if (uri != null) {
                val ok = viewModel.editFile(uri)
                if (ok) {
                    val msg = context.getString(R.string.saved_cars).format(viewModel.cars.size)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, R.string.saving_error, Toast.LENGTH_LONG).show()
                }
            } else  Log.d(TAG,"Backup cancelado")
        }
    )

    AutosTheme {
        val coroutineScope = rememberCoroutineScope()
        var gastosViewModel: GastosViewModel

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
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                val lifecycleOwner = LocalLifecycleOwner.current
                                Column(Modifier.fillMaxWidth(0.55f)) {
                                    DrawerHeader()
                                    DrawerBody(
                                        firstStart = viewModel.firstStart.value,
                                        navController = navController,
                                        selectedItem = selectedRoute,
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
                            }
                        },
                        modifier = Modifier
//                            .background(MaterialTheme.colorScheme.background)
                            .padding(it)
                            .wrapContentWidth()
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination =  if (viewModel.firstStart.value) {
                                                    RestoreDialog(
                                                        show = true,
                                                        onAccept = { restoreFromBackUpFile.launch(MIME_TYPE) }
                                                    )
                                                    selectedRoute.value = routes[1]
                                                    "newCar"
                                                } else {
                                                    "home"
                                                }
                        ) {
                            composable("home") {
                                selectedRoute.value = routes[0]
                                HomeScreen(auto, navController)
                                appBarTitle.value = stringResource(id = R.string.vehicle)
                            }
                            composable("newCar") {
                                selectedRoute.value = routes[1]
                                val newCarViewModel: NewCarViewModel = viewModel(factory = NewCarViewModel.Factory)
                                NewAutoScreen(
                                    newCarViewModel,
                                    navController
                                )
                                appBarTitle.value = stringResource(id = R.string.new_car)
                            }
                            composable("vehiculos") {
                                selectedRoute.value = routes[2]
                                val vehiculosViewModel: VehiculosViewModel = viewModel(factory = VehiculosViewModel.Factory)
                                VehiclesScreen(
                                    list = vehiculosViewModel.vehicles.observeAsState(),
                                    goToNewCar = { navController.navigate("newCar") },
                                    setAutoId = { autoId ->
                                        vehiculosViewModel.setAutoId(autoId)
                                        navController.navigate("home") {
                                            // si cambia el coche se elimina el stacK
                                            popUpTo("home")
                                        }
                                    }
                                )
                                appBarTitle.value = stringResource(id = R.string.vehicles)
                            }
                            composable("refueling") {
                                selectedRoute.value = routes[3]
                                val refuelingViewModel: RefuelingViewModel = viewModel(factory = RefuelingViewModel.Factory)
                                refuelingViewModel.initKms = auto?.initKms ?: 0
                                RefuelingScreen(
                                    viewModel = refuelingViewModel,
                                    lastRefueling = lastRefueling,
                                    onNewRefueling = {
                                        viewModel.refreshData()
                                        selectedRoute.value = routes[0]
                                        navController.navigate("home")
                                    }
                                )
                                appBarTitle.value = stringResource(id = R.string.new_repostaje)
                            }
                            composable("historico") {
                                selectedRoute.value = routes[4]
                                val refuelingViewModel: RefuelingViewModel = viewModel(factory = RefuelingViewModel.Factory)
                                LaunchedEffect( Unit ){
                                    refuelingViewModel.initKms = auto?.initKms ?: 0
                                    refuelingViewModel.getRespostajes()
                                }

                                RepostajesScreen(
                                    list = refuelingViewModel.repostajes.observeAsState(),
                                    status = refuelingViewModel.status
                                )
                                appBarTitle.value = stringResource(id = R.string.historico)
                            }
                            composable("statistics") {
                                selectedRoute.value = routes[5]
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
                            composable("newGasto") {
                                // para poder compartir la misma instancia de viewmodel pasar como propietario a la actividad
                                gastosViewModel = viewModel(
                                    viewModelStoreOwner = LocalContext.current as ComponentActivity,
                                    factory = GastosViewModel.Factory
                                )
                                NewGastoScreen(
                                    viewModel = gastosViewModel,
                                    lastKms = lastRefueling?.kms ?: auto!!.initKms,
                                    onNewGasto = { navController.navigate("newItems") }
                                )
                                appBarTitle.value = stringResource(id = R.string.mantenimiento)
                            }
                            composable("newItems") {
//                                Log.d(TAG,"entrando composable newItems")
                                // para poder compartir la misma instancia de viewmodel pasar como propietario a la actividad
                                gastosViewModel = viewModel(
                                    viewModelStoreOwner = LocalContext.current as ComponentActivity,
                                    factory = GastosViewModel.Factory
                                )
                                NewItemsScreen(
                                    viewModel = gastosViewModel,
                                    navController = navController
                                )
//                                appBarTitle.value = stringResource(id = R.string.mantenimiento)
                            }
                            composable("gasto") {
//                                Log.d(TAG,"entrando composable gasto")
                                // para poder compartir la misma instancia de viewmodel pasar como propietario a la actividad
                                gastosViewModel = viewModel(
                                    viewModelStoreOwner = LocalContext.current as ComponentActivity,
                                    factory = GastosViewModel.Factory
                                )
                                GastoScreen(
                                    gasto = gastosViewModel.makeGasto(),
                                    onSave = { save ->
                                        if (save){
                                            val result = gastosViewModel.saveGasto()
                                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                            navController.navigate("home")
                                        } else {
                                            navController.navigate("newItems") /*{
                                                popUpTo("newItems") { inclusive = true }
                                            }*/
                                        }
                                    }
                                )
//                                appBarTitle.value = stringResource(id = R.string.mantenimiento)
                            }
                            composable("mantenimiento") {
                                selectedRoute.value = routes[6]
//                                Log.d(TAG,"entrando composable mantenimiento")
                                // para poder compartir la misma instancia de viewmodel pasar como propietario a la actividad
                                gastosViewModel = viewModel(
                                    viewModelStoreOwner = LocalContext.current as ComponentActivity,
                                    factory = GastosViewModel.Factory
                                )

                                LaunchedEffect( Unit ) {
                                    gastosViewModel.getGastosByAuto(autoId)
                                }

                                MantenimientosScreen(
                                    list = gastosViewModel.search.observeAsState(),
                                    status = gastosViewModel.status,
                                    lastKms = gastosViewModel.lastKms.observeAsState()
                                ){
                                    gastosViewModel.getSearch(it)
                                }
                                appBarTitle.value = stringResource(id = R.string.mantenimiento)
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
fun DrawerBody(
    navController: NavController,
    firstStart: Boolean,
    selectedItem: MutableState<String>,
    onBackUp: () -> Unit,
    onRestore: () -> Unit,
    closeNavDrawer: () -> Unit
) {
    val scrollState = rememberScrollState()
    //                       0      1         2           3           4           5               6           7
//    val routes = listOf("home","newCar","vehiculos","refueling","historico","statistics","mantenimiento","newGasto")

    Column(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxHeight()
            .verticalScroll(scrollState)
    ) {
        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.vehicle)) },
            selected = selectedItem.value == "home",
            onClick = {
                closeNavDrawer()
                if (!firstStart)
                    navController.navigate(route = "home"){
                        popUpTo("home")
                    }
            },
            icon = { Icon(
                painterResource(id = R.drawable.twotone_selected_car_24),
                stringResource(id = R.string.vehicle)
            )}
        )

        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.new_car)) },
            selected = selectedItem.value == "newCar",
            onClick = {
                closeNavDrawer()
                navController.navigate(route = "newCar")
            },
            modifier = Modifier,
            icon = { Icon(
                painterResource(id = R.drawable.baseline_new_car_24),
                stringResource(id = R.string.new_car)
            )}
        )

        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.vehicles)) },
            selected = selectedItem.value == "vehiculos",
            onClick = {
                closeNavDrawer()
                if (!firstStart) {
                    navController.navigate(route = "vehiculos")
                }
            },
            modifier = Modifier,
            icon = { Icon(
                painterResource(id = R.drawable.twotone_commute_24),
                stringResource(id = R.string.vehicles)
            )}
        )

        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.new_repostaje)) },
            selected = selectedItem.value == "refueling",
            onClick = {
                closeNavDrawer()
                if (!firstStart) {
                    navController.navigate(route = "refueling")
                }
            },
            modifier = Modifier,
            icon = { Icon(
                painterResource(id = R.drawable.twotone_local_gas_station_24),
                stringResource(id = R.string.new_repostaje)
            )}
        )

        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.historico)) },
            selected = selectedItem.value == "historico",
            onClick = {
                closeNavDrawer()
                if (!firstStart) {
                    navController.navigate(route = "historico")
                }
            },
            modifier = Modifier,
            icon = { Icon(
                painterResource(id = R.drawable.twotone_local_gas_station_24),
                stringResource(id = R.string.historico)
            )}
        )

        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.statistics)) },
            selected = selectedItem.value == "statistics",
            onClick = {
                closeNavDrawer()
                if (!firstStart) {
                    navController.navigate(route = "statistics")
                }
            },
            modifier = Modifier,
            icon = { Icon(
                painterResource(id = R.drawable.twotone_data_exploration_24),
                stringResource(id = R.string.statistics)
            )}
        )

        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.historico)) },
            selected = selectedItem.value == "mantenimiento",
            onClick = {
                closeNavDrawer()
                if (!firstStart) {
                    navController.navigate(route = "mantenimiento")
                }
            },
            modifier = Modifier,
            icon = { Icon(
                painterResource(id = android.R.drawable.ic_menu_manage),
                stringResource(id = R.string.historico)
            )}
        )
        Divider(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            thickness = 2.dp
        )
        Divider(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 2.dp),
            thickness = 2.dp
        )

        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.backup)) },
            selected = false,
            onClick = {
                closeNavDrawer()
                if (!firstStart)
                    onBackUp()
            },
            modifier = Modifier,
            icon = { Icon(
                painterResource(id = R.drawable.twotone_backup_24),
                stringResource(id = R.string.backup)
            )}
        )

        NavigationDrawerItem(
            label = { Text(text = stringResource(id = R.string.restore)) },
            selected = false,
            onClick = {
                closeNavDrawer()
                onRestore()
            },
            modifier = Modifier,
            icon = { Icon(
                painterResource(id = R.drawable.twotone_restore_page_24),
                stringResource(id = R.string.restore)
            )}
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