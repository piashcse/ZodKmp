package com.piashcse.zodkmp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun MainView() {
    // TextField validation state
    var emailText by remember { mutableStateOf("") }
    var emailValidationResult by remember { mutableStateOf<ZodResult<String>?>(null) }
    var showEmailValidation by remember { mutableStateOf(false) }
    
    var passwordText by remember { mutableStateOf("") }
    var passwordValidationResult by remember { mutableStateOf<ZodResult<String>?>(null) }
    var showPasswordValidation by remember { mutableStateOf(false) }
    
    var nameText by remember { mutableStateOf("") }
    var nameValidationResult by remember { mutableStateOf<ZodResult<String>?>(null) }
    var showNameValidation by remember { mutableStateOf(false) }
    
    // Define validation schemas
    val emailSchema = Zod.string().email()
    val passwordSchema = Zod.string().min(8).max(50)
    val nameSchema = Zod.string().min(2).max(50)
    
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ZodKmp Validation Examples",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // TextField validation section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Real-time TextField Validation",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Email TextField with validation
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = { 
                            emailText = it
                            emailValidationResult = emailSchema.safeParse(it)
                            showEmailValidation = true
                        },
                        label = { Text("Email") },
                        isError = showEmailValidation && emailValidationResult is ZodResult.Failure,
                        supportingText = {
                            if (showEmailValidation) {
                                emailValidationResult?.let { result ->
                                    when (result) {
                                        is ZodResult.Success -> {
                                            Text(
                                                text = "✅ Valid email",
                                                color = Color.Green
                                            )
                                        }
                                        is ZodResult.Failure -> {
                                            Text(
                                                text = "❌ ${result.error.errors.joinToString(", ")}",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        trailingIcon = {
                            if (showEmailValidation && emailText.isNotEmpty()) {
                                emailValidationResult?.let { result ->
                                    when (result) {
                                        is ZodResult.Success -> {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Valid",
                                                tint = Color.Green
                                            )
                                        }
                                        is ZodResult.Failure -> {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Invalid",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Password TextField with validation
                    OutlinedTextField(
                        value = passwordText,
                        onValueChange = { 
                            passwordText = it
                            passwordValidationResult = passwordSchema.safeParse(it)
                            showPasswordValidation = true
                        },
                        label = { Text("Password (min 8 chars)") },
                        isError = showPasswordValidation && passwordValidationResult is ZodResult.Failure,
                        supportingText = {
                            if (showPasswordValidation) {
                                passwordValidationResult?.let { result ->
                                    when (result) {
                                        is ZodResult.Success -> {
                                            Text(
                                                text = "✅ Valid password",
                                                color = Color.Green
                                            )
                                        }
                                        is ZodResult.Failure -> {
                                            Text(
                                                text = "❌ ${result.error.errors.joinToString(", ")}",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        trailingIcon = {
                            if (showPasswordValidation && passwordText.isNotEmpty()) {
                                passwordValidationResult?.let { result ->
                                    when (result) {
                                        is ZodResult.Success -> {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Valid",
                                                tint = Color.Green
                                            )
                                        }
                                        is ZodResult.Failure -> {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Invalid",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Name TextField with validation
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { 
                            nameText = it
                            nameValidationResult = nameSchema.safeParse(it)
                            showNameValidation = true
                        },
                        label = { Text("Name (2-50 chars)") },
                        isError = showNameValidation && nameValidationResult is ZodResult.Failure,
                        supportingText = {
                            if (showNameValidation) {
                                nameValidationResult?.let { result ->
                                    when (result) {
                                        is ZodResult.Success -> {
                                            Text(
                                                text = "✅ Valid name",
                                                color = Color.Green
                                            )
                                        }
                                        is ZodResult.Failure -> {
                                            Text(
                                                text = "❌ ${result.error.errors.joinToString(", ")}",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        trailingIcon = {
                            if (showNameValidation && nameText.isNotEmpty()) {
                                nameValidationResult?.let { result ->
                                    when (result) {
                                        is ZodResult.Success -> {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Valid",
                                                tint = Color.Green
                                            )
                                        }
                                        is ZodResult.Failure -> {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Invalid",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Clear validation button
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { 
                            emailText = ""
                            passwordText = ""
                            nameText = ""
                            showEmailValidation = false
                            showPasswordValidation = false
                            showNameValidation = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear TextFields")
                    }
                }
            }
        }
    }
}