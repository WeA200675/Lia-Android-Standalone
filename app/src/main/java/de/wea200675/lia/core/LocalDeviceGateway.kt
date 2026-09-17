package de.wea200675.lia.core

data class LocalDevice(val id: String, val name: String, val address: String, val kind: String)

interface LocalDeviceTransport {
    fun discover(): List<LocalDevice>
    fun readConfiguration(device: LocalDevice): Result<ByteArray>
    fun writeConfiguration(device: LocalDevice, backup: ByteArray): Result<Unit>
}

/** Allow-list and encrypted backup boundary for explicitly paired WLAN devices. */
class LocalDeviceGateway(
    private val transport: LocalDeviceTransport,
    private val secureStore: SecureStore,
    private val pairedIds: Set<String>
) {
    fun discoverPaired(): List<LocalDevice> = transport.discover().filter { it.id in pairedIds }

    fun backup(device: LocalDevice): Result<Unit> {
        if (device.id !in pairedIds) return Result.failure(SecurityException("Device not paired"))
        return transport.readConfiguration(device).map { secureStore.put("device.backup." + device.id, it) }
    }

    fun restore(device: LocalDevice): Result<Unit> {
        if (device.id !in pairedIds) return Result.failure(SecurityException("Device not paired"))
        val backup = secureStore.get("device.backup." + device.id)
            ?: return Result.failure(IllegalStateException("No backup available"))
        return transport.writeConfiguration(device, backup)
    }
}
