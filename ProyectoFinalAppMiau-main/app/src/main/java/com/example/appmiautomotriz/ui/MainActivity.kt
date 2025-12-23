package com.example.appmiautomotriz.ui

import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.appmiautomotriz.R

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar sesión
        val sharedPref = getSharedPreferences("AppSession", MODE_PRIVATE)
        if (!sharedPref.getBoolean("isLoggedIn", false)) {
            val intent = android.content.Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, 
                R.id.nav_finished_tasks, 
                R.id.nav_mis_facturas, 
                R.id.nav_mis_cotizaciones,
                R.id.nav_clientes,
                R.id.nav_agendamiento,
                R.id.nav_trash, 
                R.id.nav_inventario, 
                R.id.nav_historial_vehiculo,
                R.id.nav_reportes
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Lógica de Roles
        val rol = sharedPref.getString("role", "user")
        val menu = navView.menu

        // Aplicar Colores según Rol
        val colorRes = when (rol) {
            "mechanic" -> R.color.mechanic_primary
            "client" -> R.color.client_primary
            else -> R.color.admin_primary // Default Admin (Blue)
        }
        val colorInt = androidx.core.content.ContextCompat.getColor(this, colorRes)
        
        // 1. Cambiar color Toolbar
        toolbar.setBackgroundColor(colorInt)
        
        // 2. Cambiar color Status Bar
        window.statusBarColor = colorInt

        // 3. Cambiar color Header del Navigation Drawer (Si existe)
        val headerView = navView.getHeaderView(0)
        if (headerView != null) {
            headerView.setBackgroundColor(colorInt)
        }

        // Historial Vehículo: Deshabilitado para TODOS por redundancia
        menu.findItem(R.id.nav_historial_vehiculo).isVisible = false

        if (rol == "mechanic") {
            // Mecánico: Ocultar gestión administrativa personal del cliente
            menu.findItem(R.id.nav_mis_facturas).isVisible = false
            menu.findItem(R.id.nav_mis_cotizaciones).isVisible = false
            // Mecánico puede ver clientes, inventario, tareas, etc.
        } else if (rol == "client") {
            // Cliente: Ve sus órdenes y sus módulos
            menu.findItem(R.id.nav_home).isVisible = true // SÍ debe ver sus órdenes activas
            menu.findItem(R.id.nav_finished_tasks).isVisible = true // SÍ debe ver su historial (Finalizadas)
            
            menu.findItem(R.id.nav_clientes).isVisible = false
            menu.findItem(R.id.nav_trash).isVisible = false
            menu.findItem(R.id.nav_inventario).isVisible = false
            menu.findItem(R.id.nav_reportes).isVisible = false
            
            // Cliente ve: Home (Ordenes), Historial, Agendamiento, Mis Facturas...
            
            // Start destination: Home (Ordenes activas)
            val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
            navGraph.setStartDestination(R.id.nav_home)
            navController.graph = navGraph
        } else {
            // Admin (Default)
            // Ya ocultamos historial_vehiculo arriba
        }

        navView.setNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.nav_logout) {
                // Cerrar Sesión
                with(sharedPref.edit()) {
                    clear()
                    apply()
                }
                val intent = android.content.Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            } else if (item.itemId == R.id.nav_profile) {
                // Ir a Perfil
                val intent = android.content.Intent(this, PerfilActivity::class.java)
                startActivity(intent)
                drawerLayout.closeDrawers()
                true
            } else if (item.itemId == R.id.nav_reportes) {
                navController.navigate(R.id.nav_reportes)
                drawerLayout.closeDrawers()
                true
            } else {
                // Navegación normal
                androidx.navigation.ui.NavigationUI.onNavDestinationSelected(item, navController)
                drawerLayout.closeDrawers()
                true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}