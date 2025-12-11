# 📱 Prompt para Cursor: Adaptar Microservicios para Android sin Room/SQLite

## 🎯 Objetivo

Adaptar los microservicios backend para que funcionen correctamente con la aplicación Android que ahora obtiene **TODOS los datos directamente desde los microservicios**, sin caché local (Room/SQLite). La app Android ahora realiza todas las operaciones (crear, leer, actualizar, eliminar) directamente en la base de datos de los microservicios.

## 📋 Contexto de Cambios en Android

La aplicación Android ha sido refactorizada para:
- ✅ **Eliminar Room/SQLite completamente** - No hay base de datos local
- ✅ **Obtener todos los datos desde microservicios** - Todo viene del API
- ✅ **Operaciones directas en base de datos** - Crear, actualizar, eliminar se hacen primero en el API
- ✅ **Estado en memoria solo** - Los datos se mantienen en memoria durante la sesión

## 🔧 Cambios Requeridos en Microservicios

### 1. **Book Service** - Endpoints Necesarios

#### Endpoints que DEBEN existir y funcionar correctamente:

```java
// ✅ Ya existe - Verificar que funcione correctamente
GET    /api/books                    // Listar todos los libros (con paginación)
GET    /api/books/{bookId}           // Obtener libro por ID
POST   /api/books                    // Crear libro
PUT    /api/books/{bookId}           // Actualizar libro
DELETE /api/books/{bookId}          // Eliminar libro (CRÍTICO: debe eliminar de BD)
GET    /api/books/search?q={query}   // Buscar libros
GET    /api/books/category/{category} // Libros por categoría
GET    /api/books/{bookId}/availability // Disponibilidad del libro
GET    /api/books/featured          // Libros destacados
```

#### Cambios Críticos:

1. **DELETE /api/books/{bookId}**:
   - Debe eliminar el libro de la base de datos
   - Retornar 200 OK si se elimina exitosamente
   - Retornar 404 si el libro no existe
   - Retornar 400/403 si hay préstamos activos asociados

2. **PUT /api/books/{bookId}**:
   - Debe actualizar el libro en la base de datos
   - Validar que el libro exista
   - Actualizar todos los campos enviados
   - Retornar el libro actualizado

3. **POST /api/books**:
   - Crear libro en la base de datos
   - Validar datos de entrada
   - Retornar el libro creado con ID generado

### 2. **User Service** - Endpoints Necesarios

#### Endpoints que DEBEN existir:

```java
// ✅ Ya existe - Verificar que funcione
POST   /api/users/register           // Registrar usuario
POST   /api/users/login              // Login
GET    /api/users/{userId}           // Obtener usuario por ID
PUT    /api/users/{userId}           // Actualizar usuario
GET    /api/users                    // Listar todos los usuarios (NUEVO - necesario)
DELETE /api/users/{userId}           // Eliminar usuario (NUEVO - necesario)
POST   /api/users/validate-token     // Validar token
POST   /api/users/logout             // Logout
```

#### Cambios Críticos:

1. **GET /api/users** (NUEVO):
   ```java
   @GetMapping("/api/users")
   public ResponseEntity<List<UserDto>> getAllUsers() {
       // Retornar todos los usuarios de la base de datos
       // Sin paginación o con paginación opcional
   }
   ```

2. **DELETE /api/users/{userId}** (NUEVO):
   ```java
   @DeleteMapping("/api/users/{userId}")
   public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
       // Eliminar usuario de la base de datos
       // Validar que no tenga préstamos activos
       // Retornar 200 OK si se elimina exitosamente
       // Retornar 400 si tiene préstamos activos
       // Retornar 404 si no existe
   }
   ```

### 3. **Loan Service** - Endpoints Necesarios

#### Endpoints que DEBEN existir:

```java
// ✅ Ya existe - Verificar que funcione
POST   /api/loans                    // Crear préstamo
GET    /api/loans/user/{userId}      // Préstamos de un usuario
GET    /api/loans/user/{userId}/active // Préstamos activos de un usuario
POST   /api/loans/{loanId}/return    // Devolver préstamo
PATCH  /api/loans/{loanId}/extend    // Extender préstamo
```

#### Cambios Críticos:

1. **POST /api/loans/{loanId}/return**:
   - Debe actualizar el préstamo en la base de datos
   - Cambiar status a "Returned"
   - Establecer returnDate
   - Actualizar disponibilidad del libro asociado
   - Retornar el préstamo actualizado

