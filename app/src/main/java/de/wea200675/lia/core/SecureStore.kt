package de.wea200675.lia.core

/** Storage boundary implemented by Android Keystore-backed encrypted preferences. */
interface SecureStore { fun put(key:String,value:ByteArray); fun get(key:String):ByteArray?; fun delete(key:String) }
