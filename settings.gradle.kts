pluginManagement {
    repositories {
        google {
            content {
                // solo acepta dependencias de Google, Android y AndroidX
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Forzamos a usar solo repos definidos aquí
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "libra-users"

// Incluye el módulo principal de la app
include(":app")