2. **PATCH /api/loans/{loanId}/extend**:
   - Debe actualizar la fecha de devolución en la base de datos
   - Validar que la nueva fecha sea posterior a la actual
   - Retornar el préstamo actualizado

3. **POST /api/loans**:
   - Crear préstamo en la base de datos
   - Validar disponibilidad del libro
   - Actualizar disponibilidad del libro (disponibles - 1)
   - Retornar el préstamo creado

### 4. **Notification Service** - Endpoints Necesarios

#### Endpoints que DEBEN existir:

```java
GET    /api/notifications/user/{userId}              // Notificaciones de un usuario
GET    /api/notifications/user/{userId}/unread-count // Contador de no leídas
PATCH  /api/notifications/{notificationId}/read      // Marcar como leída
PATCH  /api/notifications/user/{userId}/read-all    // Marcar todas como leídas (NUEVO)
PATCH  /api/notifications/{notificationId}          // Eliminar notificación (NUEVO)
PATCH  /api/notifications/user/{userId}/delete-all  // Eliminar todas (NUEVO)
```

#### Cambios Críticos:

1. **PATCH /api/notifications/user/{userId}/read-all** (NUEVO):
   ```java
   @PatchMapping("/api/notifications/user/{userId}/read-all")
   public ResponseEntity<Void> markAllAsRead(@PathVariable String userId) {
       // Marcar todas las notificaciones del usuario como leídas
       // Actualizar en base de datos
   }
   ```

2. **PATCH /api/notifications/{notificationId}** (NUEVO - para DELETE):
   ```java
   @DeleteMapping("/api/notifications/{notificationId}")
   public ResponseEntity<Void> deleteNotification(@PathVariable String notificationId) {
       // Eliminar notificación de la base de datos
   }
   ```

3. **PATCH /api/notifications/user/{userId}/delete-all** (NUEVO):
   ```java
   @DeleteMapping("/api/notifications/user/{userId}/delete-all")
   public ResponseEntity<Void> deleteAllNotifications(@PathVariable String userId) {
       // Eliminar todas las notificaciones del usuario
   }
   ```

## 📊 Estructura de Datos Esperada

### BookDto (Respuesta del API)

```java
{
  "id": "1",                    // String (no Long)
  "title": "1984",
  "author": "George Orwell",
  "isbn": "9788497593793",
  "category": "Ciencia",        // Mapeo de "categoria"
  "categoryId": 1,
  "publisher": "Debolsillo",
  "publishDate": "1949-06-08",
  "year": 1949,                // Mapeo de "anio"
  "status": "Available",
  "inventoryCode": "C001",
  "totalCopies": 5,             // Mapeo de "stock"
  "availableCopies": 5,         // Mapeo de "disponibles"
  "description": "Una distopía...",
  "coverUrl": "https://...",
  "featured": true              // true si homeSection es "Trending" o "Free"
}
```

### UserDto (Respuesta del API)

```java
{
  "id": "1",
  "name": "Demo User",
  "email": "demo@duoc.cl",
  "phone": "912345678",
  "role": "USER",               // "USER" o "ADMIN"
  "status": "active",
  "profileImageUri": "https://..."
}
```

### LoanDto (Respuesta del API)

```java
{
  "id": "1",
  "userId": "1",
  "bookId": "1",
  "loanDate": "2024-01-15",
  "dueDate": "2024-01-22",
  "returnDate": null,           // null si no se ha devuelto
  "status": "Active"            // "Active", "Returned", "Overdue", "Cancelled"
}
```

### NotificationDto (Respuesta del API)

```java
{
  "id": "1",
  "userId": "1",
  "type": "INFO",
  "title": "Préstamo creado",
  "message": "Se ha registrado tu préstamo #1.",
  "read": false,
  "priority": "LOW",
  "createdAt": "1704067200000"  // Timestamp en millis como String
}
```

## 🔄 Flujo de Operaciones

### Crear Libro:
1. Android llama: `POST /api/books` con BookDto
2. Microservicio: Valida datos → Crea en BD → Retorna BookDto con ID
3. Android: Solo si respuesta es exitosa, actualiza estado en memoria

### Actualizar Libro:
1. Android llama: `PUT /api/books/{bookId}` con BookDto actualizado
2. Microservicio: Valida → Actualiza en BD → Retorna BookDto actualizado
3. Android: Solo si respuesta es exitosa, actualiza estado en memoria

### Eliminar Libro:
1. Android llama: `DELETE /api/books/{bookId}`
2. Microservicio: Valida → Elimina de BD → Retorna 200 OK
3. Android: Solo si respuesta es exitosa, elimina de estado en memoria

