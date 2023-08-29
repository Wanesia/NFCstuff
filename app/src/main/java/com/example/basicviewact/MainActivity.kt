package com.example.basicviewact
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null
    private var nfcFilters: Array<IntentFilter>? = null

    private lateinit var writeNfcButton: Button


    private lateinit var nfcDataTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcDataTextView = findViewById(R.id.nfcDataTextView)


        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Create a PendingIntent to handle NFC events
        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_MUTABLE
        )


        // Create an IntentFilter to listen for NFC tag discovery
        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndef.addDataType("text/plain")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("Malformed MIME type", e)
        }
        nfcFilters = arrayOf(ndef)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, nfcFilters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMessages != null) {
                val ndefMessages = rawMessages.map { it as NdefMessage }
                processNfcData(ndefMessages)
            }
        }
    }

    private fun processNfcData(ndefMessages: List<NdefMessage>) {
        val firstNdefMessage = ndefMessages.firstOrNull()

        // Check if the message and its records are not null
        if (firstNdefMessage != null && firstNdefMessage.records.isNotEmpty()) {
            val payload = firstNdefMessage.records[0].payload

            // Convert payload to text
            val textData = payload?.let { String(it) }

            // Update UI
            nfcDataTextView.text = "NFC Data: ${textData ?: "No data"}"

            // Log the data
            textData?.let {
                Log.d("NFC", "NFC Data: $it")
            }
        } else {
            // Handle the case where there's no NDEF data to display
            nfcDataTextView.text = "NFC Data: No data"
            Log.d("NFC", "NFC Data: No data")
        }
    }

}
