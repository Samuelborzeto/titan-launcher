package com.titanlauncher.settings

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.titanlauncher.ui.TitanColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(store: SettingsStore, onBack: () -> Unit) {
    val enabled by store.titanEnabled.collectAsState(initial = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Se o pedido for recusado ou o sistema não mostrar a pergunta, abre a tela de apps padrão.
    val roleRequest = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) openHomeSettings(context)
    }

    BackHandler(onBack = onBack)

    Column(
        Modifier
            .fillMaxSize()
            .background(TitanColors.Black)
            // Impede que o toque passe para a tela inicial que está por baixo.
            .pointerInput(Unit) { detectTapGestures { } }
            .systemBarsPadding()
            .padding(20.dp),
    ) {
        Text("Ajustes", color = Color.White, fontSize = 24.sp)
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Personagem", color = Color.White, fontSize = 16.sp)
                Text(
                    "O ogro empurra a tela na troca de páginas",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = { value -> scope.launch { store.setTitanEnabled(value) } },
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val request = homeRoleIntent(context)
                val asked = request != null && runCatching { roleRequest.launch(request) }.isSuccess
                if (!asked) openHomeSettings(context)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Definir como launcher padrão")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }
    }
}

/** Pedido do papel de launcher pelo sistema (Android 10+), ou null se não der para pedir. */
private fun homeRoleIntent(context: Context): Intent? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
    val roles = context.getSystemService(RoleManager::class.java) ?: return null
    if (!roles.isRoleAvailable(RoleManager.ROLE_HOME) || roles.isRoleHeld(RoleManager.ROLE_HOME)) return null
    return runCatching { roles.createRequestRoleIntent(RoleManager.ROLE_HOME) }.getOrNull()
}

private fun openHomeSettings(context: Context) {
    runCatching {
        context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
