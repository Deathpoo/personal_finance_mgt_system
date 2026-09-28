package com.example.pfsm

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.pfsm.ui.theme.design.PFSMTheme
import com.example.pfsm.ui.theme.pages.AppTopBar
import com.example.pfsm.ui.theme.pages.BottomAppBar
import com.example.pfsm.ui.theme.pages.HomeContentPage
import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.example.pfsm.ui.theme.navigation.PFSMNavGraph
import com.example.pfsm.ui.theme.pages.ProfileScreen



@Composable
fun PFSMApp() {
    val context = LocalContext.current
    val app = context.applicationContext as PFSMApplication
    val themeMode by app.container.settingsRepository.themeMode.collectAsState(initial = "system")

    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    PFSMTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            PFSMNavGraph()
        }
    }
}

@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)

@Composable
fun PfsmAppPreview(){
    PFSMTheme {
        PFSMApp()
    }
}

