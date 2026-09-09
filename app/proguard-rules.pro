# Add project specific ProGuard rules here.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class ir.atiran.hamrah.viewer.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