### Crear Préstamo:
1. Android llama: `POST /api/loans` con CreateLoanRequestDto
2. Microservicio: 
   - Valida disponibilidad del libro
   - Crea préstamo en BD
   - Actualiza disponibles del libro (disponibles - 1)
   - Retorna LoanDto creado
3. Android: Solo si respuesta es exitosa, actualiza estado en memoria

### Devolver Préstamo:
1. Android llama: `POST /api/loans/{loanId}/return`
2. Microservicio:
   - Actualiza préstamo (status = "Returned", returnDate = ahora)
   - Actualiza disponibles del libro (disponibles + 1)
   - Retorna LoanDto actualizado
3. Android: Solo si respuesta es exitosa, actualiza estado en memoria

## ✅ Checklist de Implementación

### Book Service:
- [ ] Verificar que DELETE /api/books/{bookId} elimine de la BD
- [ ] Verificar que PUT /api/books/{bookId} actualice en la BD
- [ ] Verificar que POST /api/books cree en la BD
- [ ] Validar que los campos coincidan con BookDto esperado
- [ ] Manejar errores correctamente (404, 400, 500)

### User Service:
- [ ] Implementar GET /api/users (listar todos)
- [ ] Implementar DELETE /api/users/{userId} (eliminar usuario)
- [ ] Validar que no se elimine usuario con préstamos activos
- [ ] Verificar que PUT /api/users/{userId} actualice en la BD
- [ ] Manejar errores correctamente

### Loan Service:
- [ ] Verificar que POST /api/loans/{loanId}/return actualice BD y libro
- [ ] Verificar que PATCH /api/loans/{loanId}/extend actualice BD
- [ ] Verificar que POST /api/loans cree en BD y actualice libro
- [ ] Validar disponibilidad antes de crear préstamo
- [ ] Manejar errores correctamente

### Notification Service:
- [ ] Implementar PATCH /api/notifications/user/{userId}/read-all
- [ ] Implementar DELETE /api/notifications/{notificationId}
- [ ] Implementar DELETE /api/notifications/user/{userId}/delete-all
- [ ] Verificar que GET /api/notifications/user/{userId} retorne desde BD
- [ ] Manejar errores correctamente

## 🚨 Validaciones Importantes

1. **Eliminar Libro**:
   - No permitir eliminar si hay préstamos activos
   - Retornar error descriptivo si no se puede eliminar

2. **Eliminar Usuario**:
   - No permitir eliminar si tiene préstamos activos
   - Retornar error descriptivo

3. **Crear Préstamo**:
   - Validar que el libro tenga disponibles > 0
   - Validar que el usuario exista
   - Validar que el usuario no tenga ya un préstamo activo del mismo libro

4. **Devolver Préstamo**:
   - Validar que el préstamo exista
   - Validar que el préstamo esté activo
   - Actualizar correctamente la disponibilidad del libro

## 📝 Notas de Implementación

1. **Todas las operaciones deben ser transaccionales**: Si falla alguna parte, hacer rollback
2. **Manejo de errores consistente**: Usar códigos HTTP estándar (200, 201, 400, 404, 500)
3. **Validaciones en el backend**: No confiar solo en validaciones del cliente
4. **Logging**: Registrar todas las operaciones importantes para debugging
5. **Respuestas consistentes**: Siempre retornar DTOs completos después de operaciones

## 🔍 Testing

Después de implementar, verificar:

1. **Swagger/Postman**:
   - Probar todos los endpoints nuevos
   - Verificar que las operaciones se reflejen en la BD
   - Verificar códigos de respuesta correctos

2. **Base de Datos**:
   - Verificar que los datos se guarden correctamente
   - Verificar que las eliminaciones funcionen
   - Verificar que las actualizaciones se reflejen

3. **Integración con Android**:
   - Probar crear/actualizar/eliminar desde la app
   - Verificar que los cambios se reflejen en la BD
   - Verificar manejo de errores

## 📌 Archivos de Referencia

El archivo `seed-data-for-microservices.json` contiene todos los datos de ejemplo que la app Android puede cargar. Los microservicios deben poder recibir estos datos mediante los endpoints de bulk load si están implementados, o mediante los endpoints individuales de creación.

---

**IMPORTANTE**: Todos los cambios deben garantizar que las operaciones se realicen **PRIMERO en la base de datos del microservicio**, y solo retornar éxito si la operación en la BD fue exitosa. La app Android depende completamente de estos endpoints para funcionar correctamente.



