package com.linguacards.features.about.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val versionName = getVersionName(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(com.linguacards.features.about.R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "LinguaCards",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = versionName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            Text(
                text = stringResource(com.linguacards.features.about.R.string.about_description),
                style = MaterialTheme.typography.bodyMedium
            )

            HorizontalDivider()

            Text(
                text = stringResource(com.linguacards.features.about.R.string.about_developer),
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedButton(
                onClick = { openEmail(context) }
            ) {
                Text(stringResource(com.linguacards.features.about.R.string.about_email))
            }

            TextButton(
                onClick = { openPrivacyPolicy(context) }
            ) {
                Text(stringResource(com.linguacards.features.about.R.string.about_privacy_policy))
            }

            Text(
                text = stringResource(com.linguacards.features.about.R.string.about_license),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getVersionName(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        "${context.getString(com.linguacards.features.about.R.string.about_version)} ${packageInfo.versionName}"
    } catch (e: Exception) {
        context.getString(com.linguacards.features.about.R.string.about_version) + " unknown"
    }
}

private fun openEmail(context: Context) {
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data =
            $$"mailto:$${context.getString(com.linguacards.features.about.R.string.about_email)}".toUri()
    }
    context.startActivity(Intent.createChooser(emailIntent, "Send email"))
}

private fun openPrivacyPolicy(context: Context) {
    val url = "https://yourdomain.com/privacy"
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}