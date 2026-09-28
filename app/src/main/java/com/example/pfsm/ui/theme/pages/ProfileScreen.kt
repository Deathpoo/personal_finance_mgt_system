package com.example.pfsm.ui.theme.pages



import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pfsm.ui.theme.design.FinanceColors
import com.example.pfsm.ui.theme.design.PFSMTheme
import com.example.pfsm.ui.theme.design.responsiveWidth
import com.example.pfsm.ui.theme.util.Atma
import com.example.pfsm.ui.theme.util.StackSansNotch
import com.example.pfsm.ui.theme.util.toReadableAmount
import com.example.pfsm.viewModels.AppViewModelProvider
import com.example.pfsm.viewModels.ProfileViewModel







@Composable
fun ProfileScreen(
    padding: PaddingValues,
    profileViewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onManageCategories: () -> Unit = {},
    onBack: () -> Unit = {},
    onSignedOut: () -> Unit = {}
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val colors = FinanceColors

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showDailyLimitDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    if (uiState.isSignedOut) {
        onSignedOut()
        return
    }

    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            profileViewModel.updateProfilePic(it.toString())
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .responsiveWidth()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            }
            item {
                ProfileHeader(
                    name = uiState.user?.username ?: "",
                    email = uiState.user?.email ?: "",
                    profilePicUri = uiState.user?.profilePicUri,
                    onEditPic = {imagePicker.launch(arrayOf("image/*")) },
                    onEditName = { showEditNameDialog = true }
                )
            }

            item { SectionLabel("Preferences") }
            item {
                SettingsRow(
                    icon = Icons.Default.Payments,
                    label = "Daily spending limit",
                    value = if (uiState.currentDailyLimit > 0) "₹${uiState.currentDailyLimit.toReadableAmount()}" else "Not set",
                    onClick = { showDailyLimitDialog = true }
                )
            }

            item {
                SettingsRow(
                    icon = Icons.Default.Palette,
                    label = "App theme",
                    value = uiState.themeMode.replaceFirstChar { it.uppercase() },
                    onClick = { showThemeDialog = true }
                )
            }

            item { SectionLabel("Organize") }
            item {
                SettingsRow(
                    icon = Icons.Default.Category,
                    label = "Manage categories",
                    value = null,
                    onClick = onManageCategories
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSignOutConfirm = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.Expense.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = colors.Expense)
                        Spacer(Modifier.width(10.dp))
                        Text("Sign out", color = colors.Expense, fontWeight = FontWeight.Medium)
                    }
                }
            }
            item {
                QuoteCard()
            }
        }
    }


    if (showEditNameDialog) {
        EditNameDialog(
            currentName = uiState.user?.username ?: "",
            onDismiss = { showEditNameDialog = false },
            onSave = { newName ->
                profileViewModel.updateName(newName)
                showEditNameDialog = false
            }
        )
    }

    if (showDailyLimitDialog) {
        EditDailyLimitDialog(
            currentLimit = uiState.currentDailyLimit,
            onDismiss = { showDailyLimitDialog = false },
            onSave = { newLimit ->
                profileViewModel.updateDailyLimit(newLimit)
                showDailyLimitDialog = false
            }
        )
    }


    if (showThemeDialog) {
        SingleChoiceDialog(
            title = "App theme",
            options = listOf("system", "light", "dark"),
            selected = uiState.themeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = {
                profileViewModel.updateThemeMode(it)
                showThemeDialog = false
            },
            displayTransform = { it.replaceFirstChar { c -> c.uppercase() } }
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Sign out?") },
            text = { Text("You'll need to sign in again to access your data.") },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutConfirm = false
                    profileViewModel.signOut()
                }) { Text("Sign out", color = FinanceColors.Expense) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    name: String,
    email: String,
    profilePicUri: String?,
    onEditPic: () -> Unit,
    onEditName: () -> Unit
) {
    val colors = FinanceColors

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(colors.Gold.copy(alpha = 0.15f))
                    .clickable { onEditPic() },
                contentAlignment = Alignment.Center
            ) {
                if (profilePicUri != null) {
                    AsyncImage(
                        model = profilePicUri,
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.Gold,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.Gold)
                    .clickable { onEditPic() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Change photo",
                    tint = colors.Ink,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(name.ifBlank { "Your name" }, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit name",
                modifier = Modifier.size(16.dp).clickable { onEditName() },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(email, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Text(label, modifier = Modifier.weight(1f), fontSize = 15.sp)
            if (value != null) {
                Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditDailyLimitDialog(currentLimit: Double, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var text by remember { mutableStateOf(if (currentLimit > 0) currentLimit.toInt().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily spending limit") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 400.dp)   // caps height so it never exceeds available space
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        text = input.filterIndexed { index, c ->
                            c.isDigit() || (c == '.' && input.indexOf('.') == index)
                        }
                    },
                    label = { Text("Amount") },
                    leadingIcon = { Text("₹") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "This applies from today onward. Past dates keep whichever limit was active on them at the time.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amount = text.toDoubleOrNull()
                if (amount != null && amount > 0) onSave(amount)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditNameDialog(currentName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit name") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onSave(text.trim()) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SingleChoiceDialog(
    title: String,
    options: List<String>,
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    displayTransform: (String) -> String = { it }
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 400.dp)
            ) {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = option == selected, onClick = { onSelect(option) })
                        Spacer(Modifier.width(8.dp))
                        Text(displayTransform(option))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}



@Composable
private fun QuoteCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "\"",
            fontFamily = StackSansNotch,
            fontSize = 100.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFFD0D0D0),
        )

        Text(
            text = "don't  save  what  is  left  after  spending",
            fontFamily = Atma,
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 22.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            modifier = Modifier.offset(y = (-60).dp)
        )
        Text(
            text = "spend  what  is  left  after  saving",
            fontFamily = Atma,
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 22.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            modifier = Modifier.offset(y = (-57).dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileHeaderPreview(){
    PFSMTheme {
        ProfileHeader(
            "loki",
            "loki@gmail.com",
            null,
            {},
            {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsRowPreview(){
    PFSMTheme {
        SettingsRow(
            Icons.Default.Palette,
            "App Theme",
            "Light",
            {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun QuoteCardPreview(){
    PFSMTheme {
        QuoteCard()
    }
}