# 📱 GUÍA DE CONFIGURACIÓN COMPLETA - Android Studio

## 🎯 Objetivo

Esta guía te ayudará a configurar completamente el proyecto de Android Studio para la aplicación de Biblioteca Digital, incluyendo todas las dependencias, servicios de red, autenticación JWT, y estructura base del proyecto.

---

## 📋 REQUISITOS PREVIOS

### Software Necesario:

1. **Android Studio Hedgehog (2023.1.1)** o superior
2. **JDK 17** o superior (verificar con `java -version`)
3. **Android SDK** con:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android Emulator
4. **Kotlin 2.0.21** o superior
5. **Git** (para clonar el repositorio)

### Verificación de Requisitos:

```bash
# Verificar Java
java -version

# Verificar Android SDK (en Android Studio)
# File > Settings > Appearance & Behavior > System Settings > Android SDK
```

---

## 🚀 PASO 1: ABRIR EL PROYECTO

### 1.1 Abrir en Android Studio

1. Abre **Android Studio**
2. Selecciona **File > Open**
3. Navega a la carpeta del proyecto
4. Haz clic en **OK**

### 1.2 Sincronizar Gradle

- Android Studio detectará automáticamente que es un proyecto Gradle
- Aparecerá un banner: **"Gradle files have changed since last project sync"**
- Haz clic en **"Sync Now"** o **"Sync Project with Gradle Files"** (icono del elefante)
- Espera a que se descarguen todas las dependencias (puede tardar varios minutos)

---

## ⚙️ PASO 2: CONFIGURAR EL ENTORNO DE DESARROLLO

### 2.1 Configurar JDK

1. **File > Project Structure** (o `Ctrl+Alt+Shift+S`)
2. En la pestaña **Project**:
   - **SDK Location**: Verifica que apunte a tu Android SDK
   - **Gradle JDK**: Selecciona **JDK 17** o superior
3. Haz clic en **OK**

### 2.2 Configurar Gradle

El proyecto usa **Gradle 8.12.3** con **Kotlin DSL**. Las configuraciones están en:

- `build.gradle.kts` (nivel raíz)
- `app/build.gradle.kts` (módulo app)
- `settings.gradle.kts` (configuración del proyecto)
- `gradle/libs.versions.toml` (versiones de dependencias)

**No es necesario modificar nada**, pero verifica que:
- Gradle se descargue automáticamente
- Las dependencias se resuelvan correctamente

---

## 🌐 PASO 3: CONFIGURAR URLs DE LOS MICROSERVICIOS

### 3.1 Ubicación del Archivo

El archivo a modificar es:

```
app/src/main/java/com/empresa/libra_users/data/remote/dto/RemoteModule.kt
```

### 3.2 Configuración para Emulador Android (Por Defecto)

Si usas el **Emulador de Android**, las URLs ya están configuradas:

```kotlin
private const val BASE_URL_USER_SERVICE = "http://10.0.2.2:8081/"
private const val BASE_URL_BOOK_SERVICE = "http://10.0.2.2:8082/"
private const val BASE_URL_LOAN_SERVICE = "http://10.0.2.2:8083/"
private const val BASE_URL_NOTIFICATION_SERVICE = "http://10.0.2.2:8085/"
private const val BASE_URL_REPORT_SERVICE = "http://10.0.2.2:8084/"
```

**10.0.2.2** es la IP especial que el emulador usa para referirse al localhost de tu máquina.

### 3.3 Configuración para Dispositivo Físico

Si usas un **dispositivo físico**, necesitas la IP de tu máquina:

**Windows:**

```powershell
ipconfig
# Busca "IPv4 Address" (ejemplo: 192.168.1.100)
```

**Mac/Linux:**

```bash
ifconfig
# O
ip addr show
```

Luego actualiza las URLs en `RemoteModule.kt`:

```kotlin
private const val BASE_URL_USER_SERVICE = "http://192.168.1.100:8081/" // Tu IP
private const val BASE_URL_BOOK_SERVICE = "http://192.168.1.100:8082/"
// ... etc
```

**⚠️ IMPORTANTE:**

- Asegúrate de que tu dispositivo y tu PC estén en la **misma red WiFi**
- Desactiva el firewall temporalmente o permite conexiones en los puertos 8081-8085
- Verifica que los microservicios estén corriendo en tu máquina

---

## 🔐 PASO 4: CONFIGURACIÓN DE AUTENTICACIÓN JWT

### 4.1 TokenManager

El `TokenManager` gestiona el almacenamiento seguro del token JWT usando **DataStore**:

```
app/src/main/java/com/empresa/libra_users/data/local/TokenManager.kt
```

**Funcionalidades:**

