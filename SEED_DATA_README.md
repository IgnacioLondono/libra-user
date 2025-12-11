# Datos Precargados para Microservicios

Este directorio contiene todos los datos precargados de la aplicación Libra Users en formato JSON, listos para ser implementados en microservicios.

## Archivos Disponibles

### 1. `seed-data-for-microservices.json`
Archivo completo que contiene tanto libros como usuarios en un solo JSON.

### 2. `books-seed-data.json`
Archivo con solo los 34 libros precargados, incluyendo sus URLs de imágenes.

### 3. `users-seed-data.json`
Archivo con solo los 2 usuarios de demostración (Demo User y Admin User).

## Estructura de Datos

### Libros (Books)

Cada libro contiene los siguientes campos:

```json
{
  "id": 1,
  "title": "Título del libro",
  "author": "Autor del libro",
  "isbn": "ISBN del libro",
  "categoria": "Categoría del libro",
  "categoryId": 1,
  "publisher": "Editorial",
  "publishDate": "YYYY-MM-DD",
  "anio": 1949,
  "status": "Available | Loaned | Damaged | Retired",
  "inventoryCode": "Código de inventario",
  "stock": 5,
  "disponibles": 5,
  "descripcion": "Descripción del libro",
  "coverUrl": "URL de la imagen de portada",
  "homeSection": "Trending | Free | None"
}
```

**Estados posibles:**
- `Available`: Disponible
- `Loaned`: Prestado
- `Damaged`: Dañado
- `Retired`: Retirado

**Secciones de inicio:**
- `Trending`: Libros en tendencia
- `Free`: Libros gratuitos
- `None`: Sin sección especial

**Nota sobre URLs de imágenes:**
- Los libros con IDs 1-20 tienen URLs de imágenes completas
- Los libros con IDs 21-34 tienen `coverUrl` vacío (""), necesitarán agregar URLs de imágenes

### Usuarios (Users)

Cada usuario contiene los siguientes campos:

```json
{
  "name": "Nombre del usuario",
  "email": "email@ejemplo.com",
  "phone": "123456789",
  "password": "Contraseña en texto plano",
  "role": "USER | ADMIN"
}
```

**Usuarios precargados:**

1. **Demo User**
   - Email: `demo@duoc.cl`
   - Password: `Demo123!`
   - Role: `USER`

2. **Admin User**
   - Email: `admin123@gmail.com`
   - Password: `admin12345678`
   - Role: `ADMIN`

## Estadísticas

- **Total de libros:** 34
- **Libros con imágenes:** 20 (IDs 1-20)
- **Libros sin imágenes:** 14 (IDs 21-34)
- **Total de usuarios:** 2

## Categorías de Libros

Los libros están organizados en las siguientes categorías:

- **Ciencia** (categoryId: 1)
- **Ciencia Ficción** (categoryId: 2)
- **Literatura** (categoryId: 1, 2, 3)
- **Fantasía** (categoryId: 2, 4)
- **Juvenil** (categoryId: 3)
- **Historia** (categoryId: 3, 5)
- **Misterio** (categoryId: 4)
- **Suspenso** (categoryId: 4)
- **Terror** (categoryId: 4)

## Cómo Usar Estos Datos

### Para Microservicios REST

1. **Importar libros:**
   ```bash
   # Ejemplo con curl
   curl -X POST http://tu-microservicio/api/books/bulk \
     -H "Content-Type: application/json" \
     -d @books-seed-data.json
   ```

2. **Importar usuarios:**
   ```bash
   # Ejemplo con curl
   curl -X POST http://tu-microservicio/api/users/bulk \
     -H "Content-Type: application/json" \
     -d @users-seed-data.json
   ```

### Para Bases de Datos

Puedes usar estos JSONs para:
- Scripts de inserción masiva
- Migraciones de base de datos
- Seeders de datos iniciales
- Tests de integración

### Para APIs GraphQL

Los datos están estructurados de manera que pueden ser fácilmente convertidos a mutaciones GraphQL:

```graphql
mutation CreateBook($book: BookInput!) {
  createBook(book: $book) {
    id
    title
    author
  }
}
```

## Notas Importantes

1. **Contraseñas:** Las contraseñas están en texto plano. Asegúrate de hashearlas antes de almacenarlas en producción.

2. **IDs:** Los IDs están predefinidos. Si tu base de datos usa auto-incremento, puedes omitir el campo `id` y dejar que la base de datos los asigne.

3. **URLs de Imágenes:** Los libros 21-34 necesitan URLs de imágenes. Puedes:
   - Buscar imágenes en línea
   - Usar un servicio de almacenamiento de imágenes
   - Dejar el campo vacío y agregarlas después

4. **Validación:** Asegúrate de validar los datos antes de insertarlos en tu base de datos.

## Ejemplo de Uso en Node.js

```javascript
const fs = require('fs');
const data = JSON.parse(fs.readFileSync('seed-data-for-microservices.json', 'utf8'));

// Insertar libros
data.books.forEach(book => {
  // Lógica para insertar en tu base de datos
  console.log(`Insertando libro: ${book.title}`);
});

// Insertar usuarios
data.users.forEach(user => {
  // Hashear contraseña antes de insertar
  const hashedPassword = hashPassword(user.password);
  // Lógica para insertar en tu base de datos
  console.log(`Insertando usuario: ${user.email}`);
});
```

## Ejemplo de Uso en Python

```python
import json

with open('seed-data-for-microservices.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

# Insertar libros
for book in data['books']:
    print(f"Insertando libro: {book['title']}")
    # Lógica para insertar en tu base de datos

# Insertar usuarios
for user in data['users']:
    # Hashear contraseña antes de insertar
    hashed_password = hash_password(user['password'])
    print(f"Insertando usuario: {user['email']}")
    # Lógica para insertar en tu base de datos
```

## Soporte

Si necesitas ayuda para implementar estos datos en tus microservicios, consulta la documentación de tu framework o base de datos específica.



