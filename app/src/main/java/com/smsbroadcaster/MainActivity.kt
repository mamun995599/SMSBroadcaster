package com.smsbroadcaster

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.smsbroadcaster.adapter.ClientAdapter
import com.smsbroadcaster.databinding.ActivityMainBinding
import com.smsbroadcaster.model.ConnectedClient
import com.smsbroadcaster.service.SMSBroadcastService
import com.smsbroadcaster.utils.NetworkUtils
import com.smsbroadcaster.utils.PreferenceManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefManager: PreferenceManager
    private lateinit var clientAdapter: ClientAdapter

    private var smsBroadcastService: SMSBroadcastService? = null
    private var isServiceBound = false

    private val requiredPermissions = mutableListOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Some permissions denied. App may not work properly.", Toast.LENGTH_LONG).show()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SMSBroadcastService.LocalBinder
            smsBroadcastService = binder.getService()
            isServiceBound = true
            updateUIFromService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            smsBroadcastService = null
            isServiceBound = false
        }
    }

    private val serviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SMSBroadcastService.ACTION_CLIENT_UPDATE -> {
                    updateClientList()
                }
                SMSBroadcastService.ACTION_LOG_UPDATE -> {
                    val message = intent.getStringExtra(SMSBroadcastService.EXTRA_LOG_MESSAGE) ?: ""
                    val isSuccess = intent.getBooleanExtra(SMSBroadcastService.EXTRA_LOG_SUCCESS, true)
                    appendToConsole(message, isSuccess)
                }
                SMSBroadcastService.ACTION_SERVER_STATUS -> {
                    val isRunning = intent.getBooleanExtra(SMSBroadcastService.EXTRA_SERVER_RUNNING, false)
                    val port = intent.getIntExtra(SMSBroadcastService.EXTRA_PORT, 0)
                    updateServerStatus(isRunning, port)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PreferenceManager(this)

        setupUI()
        checkAndRequestPermissions()
        requestBatteryOptimizationExemption()
        displayDeviceIPs()
        loadSavedSettings()

        // Register broadcast receivers
        val filter = IntentFilter().apply {
            addAction(SMSBroadcastService.ACTION_CLIENT_UPDATE)
            addAction(SMSBroadcastService.ACTION_LOG_UPDATE)
            addAction(SMSBroadcastService.ACTION_SERVER_STATUS)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceReceiver, filter)

        // Bind to service if it's running
        if (prefManager.isServerEnabled) {
            bindToService()
        }
    }

    private fun setupUI() {
        // Setup RecyclerView for clients
        clientAdapter = ClientAdapter()
        binding.rvConnectedClients.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = clientAdapter
        }

        // Setup switch listener
        binding.switchServer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                saveSettings()
                startBroadcastService()
            } else {
                stopBroadcastService()
            }
        }

        // Setup radio button listener
        binding.rgHttpMethod.setOnCheckedChangeListener { _, checkedId ->
            prefManager.httpMethod = if (checkedId == R.id.rbGet) "GET" else "POST"
        }

        // Setup clear log button
        binding.btnClearLog.setOnClickListener {
            binding.tvConsoleLog.text = "> Console cleared...\n"
        }
    }

    private fun loadSavedSettings() {
        binding.etWebSocketPort.setText(
            if (prefManager.webSocketPort > 0) prefManager.webSocketPort.toString() else ""
        )
        binding.etHttpIpAddress.setText(prefManager.httpIp)
        binding.etHttpPort.setText(
            if (prefManager.httpPort > 0) prefManager.httpPort.toString() else ""
        )

        if (prefManager.httpMethod == "POST") {
            binding.rbPost.isChecked = true
        } else {
            binding.rbGet.isChecked = true
        }

        // Set switch state based on service status
        binding.switchServer.isChecked = prefManager.isServerEnabled
    }

    private fun saveSettings() {
        val wsPort = binding.etWebSocketPort.text.toString().toIntOrNull()
            ?: PreferenceManager.DEFAULT_WEBSOCKET_PORT
        prefManager.webSocketPort = wsPort

        prefManager.httpIp = binding.etHttpIpAddress.text.toString().trim()
        prefManager.httpPort = binding.etHttpPort.text.toString().toIntOrNull() ?: 80
        prefManager.httpMethod = if (binding.rbPost.isChecked) "POST" else "GET"
    }

    private fun displayDeviceIPs() {
        val ips = NetworkUtils.getAllIpAddressesFormatted()
        binding.tvDeviceIp.text = ips
    }

    private fun startBroadcastService() {
        val intent = Intent(this, SMSBroadcastService::class.java).apply {
            action = SMSBroadcastService.ACTION_START_SERVICE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindToService()

        val port = prefManager.webSocketPort.takeIf { it > 0 }
            ?: PreferenceManager.DEFAULT_WEBSOCKET_PORT
        updateServerStatus(true, port)
    }

    private fun stopBroadcastService() {
        val intent = Intent(this, SMSBroadcastService::class.java).apply {
            action = SMSBroadcastService.ACTION_STOP_SERVICE
        }
        startService(intent)

        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }

        updateServerStatus(false, 0)
        clientAdapter.submitList(emptyList())
        updateClientCount(0)
    }

    private fun bindToService() {
        val intent = Intent(this, SMSBroadcastService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    private fun updateUIFromService() {
        smsBroadcastService?.let { service ->
            if (service.isServerRunning()) {
                updateServerStatus(true, prefManager.webSocketPort)
                updateClientList()
            }
        }
    }

    private fun updateClientList() {
        smsBroadcastService?.let { service ->
            val clients = service.getConnectedClients()
            clientAdapter.submitList(clients)
            updateClientCount(clients.size)

            binding.tvNoClients.visibility = if (clients.isEmpty()) View.VISIBLE else View.GONE
            binding.rvConnectedClients.visibility = if (clients.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun updateClientCount(count: Int) {
        binding.tvClientCount.text = "$count client(s) connected"
    }

    private fun updateServerStatus(isRunning: Boolean, port: Int) {
        if (isRunning) {
            binding.tvServerStatus.text = "Server is ON (Port: $port)"
            binding.tvServerStatus.setTextColor(ContextCompat.getColor(this, R.color.success))
        } else {
            binding.tvServerStatus.text = getString(R.string.server_off)
            binding.tvServerStatus.setTextColor(ContextCompat.getColor(this, R.color.error))
        }
    }

    private fun appendToConsole(message: String, isSuccess: Boolean) {
        val color = if (isSuccess) {
            ContextCompat.getColor(this, R.color.console_text)
        } else {
            ContextCompat.getColor(this, R.color.console_error)
        }

        val spannable = SpannableStringBuilder("$message\n")
        spannable.setSpan(
            ForegroundColorSpan(color),
            0,
            spannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvConsoleLog.append(spannable)

        // Auto-scroll to bottom
        binding.svConsole.post {
            binding.svConsole.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                AlertDialog.Builder(this)
                    .setTitle("Battery Optimization")
                    .setMessage("To ensure the service runs reliably in background, please disable battery optimization for this app.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:$packageName")
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Later", null)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        displayDeviceIPs()
        if (prefManager.isServerEnabled && !isServiceBound) {
            bindToService()
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceReceiver)
        if (isServiceBound) {
            unbindService(serviceConnection)
        }
        super.onDestroy()
    }
}