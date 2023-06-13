package com.example.autos

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.autos.data.local.AutosDatabase
import com.example.autos.databinding.ActivityMainBinding
import com.example.autos.repository.AutosRepository

enum class AutosStatus { LOADING, ERROR, DONE }

private const val CREATE_FILE =1
private const val PICK_DB_FILE = 7
private const val FILE_NAME = "AutosDataBackup.json"

private const val TAG = "xxMa"

private lateinit var drawerLayout: DrawerLayout

class MainActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        OnSharedPreferenceChangeListener
{

//    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var navView: NavigationView
    private lateinit var preferences: SharedPreferences
    private var firstStart = false

    val viewModel: MainActivityViewModel by lazy {
        val db = AutosDatabase.getDatabase(this)
        val repository = AutosRepository(db, this.application)
        val viewModelFactory = MainActivityViewModelFactory(this.application, repository)
        ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = getSharedPreferences("preferences",Context.MODE_PRIVATE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawerLayout
//        val toggle = ActionBarDrawerToggle(this, drawerLayout,R.string.open,R.string.close)

//        drawerLayout.addDrawerListener(DrawerToggle(this, drawerLayout, R.string.open, R.string.close))

        navView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val navController = navHostFragment!!.findNavController()
//        val navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(navView, navController)
        navView.setNavigationItemSelectedListener(this)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(
//            navController.graph/*setOf(
//                R.id.nav_vehicle, R.id.nav_newCar, R.id.nav_vehicles, R.id.nav_refueling, R.id.nav_repostajes
//            )*/, drawerLayout
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)

        val autoId = preferences.getInt("auto_id", -1)
        if (autoId == -1){
            firstStart = true
            preferences.registerOnSharedPreferenceChangeListener(this)
            disableMenuItems(true)
            showRestoreDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    //    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment)
//        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
//    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!firstStart) {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!firstStart) {
            val navController = findNavController(R.id.nav_host_fragment)
            return NavigationUI.navigateUp(navController, drawerLayout)
//            return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        }
        return true
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        when(item.itemId) {
            R.id.nav_vehicle -> navController.navigate(R.id.nav_vehicle)
            R.id.nav_newCar -> navController.navigate(R.id.nav_newCar)
            R.id.nav_vehicles -> navController.navigate(R.id.nav_vehicles)
            R.id.nav_refueling -> navController.navigate(R.id.nav_refueling)
            R.id.nav_repostajes -> navController.navigate(R.id.nav_repostajes)
            R.id.nav_statistics -> navController.navigate(R.id.nav_statistics)

            R.id.restore -> {
                restoreFromFile()
            }

            R.id.backup -> {
                //  openDirectory()
                viewModel.getData()
                val documentFolder =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
//                Log.d(TAG, "documentFolder: ${Uri.parse(documentFolder)}")
//                Log.d(TAG,"estado storage: ${Environment.getExternalStorageState()}")

                viewModel.datosRecibidos.observerOnce(this){
                    Log.d(TAG,"allDataReceived: ${it}")
                    if (it)
                        createFile(Uri.parse(documentFolder))
                }
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START,true)
        return true
    }


    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onActivityResult(requestCode, resultCode, data)",
        "androidx.appcompat.app.AppCompatActivity"
    )
    )
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val contentResolver = applicationContext.contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CREATE_FILE ->{
                    data?.data?.also { uri ->
                        Log.d(TAG,"uri: $uri")
                        contentResolver.takePersistableUriPermission(uri, takeFlags)

//                        Log.d(TAG, "copiando archivo AutosBackup.json")
                        var jsonString = viewModel.dataToJson(viewModel.cars as List<Any>, "Autos")
                        jsonString = jsonString.plus("|")
                        jsonString = jsonString.plus(viewModel.dataToJson(viewModel.refuelings as List<Any>, "Refuelings"))
                        val ok = viewModel.editFile(uri, jsonString)
                        if (ok) {
                            val msg = getString(R.string.saved_cars).format(viewModel.cars.size)
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        }
                        else {
                            Toast.makeText(this, R.string.saving_error, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                PICK_DB_FILE -> {
                    data?.data?.also { uri ->
                        Log.d(TAG,"data: $data")
                        viewModel.rebuildData(uri)
                    }
                    val msg = getString(R.string.restored_cars).format(viewModel.restoredVehicles)
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    findNavController(R.id.nav_host_fragment).navigate(R.id.nav_vehicles)
                    navView.menu.findItem(R.id.nav_vehicles).isEnabled = true
                    navView.menu.findItem(R.id.restore).isEnabled = false
                }
            }
        }else{
            Log.e(TAG, "result code no OK: $resultCode")
        }
    }

    private fun restoreFromFile(){
        val documentFolder = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        val uri = Uri.parse(documentFolder)
        openFile(uri)
    }

    private fun createFile(pickerInitialUri: Uri){
//        Log.d(TAG,"createFile pickerInitialUri: $pickerInitialUri")
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"

            putExtra(Intent.EXTRA_TITLE, FILE_NAME)

            // opcionalmente se puede especificar la URI del directorio inicial que abre el file picker
            // antes de crear el archivo. Para Api level >=26
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            }

        }

         startActivityForResult(intent, CREATE_FILE)
    }

    private fun openFile(pickerInitialUri: Uri){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
            }
        }
        startActivityForResult(intent, PICK_DB_FILE)
    }


    fun showRestoreDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.restore_dialog_msg))
            .setCancelable(false)
            .setPositiveButton(getString(android.R.string.ok)){ _, _ ->
                restoreFromFile()
            }
            .setNegativeButton(android.R.string.cancel){ dialog, _ ->
                dialog.dismiss()
//                showKeyboard(modelo)
            }
        builder.create().show()
//        viewModel.showedRestoreDialog()
    }




    private fun disableMenuItems(disable: Boolean){
        navView.menu.findItem(R.id.nav_vehicle).isEnabled = !disable
        navView.menu.findItem(R.id.nav_vehicles).isEnabled = !disable
        navView.menu.findItem(R.id.nav_refueling).isEnabled = !disable
        navView.menu.findItem(R.id.nav_repostajes).isEnabled = !disable
        navView.menu.findItem(R.id.nav_statistics).isEnabled = !disable
        navView.menu.findItem(R.id.backup).isEnabled = !disable
        if (!disable){
            navView.menu.findItem(R.id.restore).isEnabled = true
        }
    }

    // extensión para la auto eliminacion del observador despues de una observación
    private fun <T> LiveData<T>.observerOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        observe(lifecycleOwner, object: Observer<T> {
            override fun onChanged(t: T) {
                observer.onChanged(t)
                removeObserver(this)
            }

        })
    }

    /*private inner class DrawerToggle(
        activity: AppCompatActivity,
        drawerLayout: DrawerLayout,
        open: Int,
        close: Int
    ): ActionBarDrawerToggle(activity, drawerLayout, open, close) {
        override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            super.onDrawerSlide(drawerView, slideOffset)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (currentFocus != null)
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }*/

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(TAG,"change preference")
        if (key == "auto_id"){
            if (sharedPreferences != null) {
                if (sharedPreferences.getInt("auto_id", -1) != -1){
                    firstStart = false
                    disableMenuItems(false)
                    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
                }

            }
        }
    }
}