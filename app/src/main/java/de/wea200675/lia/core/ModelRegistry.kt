package de.wea200675.lia.core

import android.content.Context
import java.io.File

class ModelRegistry(context:Context) {
 private val dir=File(context.filesDir,"models").apply{mkdirs()}
 fun modelFile(spec:ModelSpec)=File(dir,spec.fileName)
 fun storageUsedBytes()=dir.walkTopDown().filter{it.isFile}.sumOf{it.length()}
 fun isVerified(spec:ModelSpec)=ModelVerifier.verified(modelFile(spec),spec.sha256)
 fun primaryOrRecovery(primary:ModelSpec,recovery:ModelSpec):ModelSpec?=when { isVerified(primary)->primary; isVerified(recovery)->recovery; else->null }
}
