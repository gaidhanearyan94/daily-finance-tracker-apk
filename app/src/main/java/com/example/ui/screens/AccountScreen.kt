package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firebase.SyncState
import com.example.ui.ExpenseViewModel
import com.example.ui.theme.FinFlowCardDark
import com.example.ui.theme.FinFlowNeonLime
import com.example.ui.theme.FinFlowPillDark
import com.example.ui.theme.FinFlowPillDarkElevated
import com.example.ui.theme.FinFlowTextPrimary
import com.example.ui.theme.FinFlowTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AuthMode {
    EMAIL,
    PHONE,
    GOOGLE
}

@Composable
fun AccountScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedAuthMode by remember { mutableStateOf(AuthMode.EMAIL) }
    var isSignUp by remember { mutableStateOf(false) }

    // Email state
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Phone state
    var phoneNumberInput by remember { mutableStateOf("") }
    var smsCodeInput by remember { mutableStateOf("") }
    var isPhoneCodeSent by remember { mutableStateOf(false) }

    // Status / Message feedback
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isErrorMessage by remember { mutableStateOf(false) }
    var isOperating by remember { mutableStateOf(false) }

    fun showFeedback(msg: String, isError: Boolean = false) {
        feedbackMessage = msg
        isErrorMessage = isError
        isOperating = false
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("account_screen_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Title & Project Badge
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Cloud Account",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = FinFlowTextPrimary,
                    modifier = Modifier.testTag("account_title")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (currentUser != null) FinFlowNeonLime else Color(0xFFFFB020))
                    )
                    Text(
                        text = "Firebase: personal-finance-tracker-8a00c",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FinFlowTextSecondary
                    )
                }
            }
        }

        // 2. Feedback Alert Banner (if any)
        if (feedbackMessage != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feedback_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isErrorMessage) Color(0xFF331616) else Color(0xFF132B1E)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isErrorMessage) Color(0xFFFF5252) else FinFlowNeonLime
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isErrorMessage) Icons.Default.Security else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isErrorMessage) Color(0xFFFF5252) else FinFlowNeonLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = feedbackMessage.orEmpty(),
                            fontSize = 13.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. User Signed In vs Not Signed In
        if (currentUser != null) {
            val user = currentUser!!
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_profile_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = FinFlowCardDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(FinFlowPillDarkElevated)
                                    .border(2.dp, FinFlowNeonLime, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (user.displayName?.take(1)
                                        ?: user.email?.take(1)
                                        ?: user.phoneNumber?.take(2)
                                        ?: "U").uppercase(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinFlowNeonLime
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName?.ifBlank { null } ?: "Personal Finance User",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = user.email ?: user.phoneNumber ?: "Authenticated User",
                                    fontSize = 13.sp,
                                    color = FinFlowTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "UID: ${user.uid.take(12)}...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            color = Color(0xFF262C34)
                        )

                        // Cloud Sync Status Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Firestore Sync Status",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = FinFlowTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (syncState == SyncState.SYNCED) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = if (syncState == SyncState.SYNCED) FinFlowNeonLime else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (syncState) {
                                            SyncState.SYNCING -> "Syncing data..."
                                            SyncState.SYNCED -> "Cloud Synced"
                                            SyncState.ERROR -> "Sync Warning"
                                            else -> "Ready to sync"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (syncState == SyncState.SYNCED) FinFlowNeonLime else Color.White
                                    )
                                }
                            }

                            if (lastSyncTime != null) {
                                val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
                                Text(
                                    text = "Last: ${timeFormat.format(Date(lastSyncTime!!))}",
                                    fontSize = 11.sp,
                                    color = FinFlowTextSecondary
                                )
                            }
                        }

                        if (syncMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = syncMessage,
                                fontSize = 12.sp,
                                color = Color(0xFF9EABB9)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Backup & Restore Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.syncLocalAndCloud()
                                    showFeedback("Local expenses & cards synced to Firestore!")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sync_to_cloud_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FinFlowNeonLime,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Cloud", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.pullCloudData { success, msg ->
                                        showFeedback(msg, isError = !success)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("restore_from_cloud_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = ButtonDefaults.outlinedButtonBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF3B434E))),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sign Out Button
                        Button(
                            onClick = {
                                viewModel.signOut()
                                showFeedback("You have been signed out.")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sign_out_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2B2020),
                                contentColor = Color(0xFFFF6B6B)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            // NOT SIGNED IN: Show Auth Selector & Forms
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_container_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = FinFlowCardDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Sign In to FinFlow Cloud",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sync expenses, credit cards, and analytics with personal-finance-tracker-8a00c",
                            fontSize = 12.sp,
                            color = FinFlowTextSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Segmented Mode Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(FinFlowPillDark)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AuthMode.values().forEach { mode ->
                                val isSelected = selectedAuthMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) FinFlowPillDarkElevated else Color.Transparent)
                                        .clickable { selectedAuthMode = mode }
                                        .padding(vertical = 10.dp)
                                        .testTag("auth_mode_${mode.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (mode) {
                                                AuthMode.EMAIL -> Icons.Default.Email
                                                AuthMode.PHONE -> Icons.Default.Phone
                                                AuthMode.GOOGLE -> Icons.Default.Security
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) FinFlowNeonLime else Color(0xFF8C959F),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when (mode) {
                                                AuthMode.EMAIL -> "Email"
                                                AuthMode.PHONE -> "Phone"
                                                AuthMode.GOOGLE -> "Google"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color(0xFF8C959F)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // AUTH FORMS
                        when (selectedAuthMode) {
                            AuthMode.EMAIL -> {
                                // Toggle Sign In vs Sign Up
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isSignUp) "Create Account" else "Sign In with Email",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    TextButton(onClick = { isSignUp = !isSignUp }) {
                                        Text(
                                            text = if (isSignUp) "Have an account? Sign In" else "New user? Register",
                                            fontSize = 12.sp,
                                            color = FinFlowNeonLime
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (isSignUp) {
                                    OutlinedTextField(
                                        value = displayNameInput,
                                        onValueChange = { displayNameInput = it },
                                        label = { Text("Display Name") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FinFlowNeonLime) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("email_display_name_input"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = FinFlowNeonLime,
                                            unfocusedBorderColor = Color(0xFF333B44),
                                            focusedLabelColor = FinFlowNeonLime,
                                            unfocusedLabelColor = FinFlowTextSecondary,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Email Address") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = FinFlowNeonLime) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("email_input"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FinFlowNeonLime,
                                        unfocusedBorderColor = Color(0xFF333B44),
                                        focusedLabelColor = FinFlowNeonLime,
                                        unfocusedLabelColor = FinFlowTextSecondary,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text("Password (min 6 chars)") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FinFlowNeonLime) },
                                    trailingIcon = {
                                        IconButton(onClick = { showPassword = !showPassword }) {
                                            Icon(
                                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = Color(0xFF8C959F)
                                            )
                                        }
                                    },
                                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("password_input"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FinFlowNeonLime,
                                        unfocusedBorderColor = Color(0xFF333B44),
                                        focusedLabelColor = FinFlowNeonLime,
                                        unfocusedLabelColor = FinFlowTextSecondary,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        isOperating = true
                                        if (isSignUp) {
                                            viewModel.signUpWithEmail(
                                                email = emailInput,
                                                pass = passwordInput,
                                                displayName = displayNameInput,
                                                onSuccess = { showFeedback("Account created! Signed in to Cloud.") },
                                                onError = { showFeedback(it, isError = true) }
                                            )
                                        } else {
                                            viewModel.signInWithEmail(
                                                email = emailInput,
                                                pass = passwordInput,
                                                onSuccess = { showFeedback("Signed in successfully!") },
                                                onError = { showFeedback(it, isError = true) }
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("email_submit_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FinFlowNeonLime,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    enabled = !isOperating
                                ) {
                                    if (isOperating) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                                    } else {
                                        Text(
                                            text = if (isSignUp) "Register Account" else "Sign In",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            AuthMode.PHONE -> {
                                Text(
                                    text = "Phone Number Authentication",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Receive a 6-digit SMS verification code to sign in",
                                    fontSize = 12.sp,
                                    color = FinFlowTextSecondary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = phoneNumberInput,
                                    onValueChange = { phoneNumberInput = it },
                                    label = { Text("Phone Number (with +country code)") },
                                    placeholder = { Text("+1 555 123 4567", color = Color.Gray) },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = FinFlowNeonLime) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("phone_input"),
                                    shape = RoundedCornerShape(14.dp),
                                    enabled = !isPhoneCodeSent,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FinFlowNeonLime,
                                        unfocusedBorderColor = Color(0xFF333B44),
                                        focusedLabelColor = FinFlowNeonLime,
                                        unfocusedLabelColor = FinFlowTextSecondary,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (!isPhoneCodeSent) {
                                    Button(
                                        onClick = {
                                            if (activity == null) {
                                                showFeedback("Activity context required for SMS verification", isError = true)
                                                return@Button
                                            }
                                            isOperating = true
                                            viewModel.sendPhoneVerification(
                                                activity = activity,
                                                phone = phoneNumberInput,
                                                onCodeSent = {
                                                    isPhoneCodeSent = true
                                                    showFeedback("Verification code sent! Check your SMS.")
                                                },
                                                onError = { showFeedback(it, isError = true) }
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("send_sms_button"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = FinFlowNeonLime,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        enabled = !isOperating
                                    ) {
                                        if (isOperating) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                                        } else {
                                            Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Send SMS Verification Code", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                } else {
                                    // Enter SMS verification code
                                    OutlinedTextField(
                                        value = smsCodeInput,
                                        onValueChange = { smsCodeInput = it },
                                        label = { Text("6-Digit SMS Code") },
                                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FinFlowNeonLime) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sms_code_input"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = FinFlowNeonLime,
                                            unfocusedBorderColor = Color(0xFF333B44),
                                            focusedLabelColor = FinFlowNeonLime,
                                            unfocusedLabelColor = FinFlowTextSecondary,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { isPhoneCodeSent = false },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                        ) {
                                            Text("Change Phone", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                isOperating = true
                                                viewModel.verifyPhoneCode(
                                                    code = smsCodeInput,
                                                    onSuccess = { showFeedback("Phone verified! Signed in.") },
                                                    onError = { showFeedback(it, isError = true) }
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1.4f)
                                                .height(48.dp)
                                                .testTag("verify_phone_code_button"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = FinFlowNeonLime,
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            enabled = !isOperating
                                        ) {
                                            if (isOperating) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                                            } else {
                                                Text("Verify & Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            AuthMode.GOOGLE -> {
                                Text(
                                    text = "Google Sign-In",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Fast 1-tap Google Authentication via Credential Manager",
                                    fontSize = 12.sp,
                                    color = FinFlowTextSecondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (activity == null) {
                                            showFeedback("Activity context required for Google Sign-In", isError = true)
                                            return@Button
                                        }
                                        isOperating = true
                                        viewModel.signInWithGoogle(
                                            activity = activity,
                                            onSuccess = { showFeedback("Signed in with Google!") },
                                            onError = { showFeedback(it, isError = true) }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("google_sign_in_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    enabled = !isOperating
                                ) {
                                    if (isOperating) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = Color(0xFF4285F4),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Continue with Google",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Note: If running on emulator or non-Play Services device, use Email/Password or Phone Authentication which work seamlessly!",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8C959F)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Firebase Project & Infrastructure Details Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("firebase_info_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FinFlowCardDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = FinFlowNeonLime,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backend Infrastructure",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    BackendDetailRow(label = "Firebase Project", value = "personal-finance-tracker-8a00c")
                    BackendDetailRow(label = "Package Name", value = "com.aistudio.expensetracker.vxyqzp")
                    BackendDetailRow(label = "Firestore DB", value = "ai-studio-android-finflow-9436150b-ce08-4514-9cff-ebeaa0bd2e82")
                    BackendDetailRow(label = "Rules Status", value = "Deployed (Authenticated user isolation)")
                    BackendDetailRow(label = "Local Cache", value = "${uiState.expenses.size} expenses • ${uiState.cards.size} cards in Room")
                }
            }
        }
    }
}

@Composable
private fun BackendDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = FinFlowTextSecondary
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
