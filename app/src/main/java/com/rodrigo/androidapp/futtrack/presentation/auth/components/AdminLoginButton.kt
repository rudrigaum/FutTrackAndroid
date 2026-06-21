package com.rodrigo.androidapp.futtrack.presentation.auth.components

import android.content.Context
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import com.rodrigo.androidapp.futtrack.R

@Composable
fun AdminLoginButton(
    onTokenReceived: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            coroutineScope.launch {
                handleGoogleSignIn(context, onTokenReceived)
            }
        },
        modifier = modifier
    ) {
        Text("Área do Administrador")
    }
}

private suspend fun handleGoogleSignIn(
    context: Context,
    onTokenReceived: (String) -> Unit
) {
    val credentialManager = CredentialManager.create(context)
    val webClientId = context.getString(R.string.default_web_client_id)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        val credential = result.credential

        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            onTokenReceived(googleIdTokenCredential.idToken)
        }
    } catch (e: Exception) {
        Log.e("Auth", "Erro ao tentar realizar o login com o Google", e)
    }
}