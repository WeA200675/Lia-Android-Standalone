package de.wea200675.lia

import de.wea200675.lia.core.LocalDevice
import de.wea200675.lia.core.LocalDeviceGateway
import de.wea200675.lia.core.LocalDeviceTransport
import de.wea200675.lia.core.SecureStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalDeviceGatewayTest {
    private class MemoryStore : SecureStore {
        private val values = mutableMapOf<String, ByteArray>()
        override fun put(key: String, value: ByteArray) { values[key] = value.copyOf() }
        override fun get(key: String): ByteArray? = values[key]?.copyOf()
        override fun delete(key: String) { values.remove(key) }
    }

    @Test fun discoveryOnlyReturnsPairedDevices() {
        val paired = LocalDevice("paired", "Mower", "192.168.1.10", "mower")
        val other = LocalDevice("other", "Camera", "192.168.1.11", "camera")
        val transport = object : LocalDeviceTransport {
            override fun discover() = listOf(paired, other)
            override fun readConfiguration(device: LocalDevice) = Result.success(byteArrayOf(1, 2, 3))
            override fun writeConfiguration(device: LocalDevice, backup: ByteArray) = Result.success(Unit)
        }
        val gateway = LocalDeviceGateway(transport, MemoryStore(), setOf("paired"))
        assertEquals(listOf(paired), gateway.discoverPaired())
    }

    @Test fun backupAndRestoreRequirePairingAndExistingBackup() {
        val paired = LocalDevice("paired", "Mower", "192.168.1.10", "mower")
        val unpaired = paired.copy(id = "unpaired")
        var restored: ByteArray? = null
        val transport = object : LocalDeviceTransport {
            override fun discover() = emptyList<LocalDevice>()
            override fun readConfiguration(device: LocalDevice) = Result.success(byteArrayOf(7, 8))
            override fun writeConfiguration(device: LocalDevice, backup: ByteArray) = Result.success(Unit).also { restored = backup }
        }
        val gateway = LocalDeviceGateway(transport, MemoryStore(), setOf("paired"))
        assertTrue(gateway.backup(paired).isSuccess)
        assertTrue(gateway.restore(paired).isSuccess)
        assertEquals(byteArrayOf(7, 8).toList(), restored?.toList())
        assertFalse(gateway.backup(unpaired).isSuccess)
        assertFalse(gateway.restore(unpaired).isSuccess)
    }
}
