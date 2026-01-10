# =================== Reglas Generales para Kotlin y R8 ===================
# Mantiene atributos esenciales para la reflexión y el correcto funcionamiento de Kotlin.
-keepattributes Signature,InnerClasses,EnclosingMethod

# Mantiene los metadatos de Kotlin, cruciales para la introspección.
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** { @kotlin.Metadata <fields>; }

# Mantiene las clases de continuación para las corrutinas.
-keep class kotlin.coroutines.Continuation


# =================== Reglas para Retrofit ===================
# Mantiene la firma genérica de Call y Response, que R8 podría eliminar.
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Mantiene las interfaces de servicio de Retrofit (anotadas con @retrofit2.http.*).
# R8 no puede ver las implementaciones (creadas por Proxy) y puede anularlas si no se mantienen.
-if interface * { @retrofit2.http.* <methods>; } -keep,allowobfuscation interface <1>
-if interface * { @retrofit2.http.* <methods>; } -keep,allowobfuscation interface * extends <1>


# =================== Reglas para kotlinx.serialization ===================
# Mantiene los miembros de las clases internas de la librería de serialización.
-keepclassmembers class kotlinx.serialization.internal.* {
    *;
}
# Mantiene los constructores de los serializers generados.
-keep class * extends kotlinx.serialization.internal.GeneratedSerializer {
    <init>(...);
}
# Mantiene las clases de serializer generadas (que terminan en $$serializer).
-keep class **$$serializer {
    *;
}
# Mantiene las clases que implementan KSerializer (para serializers personalizados).
-keep class * implements kotlinx.serialization.KSerializer {
    *;
}
# Mantiene el nombre de la anotación SerialName.
-keepnames class kotlinx.serialization.SerialName
# Mantiene los miembros (campos) que están anotados con @SerialName.
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName *;
}
# Mantiene todas las clases en el paquete DTO que son anotadas con @Serializable.
# Esta es una regla general importante para que R8 no elimine las clases de datos.
-keep @kotlinx.serialization.Serializable class com.sinc.mobile.data.network.dto.** { *; }


# =================== Reglas Específicas para DTOs (Diagnóstico) ===================
# Reglas explícitas para clases que han demostrado ser problemáticas.
-keep class com.sinc.mobile.data.network.dto.ValidationErrorResponse { *; }
-keep class com.sinc.mobile.data.network.dto.RequestPasswordResetRequest { *; }


# =================== Reglas por defecto del proyecto ===================
# Si tu proyecto usa WebView con JS, descomenta lo siguiente
# y especifica el nombre de clase completo para la interfaz de JavaScript.
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Descomenta esto para preservar la información del número de línea para
# depurar los stack traces.
#-keepattributes SourceFile,LineNumberTable

# Si mantienes la información del número de línea, descomenta esto para
# ocultar el nombre del archivo fuente original.
#-renamesourcefileattribute SourceFile