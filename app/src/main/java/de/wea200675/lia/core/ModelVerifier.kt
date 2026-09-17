package de.wea200675.lia.core

import java.io.File
import java.security.MessageDigest

object ModelVerifier {
    fun sha256(file:File):String { val md=MessageDigest.getInstance("SHA-256"); file.inputStream().use { input -> val buf=ByteArray(1024*1024); var n=input.read(buf); while(n>0){ md.update(buf,0,n); n=input.read(buf) } }; return md.digest().joinToString(""){ "%02x".format(it) } }
    fun verified(file:File,expected:String):Boolean = file.isFile && expected.matches(Regex("[0-9a-fA-F]{64}")) && sha256(file).equals(expected,ignoreCase=true)
}