- `saveToken(token: String)`: Guarda el token después del login
- `getToken()`: Obtiene el token actual
- `getBearerToken()`: Obtiene el token con formato "Bearer <token>"
- `clearToken()`: Limpia el token al hacer logout
- `hasToken()`: Verifica si existe un token guardado

**No requiere configuración adicional**, ya está integrado con `UserPreferencesRepository`.

### 4.2 AuthInterceptor

El `AuthInterceptor` agrega automáticamente el token JWT a todas las peticiones HTTP:

```
app/src/main/java/com/empresa/libra_users/data/remote/AuthInterceptor.kt
```

**Funcionamiento:**

- Lee el token del `TokenManager`
- Lo agrega como header: `Authorization: Bearer <token>`
- Se excluyen las rutas de login y registro (no requieren autenticación)

**Rutas excluidas (no se agrega token):**

- `/api/users/login`
- `/api/users/register`
- `/api/users/validate-token`

**Ya está integrado** en `RemoteModule.kt` y se aplica automáticamente a todas las peticiones.

### 4.3 Integración Automática

El `AuthInterceptor` está configurado en `RemoteModule.kt`:

```kotlin
@Provides
@Singleton
fun provideOkHttpClient(
    authInterceptor: AuthInterceptor
): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(authInterceptor) // Agrega el token JWT automáticamente
        .addInterceptor(loggingInterceptor)
        // ...
        .build()
}
```

**No necesitas agregar headers manualmente** en las llamadas a las APIs. El interceptor lo hace automáticamente.

---

## 📦 PASO 5: VERIFICAR DEPENDENCIAS

### 5.1 Dependencias Principales

Todas las dependencias están en `app/build.gradle.kts` y `gradle/libs.versions.toml`. Verifica que se descarguen correctamente:

**Networking:**

- Retrofit 2.9.0
- OkHttp 4.12.0
- Gson Converter

**Dependency Injection:**

- Hilt 2.51.1 (usando KSP)
- Hilt Navigation Compose

**UI:**

- Jetpack Compose BOM 2024.09.00
- Material 3
- Navigation Compose 2.8.3

**Coroutines:**

- Kotlinx Coroutines 1.10.2

**Storage:**

- DataStore Preferences 1.1.1
- Room 2.6.1 (usando KSP)

### 5.2 Si hay Errores de Dependencias:

1. **File > Invalidate Caches / Restart**
2. Selecciona **"Invalidate and Restart"**
3. Espera a que Android Studio reinicie
4. Sincroniza Gradle nuevamente

---

## 🏗️ PASO 6: ESTRUCTURA DEL PROYECTO

### 6.1 Estructura de Carpetas

```
app/src/main/java/com/empresa/libra_users/

├── data/
│   ├── local/
│   │   ├── TokenManager.kt              # ✅ Gestión de tokens JWT
│   │   ├── database/
│   │   │   ├── AppDatabase.kt
│   │   │   └── InitialData.kt
│   │   └── user/
│   │       ├── BookDao.kt
│   │       ├── BookEntity.kt
│   │       ├── LoanDao.kt
│   │       ├── LoanEntity.kt
│   │       ├── NotificationDao.kt
│   │       ├── NotificationEntity.kt
│   │       ├── UserDao.kt
│   │       └── UserEntity.kt
│   ├── remote/
│   │   ├── AuthInterceptor.kt            # ✅ Interceptor JWT
│   │   ├── dto/
│   │   │   ├── BookApi.kt
│   │   │   ├── BookDto.kt
│   │   │   ├── LoanApi.kt
│   │   │   ├── LoanDto.kt
│   │   │   ├── NotificationApi.kt
│   │   │   ├── NotificationDto.kt
│   │   │   ├── RemoteModule.kt           # ✅ Configuración de red
│   │   │   ├── ReportApi.kt
│   │   │   ├── UserApi.kt
│   │   │   └── UserDto.kt
│   │   └── mapper/
│   │       ├── BookMapper.kt
│   │       ├── LoanMapper.kt
│   │       └── UserMapper.kt
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── BookRepository.kt
│   │   ├── LoanRepository.kt
│   │   ├── NotificationRepository.kt
│   │   └── UserRepository.kt
│   └── UserPreferencesRepository.kt
│
├── di/
│   ├── AppModule.kt                      # ✅ Dependency Injection
│
├── domain/
│   └── validation/
│       └── Validators.kt
│
├── screen/
│   ├── AccountSettingsScreen.kt
│   ├── admin/
│   │   ├── books/
│   │   │   └── AdminBooksScreen.kt
│   │   ├── loans/
│   │   │   └── AdminLoansScreen.kt
│   │   ├── reports/
│   │   │   └── AdminReportsScreen.kt
│   │   └── users/
│   │       └── AdminUsersScreen.kt
│   ├── AdminDashboardScreen.kt
│   ├── BookDetailsScreen.kt
│   ├── CatalogScreen.kt
│   ├── HomeScreen.kt
│   ├── LoginScreen.kt
│   ├── RegisterScreen.kt
│   └── ...
│
├── ui/
│   ├── state/
│   │   └── AuthUiState.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── ...
│
├── viewmodel/
│   ├── admin/
│   │   └── AdminDashboardViewModel.kt
│   ├── AuthViewModelFactory.kt
│   └── MainViewModel.kt
│
├── navigation/
│   ├── NavGraph.kt
│   └── Routes.kt
│
├── LibraUsersApp.kt                       # ✅ Application class con Hilt
└── MainActivity.kt                        # ✅ Activity principal
```

