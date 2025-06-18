package com.beadpay.samplepos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/* ── Intent contract (string-only) ─────────────────────────────────────────── */
private const val INTENT_ACTION_PAY = "com.beadpay.wrapper.intent.PAY"

private const val EXTRA_AMOUNT     = "amount"      // Float  extra → dollars
private const val EXTRA_PAYMENT_ID = "paymentId"   // String extra
private const val EXTRA_STATUS     = "status"      // String extra

private const val TAG = "SamplePOS"
/* ──────────────────────────────────────────────────────────────────────────── */

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { PayScreen() } }
    }
}

@Composable
private fun PayScreen() {
    /** Text the cashier types in (dollar amount). */
    var amountText by remember { mutableStateOf("") }

    /** Holds the latest (paymentId, status) pair while the dialog is shown. */
    var dialogState by remember { mutableStateOf<Pair<String, String>?>(null) }

    val context = LocalContext.current

    /* Launches the wrapper and awaits Activity-Result. */
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res: ActivityResult ->
        if (res.resultCode == Activity.RESULT_OK) {
            val data       = res.data
            val paymentId  = data?.getStringExtra(EXTRA_PAYMENT_ID) ?: "?"
            val status     = data?.getStringExtra(EXTRA_STATUS)     ?: "UNKNOWN"

            Log.d(TAG, "Wrapper returned id=$paymentId status=$status")
            dialogState = paymentId to status       // triggers result dialog
        }
    }

    /* ---------------- UI – enter amount and start payment ---------------- */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value         = amountText,
            onValueChange = { amountText = it },
            label         = { Text("Enter amount (USD)") },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val dollars = amountText.toFloatOrNull()
                if (dollars == null || dollars <= 0f) {
                    Toast
                        .makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT)
                        .show()
                    return@Button
                }

                /* Fire the wrapper intent. */
                val payIntent = Intent(INTENT_ACTION_PAY).apply {
                    putExtra(EXTRA_AMOUNT, dollars)          // Float extra
                }
                Log.d(TAG, "Launching wrapper for $$dollars")
                launcher.launch(payIntent)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Pay") }
    }

    /* ---------------- Result dialog ---------------- */
    dialogState?.let { (paymentId, status) ->
        AlertDialog(
            onDismissRequest = { dialogState = null },
            confirmButton    = {
                TextButton(onClick = { dialogState = null }) { Text("OK") }
            },
            title = { Text("Payment result") },
            text  = { Text("Payment ID: $paymentId\nStatus: $status") }
        )
    }
}
