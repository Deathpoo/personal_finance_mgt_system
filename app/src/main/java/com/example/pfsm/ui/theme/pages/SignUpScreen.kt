package com.example.pfsm.ui.theme.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pfsm.ui.theme.design.FinanceColors
import com.example.pfsm.ui.theme.design.bringIntoViewOnFocus
import com.example.pfsm.ui.theme.design.onFocusLost
import com.example.pfsm.viewModels.AppViewModelProvider
import com.example.pfsm.viewModels.SignUpViewModel

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onSignedUp: () -> Unit = {},
    onGoToSignIn: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FinanceColors
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onSignedUp()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create account", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(
                "Start tracking your money",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("Full name") },
                singleLine = true,
                isError = uiState.usernameError != null,
                supportingText = uiState.usernameError?.let { { Text(it, color = colors.Expense) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus()
                    .onFocusLost { viewModel.onUsernameFocusLost() }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Email") },
                singleLine = true,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { { Text(it, color = colors.Expense) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus()
                    .onFocusLost { viewModel.onEmailFocusLost() }
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Password") },
                singleLine = true,
                isError = uiState.passwordError != null,
                supportingText = {
                    Text(
                        uiState.passwordError ?: "6 characters: A-Z, a-z, 0-9 and a special character",
                        color = if (uiState.passwordError != null) colors.Expense else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        if (passwordVisible) "Hide" else "Show",
                        fontSize = 12.sp,
                        color = colors.Gold,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { passwordVisible = !passwordVisible }
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus()
                    .onFocusLost { viewModel.onPasswordFocusLost() },

            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChanged,
                label = { Text("Confirm password") },
                singleLine = true,
                isError = uiState.confirmPasswordError != null,
                supportingText = uiState.confirmPasswordError?.let { { Text(it, color = colors.Expense) } },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .bringIntoViewOnFocus()
                    .onFocusLost { viewModel.onConfirmPasswordFocusLost() }
            )

            uiState.generalError?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = colors.Expense, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.submit() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.Gold, contentColor = colors.Ink)
            ) {
                Text(if (uiState.isLoading) "Creating account..." else "Sign up", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Sign in",
                    fontSize = 13.sp,
                    color = colors.Gold,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onGoToSignIn() }
                )
            }
        }
    }
}