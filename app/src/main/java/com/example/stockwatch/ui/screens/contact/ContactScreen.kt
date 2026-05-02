package com.example.stockwatch.ui.screens.contact

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

private val GPW_LOCATION = LatLng(52.2324, 21.0108)

@Composable
fun ContactScreen() {
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(GPW_LOCATION, 15f)
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Kontakt") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(myLocationButtonEnabled = true)
            ) {
                Marker(
                    state = MarkerState(position = GPW_LOCATION),
                    title = "Giełda Papierów Wartościowych",
                    snippet = "ul. Książęca 4, Warszawa"
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ContactItem(
                    icon = Icons.Default.Language,
                    label = "Strona WWW",
                    value = "www.gpw.pl"
                ) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gpw.pl"))
                    runCatching { context.startActivity(intent) }
                        .onFailure { Toast.makeText(context, "Brak przeglądarki", Toast.LENGTH_SHORT).show() }
                }
                ContactItem(
                    icon = Icons.Default.Phone,
                    label = "Telefon",
                    value = "+48 22 628 32 32"
                ) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+48226283232"))
                    runCatching { context.startActivity(intent) }
                        .onFailure { Toast.makeText(context, "Brak aplikacji telefonu", Toast.LENGTH_SHORT).show() }
                }
                ContactItem(
                    icon = Icons.Default.Map,
                    label = "Nawigacja",
                    value = "ul. Książęca 4, Warszawa"
                ) {
                    val uri = Uri.parse("geo:52.2324,21.0108?q=Giełda+Papierów+Wartościowych+Warszawa")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    runCatching { context.startActivity(intent) }
                        .onFailure { Toast.makeText(context, "Brak aplikacji map", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }
}

@Composable
fun ContactItem(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall)
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
