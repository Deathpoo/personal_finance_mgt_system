package com.example.pfsm.ui.theme.design


import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
fun Modifier.responsiveWidth(): Modifier = this.widthIn(max = 640.dp)