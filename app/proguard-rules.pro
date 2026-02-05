-keep class com.nvv.mediadata.data.viewmodel.PriorityRenderersFactory { *; }
-keep class androidx.media3.** { *; }
-keep class com.nvv.mediadata.data.viewmodel.PlaybackService { *; }
-keep class androidx.media3.decoder.ffmpeg.** { *; }
-keep class androidx.media3.decoder.av1.** { *; }
-keep class androidx.media3.decoder.iamf.** { *; }
-keep class androidx.media3.session.** { *; }


-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*, InnerClasses, Signature
-keepattributes EnclosingMethod
-keep interface androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class **_AssistedFactory { *; }
-keep class **_Factory { *; }
-keep class **_HiltModules** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
-keepattributes *Annotation*
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.nvv.mediadata.**$$serializer { *; }
-keepclassmembers class com.nvv.mediadata.** {
    *** Companion;
}
-keepclasseswithmembers class com.nvv.mediadata.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class androidx.documentfile.provider.** { *; }
-dontwarn androidx.documentfile.provider.**
-dontwarn androidx.compose.**
-keep @androidx.compose.runtime.Composable class *
-keep class * extends androidx.compose.runtime.Composer
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}
-keep class * extends android.content.BroadcastReceiver {
    <init>();
}
-keep class * extends android.app.Service {
    <init>();
}
-keep class timber.log.** { *; }
-dontwarn timber.log.**
-keep class * extends android.app.Activity
-keep class * extends android.app.Application
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*