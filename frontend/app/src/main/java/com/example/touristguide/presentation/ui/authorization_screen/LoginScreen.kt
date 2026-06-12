package com.example.touristguide.presentation.ui.authorization_screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    viewModel: AuthorizationViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.clearFields()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Вход",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 32.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        AuthTextField(
            label = "Логин",
            value = viewModel.login,
            onValueChange = viewModel::onLoginChange
        )

        AuthTextField(
            label = "Пароль",
            value = viewModel.password,
            onValueChange = viewModel::onPasswordChange,
            isPassword = true,
            modifier = Modifier.padding(top = 20.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.login(
                    onSuccess = onLoginSuccess,
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2B2B2B),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Войти",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }

        Text(
            text = "Зарегистрироваться",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
                .clickable(onClick = onRegisterClick),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF2196F3),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun RegisterScreen(
    viewModel: AuthorizationViewModel,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.clearFields()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Регистрация",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 32.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        AuthTextField(
            label = "Логин",
            value = viewModel.login,
            onValueChange = viewModel::onLoginChange
        )

        AuthTextField(
            label = "Пароль",
            value = viewModel.password,
            onValueChange = viewModel::onPasswordChange,
            isPassword = true,
            modifier = Modifier.padding(top = 20.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.register(
                    onSuccess = onRegisterSuccess,
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2B2B2B),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Зарегистрироваться",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (isPassword) {
                PasswordVisualTransformation()
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
            },
            keyboardOptions = if (isPassword) {
                KeyboardOptions(keyboardType = KeyboardType.Password)
            } else {
                KeyboardOptions(keyboardType = KeyboardType.Text)
            }
        )
    }
}
