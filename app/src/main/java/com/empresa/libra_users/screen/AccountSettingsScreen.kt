package com.empresa.libra_users.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.empresa.libra_users.viewmodel.ActiveLoanDetails
import com.empresa.libra_users.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(vm: MainViewModel) {
    val updateUserState by vm.updateUserState.collectAsStateWithLifecycle()
    val activeLoans by vm.activeLoans.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showImageSourceSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> vm.onUpdateUserProfileImageChange(uri) }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempCameraUri?.let { vm.onUpdateUserProfileImageChange(it) }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val newUri = createImageUri(context)
                tempCameraUri = newUri
                cameraLauncher.launch(newUri)
            } else {
                showPermissionRationaleDialog = true
            }
        }
    )

    LaunchedEffect(Unit) {
        vm.loadCurrentUserData()
        vm.loadActiveLoans()
    }

    LaunchedEffect(updateUserState.success) {
        if (updateUserState.success) {
            scope.launch {
                snackbarHostState.showSnackbar("¡Datos actualizados con éxito!")
            }
            vm.clearUpdateUserState()
        }
    }
    LaunchedEffect(updateUserState.errorMsg) {
        updateUserState.errorMsg?.let {
            scope.launch { snackbarHostState.showSnackbar("Error: $it") }
            vm.clearUpdateUserState()
        }
    }

    if (showImageSourceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false },
            sheetState = sheetState
        ) {
            ListItem(
                headlineContent = { Text("Desde la Galería") },
                leadingContent = { Icon(Icons.Default.PhotoLibrary, "Galería") },
                modifier = Modifier.clickable {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showImageSourceSheet = false
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    }
                }
            )
            ListItem(
                headlineContent = { Text("Tomar Foto") },
                leadingContent = { Icon(Icons.Default.CameraAlt, "Cámara") },
                modifier = Modifier.clickable {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showImageSourceSheet = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showPermissionRationaleDialog) {
        PermissionRationaleDialog(
            onDismiss = { showPermissionRationaleDialog = false },
            onConfirm = { openAppSettings(context) }
        )
    }

    if (updateUserState.showVerificationDialog) {
        VerificationDialog(vm = vm, onDismiss = { vm.cancelEmailUpdate() })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text("Configuración de la cuenta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showImageSourceSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (updateUserState.profileImageUri != null) {
                        AsyncImage(
                            model = updateUserState.profileImageUri?.toUri(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Añadir foto de perfil",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = { showImageSourceSheet = true }) {
                    Text("Cambiar foto")
                }
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = updateUserState.name,
                    onValueChange = vm::onUpdateUserNameChange,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = updateUserState.phone,
                    onValueChange = vm::onUpdateUserPhoneChange,
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = updateUserState.email,
                        onValueChange = vm::onUpdateUserEmailChange,
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.weight(1f),
                        enabled = !updateUserState.showVerificationDialog
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { vm.initiateEmailUpdate() }) {
                        Text("Cambiar")
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { vm.updateUser() },
                    enabled = !updateUserState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (updateUserState.isSubmitting) "Guardando..." else "Guardar Cambios")
                }

                Spacer(Modifier.height(32.dp))
                Divider()
                Spacer(Modifier.height(16.dp))
                Text("Mis Préstamos Activos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
            }

            if (activeLoans.isEmpty()) {
                item {
                    Text(
                        "No tienes préstamos activos en este momento.",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(activeLoans) { loanDetails ->
                    ActiveLoanCard(loanDetails = loanDetails)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ActiveLoanCard(loanDetails: ActiveLoanDetails) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dueDate = try {
        LocalDate.parse(loanDetails.loan.dueDate, formatter)
    } catch (e: Exception) {
        LocalDate.now() // Fallback
    }
    val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), dueDate)
    val isOverdue = daysRemaining < 0

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = loanDetails.book.coverUrl,
                    contentDescription = loanDetails.book.title,
                    modifier = Modifier.width(60.dp).aspectRatio(0.7f).clip(MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(loanDetails.book.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Devolver el: ${loanDetails.loan.dueDate}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    if (isOverdue) {
                        Text(
                            "VENCIDO",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "Te quedan $daysRemaining días",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (isOverdue) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Este préstamo está vencido. Tienes un plazo de 48 horas para devolverlo o se aplicará una multa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}


@Composable
private fun PermissionRationaleDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permiso Requerido") },
        text = { Text("Para tomar una foto de perfil, es necesario que concedas el permiso de acceso a la cámara. Por favor, actívalo en la configuración de la aplicación.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Ir a Configuración")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

@Composable
private fun VerificationDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    val updateUserState by vm.updateUserState.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verificar correo electrónico") },
        text = {
            Column {
                Text("Se ha enviado un código de verificación a su correo. Por favor, introdúzcalo a continuación.")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = updateUserState.verificationCode,
                    onValueChange = { vm.onVerificationCodeChange(it) },
                    label = { Text("Código de Verificación") },
                    isError = updateUserState.verificationError != null,
                    singleLine = true
                )
                if (updateUserState.verificationError != null) {
                    Text(
                        text = updateUserState.verificationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { vm.confirmEmailUpdate() },
                enabled = !updateUserState.isVerifying
            ) {
                if (updateUserState.isVerifying) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun createImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}
