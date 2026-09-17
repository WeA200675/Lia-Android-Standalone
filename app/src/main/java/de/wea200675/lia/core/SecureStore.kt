package de.wea200675.lia.core

/** Storage boundary; implementation will use Android Keystore + encrypted preferences. */
interface SecureStore { fun put(key:String,value:ByteArray); fun get(key:String):ByteArray?; fun delete(key:String) }
