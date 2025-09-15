# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.android.org.conscrypt.SSLParametersImpl
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl



# Сохраняем все классы в пакете models, их поля и методы.
# Это необходимо для библиотек рефлексии, таких как GSON.
-keep class com.alaka_ala.florafilm.ui.util.api.kinopoisk.models.** { *; }
-keep class com.alaka_ala.florafilm.ui.util.api.lumex.**

# Это самое важное правило для TypeToken.
# Оно сохраняет generic-сигнатуры, которые R8 может удалить.
-keepattributes Signature




# --- Правила для Firebase Realtime Database POJO (Plain Old Java Objects) ---

# Сохраняем класс FilmDataModel и его членов от обфускации и сокращения.
# Это необходимо, чтобы Firebase мог корректно сопоставлять данные из JSON с объектом.
-keepclassmembers class com.alaka_ala.florafilm.ui.util.api.firebase.FilmDataModel {
    # Сохраняем пустой конструктор, который Firebase использует для создания экземпляра класса.
    <init>();
    # Сохраняем все поля и методы (геттеры/сеттеры).
    *;
}

# Также рекомендуется сохранить сам класс, чтобы он не был удален, если R8 посчитает его неиспользуемым.
-keep class com.alaka_ala.florafilm.ui.util.api.firebase.FilmDataModel

-keep class org.libtorrent4j.swig.libtorrent_jni {*;}