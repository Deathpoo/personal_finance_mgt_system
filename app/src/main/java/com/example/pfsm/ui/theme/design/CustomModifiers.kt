package com.example.pfsm.ui.theme.design


import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.dp


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Modifier.onFocusLost(onLost: () -> Unit): Modifier {
    var wasFocused by remember { mutableStateOf(false) }
    return this.onFocusChanged { state ->
        if (wasFocused && !state.isFocused) onLost()
        wasFocused = state.isFocused
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.bringIntoViewOnFocus(): Modifier {
    val requester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    return this
        .bringIntoViewRequester(requester)
        .onFocusEvent { state ->
            if (state.isFocused) {
                scope.launch {
                    delay(300)
                    requester.bringIntoView()
                }
            }
        }
}


fun Modifier.responsiveWidth(): Modifier = this.widthIn(max = 640.dp)