### 6.2 Verificar Archivos Clave

Asegúrate de que estos archivos existan:

✅ **Application Class:**

```
app/src/main/java/com/empresa/libra_users/LibraUsersApp.kt
```

Debe tener `@HiltAndroidApp`

✅ **MainActivity:**

```
app/src/main/java/com/empresa/libra_users/MainActivity.kt
```

Debe tener `@AndroidEntryPoint`

✅ **AndroidManifest:**

```
app/src/main/AndroidManifest.xml
```

Debe tener:

- Permisos de Internet
- `android:name=".LibraUsersApp"`
- `usesCleartextTraffic="true"` (para desarrollo)

✅ **TokenManager:**

```
app/src/main/java/com/empresa/libra_users/data/local/TokenManager.kt
```

✅ **AuthInterceptor:**

```
app/src/main/java/com/empresa/libra_users/data/remote/AuthInterceptor.kt
```

✅ **RemoteModule:**

```
app/src/main/java/com/empresa/libra_users/data/remote/dto/RemoteModule.kt
```

---

## 🧪 PASO 7: CONFIGURAR TESTING

### 7.1 Dependencias de Testing

Ya están incluidas en `app/build.gradle.kts`:

- JUnit 4.13.2
- AndroidX JUnit 1.2.1
- Espresso 3.6.1 (para UI tests)
- Compose UI Test

### 7.2 Ejecutar Tests

```bash
# Todos los tests
./gradlew test

# Tests específicos
./gradlew test --tests "com.empresa.libra_users.*Test"

# Desde Android Studio:
# Click derecho en carpeta test > Run 'Tests in...'
```

---

## 📱 PASO 8: CONFIGURAR EMULADOR O DISPOSITIVO

### 8.1 Crear Emulador (AVD)

1. **Tools > Device Manager**
2. Haz clic en **"Create Device"**
3. Selecciona un dispositivo (ej: Pixel 5)
4. Selecciona una imagen del sistema (API 34 recomendado)
5. Completa la configuración y haz clic en **Finish**

### 8.2 Conectar Dispositivo Físico

1. Habilita **Opciones de Desarrollador** en tu dispositivo Android
2. Activa **Depuración USB**
3. Conecta el dispositivo por USB
4. Autoriza la depuración cuando aparezca el diálogo
5. Verifica en Android Studio: **Run > Select Device**

---

## 🚀 PASO 9: EJECUTAR LA APLICACIÓN

### 9.1 Verificar que los Microservicios Estén Corriendo

Antes de ejecutar la app, asegúrate de que los microservicios estén activos:

```bash
# En terminales separadas:

cd user-management-service && mvn spring-boot:run
cd book-catalog-service && mvn spring-boot:run
cd loan-management-service && mvn spring-boot:run
cd reports-service && mvn spring-boot:run
cd notifications-service && mvn spring-boot:run
```

O usa Docker Compose:

```bash
docker-compose up -d
```

### 9.2 Ejecutar la App

1. Selecciona el dispositivo/emulador en la barra superior
2. Haz clic en el botón **Run** (▶️) o presiona `Shift+F10`
3. Espera a que compile e instale la app
4. La app se abrirá automáticamente

---

## 🔧 PASO 10: CONFIGURACIÓN ADICIONAL

### 10.1 Generar Keystore (Para APK Release)

```bash
# Manualmente:

keytool -genkey -v -keystore keystore/library-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias library-key \
  -storepass library123 \
  -keypass library123
```

### 10.2 Generar APK Release

```bash
./gradlew assembleRelease
```

El APK estará en:

```
app/build/outputs/apk/release/app-release.apk
```

### 10.3 ProGuard (Ya Configurado)

El archivo `proguard-rules.pro` ya está configurado para:

- Retrofit
- OkHttp
- Gson
- Hilt
- Room

---

## ⚠️ SOLUCIÓN DE PROBLEMAS COMUNES

### Error: "Gradle sync failed"

1. **File > Invalidate Caches / Restart**
2. Elimina la carpeta `.gradle` en el proyecto
3. Sincroniza nuevamente

