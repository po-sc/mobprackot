plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
}

// buildscript блок удалён или не нужен, если вы используете плагины из plugins { ... }

// Не указывайте здесь репозитории. Репозитории уже указаны в settings.gradle.kts.

