package com.sinc.mobile.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sinc.mobile.app.features.home.MainScreenRoutes
import com.sinc.mobile.app.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Sidebar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onExternalNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            SidebarSection(title = "Principal")
            SidebarItem(
                icon = Icons.Outlined.Home,
                text = "Inicio",
                isSelected = currentRoute == MainScreenRoutes.DASHBOARD,
                onClick = {
                    onNavigate(MainScreenRoutes.DASHBOARD)
                    onCloseDrawer()
                }
            )
            SidebarItem(
                icon = Icons.Outlined.Person,
                text = "Mi Perfil",
                isSelected = false,
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SidebarSection(title = "Gestión Productiva")
            SidebarItem(
                icon = Icons.Outlined.MenuBook,
                text = "Cuaderno de Campo",
                isSelected = currentRoute == MainScreenRoutes.MOVIMIENTO,
                onClick = {
                    onNavigate(MainScreenRoutes.MOVIMIENTO)
                    onCloseDrawer()
                }
            )
            SidebarItem(
                icon = Icons.Outlined.Archive,
                text = "Mi Stock",
                isSelected = false,
                onClick = { /* TODO */ }
            )
            SidebarItem(
                icon = Icons.Outlined.LocationOn,
                text = "Mis Campos",
                isSelected = currentRoute == MainScreenRoutes.CAMPOS,
                onClick = {
                    onNavigate(MainScreenRoutes.CAMPOS)
                    onCloseDrawer()
                }
            )
            SidebarItem(
                icon = Icons.Outlined.AddCircleOutline,
                text = "Registrar Campo",
                isSelected = false,
                onClick = {
                    onExternalNavigate(Routes.CREATE_UNIDAD_PRODUCTIVA)
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SidebarSection(title = "Análisis y Datos")
            SidebarItem(
                icon = Icons.Outlined.BarChart,
                text = "Estadísticas",
                isSelected = false,
                onClick = { /* TODO */ }
            )
            SidebarItem(
                icon = Icons.Outlined.Article,
                text = "Reportes",
                isSelected = false,
                onClick = { /* TODO */ }
            )
        }
    }
}