### Error: "Cannot resolve symbol"

1. Verifica que las dependencias se descargaron correctamente
2. **File > Sync Project with Gradle Files**
3. **Build > Clean Project**
4. **Build > Rebuild Project**

### Error: "Connection refused" o "Network error"

1. Verifica que los microservicios estén corriendo
2. Verifica las URLs en `RemoteModule.kt`
3. Si usas dispositivo físico, verifica que esté en la misma red
4. Verifica el firewall

### Error: "JWT token not found"

1. Asegúrate de hacer login primero
2. Verifica que `TokenManager` esté guardando el token
3. Revisa los logs de `AuthInterceptor` en Logcat

### Error: "Cleartext traffic not permitted"

Ya está configurado en `AndroidManifest.xml`:

```xml
android:usesCleartextTraffic="true"
```

Si persiste, verifica que el atributo esté presente.

### Error: "AuthInterceptor no agrega el token"

1. Verifica que `TokenManager` esté guardando el token correctamente
2. Verifica que `AuthInterceptor` esté registrado en `RemoteModule.kt`
3. Revisa los logs de OkHttp para ver si el header se está agregando

---

## 📚 ARQUITECTURA DE AUTENTICACIÓN

### Flujo de Autenticación

1. **Login:**
   - Usuario ingresa credenciales
   - `UserRepository.login()` llama a `UserApi.login()`
   - El servidor responde con un token JWT
   - `UserRepository` guarda el token usando `UserPreferencesRepository.saveAuthToken()`

2. **Peticiones Autenticadas:**
   - Cualquier llamada a una API (excepto login/register)
   - `AuthInterceptor` intercepta la petición
   - Lee el token de `TokenManager`
   - Agrega el header `Authorization: Bearer <token>`
   - La petición continúa con el token incluido

3. **Logout:**
   - `UserRepository` o `UserPreferencesRepository.clearAll()`
   - Se limpia el token almacenado
   - Las siguientes peticiones no incluirán el token

### Componentes Clave

- **TokenManager**: Gestiona el almacenamiento y recuperación del token
- **AuthInterceptor**: Agrega automáticamente el token a las peticiones
- **UserPreferencesRepository**: Almacenamiento persistente usando DataStore
- **RemoteModule**: Configura Retrofit y OkHttp con el interceptor

---

## ✅ CHECKLIST DE CONFIGURACIÓN

- [ ] Proyecto abierto en Android Studio
- [ ] Gradle sincronizado sin errores
- [ ] JDK 17 configurado
- [ ] URLs de microservicios configuradas (emulador o dispositivo físico)
- [ ] Dependencias descargadas correctamente
- [ ] Estructura de carpetas verificada
- [ ] TokenManager.kt creado y funcionando
- [ ] AuthInterceptor.kt creado y funcionando
- [ ] RemoteModule.kt actualizado con AuthInterceptor
- [ ] APIs actualizadas (sin headers manuales de Authorization)
- [ ] Emulador creado o dispositivo físico conectado
- [ ] Microservicios corriendo en el backend
- [ ] App ejecutándose sin errores
- [ ] Login funcionando correctamente
- [ ] Token JWT guardándose y usándose en peticiones

---

## 🎉 ¡LISTO!

Si completaste todos los pasos, tu proyecto de Android Studio está configurado y listo para desarrollar.

**Próximos pasos:**

1. Implementar nuevas pantallas de UI con Jetpack Compose
2. Conectar los ViewModels con las pantallas
3. Implementar navegación entre pantallas
4. Agregar manejo de errores y estados de carga
5. Implementar pruebas unitarias y de integración

---

## 📝 NOTAS IMPORTANTES

### Cambios Realizados en esta Configuración

1. **TokenManager.kt**: Creado para gestionar tokens JWT de forma centralizada
2. **AuthInterceptor.kt**: Creado para agregar automáticamente el token a las peticiones
3. **RemoteModule.kt**: Actualizado para incluir el `AuthInterceptor` en el `OkHttpClient`
4. **AppModule.kt**: Actualizado para proporcionar `TokenManager`
5. **APIs**: Actualizadas para remover parámetros manuales de `@Header("Authorization")`
6. **Repositorios**: Actualizados para no pasar tokens manualmente

### Ventajas de esta Arquitectura

- **Centralización**: El manejo de tokens está centralizado en `TokenManager`
- **Automatización**: El `AuthInterceptor` agrega el token automáticamente
- **Mantenibilidad**: No necesitas agregar headers manualmente en cada llamada
- **Seguridad**: El token se almacena de forma segura usando DataStore
- **Flexibilidad**: Fácil de excluir rutas que no requieren autenticación

---

**¿Necesitas ayuda?** Revisa los logs en Logcat o los archivos de ejemplo incluidos en el proyecto.


