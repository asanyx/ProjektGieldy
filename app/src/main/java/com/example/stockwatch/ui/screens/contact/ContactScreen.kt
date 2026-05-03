package com.example.stockwatch.ui.screens.contact

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

private val GPW_LOCATION = LatLng(52.2324, 21.0108)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContactScreen() {
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(GPW_LOCATION, 15f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Mapa
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = locationPermission.status.isGranted
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = locationPermission.status.isGranted
            )
        ) {
            Marker(
                state = MarkerState(position = GPW_LOCATION),
                title = "Giełda Papierów Wartościowych",
                snippet = "ul. Książęca 4, Warszawa"
            )
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Giełda Papierów Wartościowych",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ul. Książęca 4, 00-498 Warszawa",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Przycisk WWW
            ContactButton(
                icon = Icons.Default.Language,
                label = "Odwiedź stronę GPW",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gpw.pl"))
                    runCatching { context.startActivity(intent) }
                        .onFailure {
                            Toast.makeText(context, "Brak aplikacji do otwierania stron", Toast.LENGTH_SHORT).show()
                        }
                }
            )

            // Przycisk telefon
            ContactButton(
                icon = Icons.Default.Phone,
                label = "Zadzwoń: +48 22 628 32 32",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+48226283232"))
                    runCatching { context.startActivity(intent) }
                        .onFailure {
                            Toast.makeText(context, "Brak aplikacji telefonu", Toast.LENGTH_SHORT).show()
                        }
                }
            )

            // Przycisk nawigacja
            ContactButton(
                icon = Icons.Default.Navigation,
                label = "Nawiguj do GPW",
                onClick = {
                    val uri = Uri.parse("geo:52.2324,21.0108?q=Giełda+Papierów+Wartościowych+Warszawa")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    runCatching { context.startActivity(intent) }
                        .onFailure {
                            Toast.makeText(context, "Brak aplikacji map", Toast.LENGTH_SHORT).show()
                        }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Przycisk lokalizacji użytkownika
            if (!locationPermission.status.isGranted) {
                OutlinedButton(
                    onClick = { locationPermission.launchPermissionRequest() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pokaż moją lokalizację na mapie")
                }
            }
        }
    }
}

@Composable
private fun ContactButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}
