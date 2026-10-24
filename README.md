# 📚 BookTrack - Desafío Práctico 3
### Universidad Don Bosco — Facultad de Ingeniería — Escuela de Computación
**Asignatura:** DSM441 - Desarrollo de Software para Móviles  
**Ciclo:** 02 2026 | **Grupo:** 01T  
**Docente:** Mario Meléndez  
**Estudiante:** Jorge Luis Lugo González  
**Carnet:** LG242867 

---

## 📖 Descripción del Proyecto
**BookTrack** es una aplicación móvil desarrollada en Android (Kotlin) diseñada para ayudar a los usuarios a registrar sus lecturas, controlar su progreso diario, calificar sus libros terminados y comparar su avance contra una meta anual personalizada.

La aplicación integra autenticación mediante **Firebase Authentication** y almacenamiento persistente local utilizando **SQLite**, garantizando la privacidad de los datos para que cada usuario únicamente pueda visualizar y gestionar sus propios registros.

---

## ✨ Funcionalidades Principales

### 1. 🔐 Registro y Autenticación de Usuarios
- Registro e inicio de sesión con correo electrónico y contraseña mediante **Firebase Authentication**.
- **Validaciones previas al envío**:
  - Formato de correo electrónico válido.
  - Contraseña de al menos 6 caracteres.
  - Confirmación de contraseña en el formulario de registro.
- **Persistencia de sesión**: el usuario no pierde la sesión si sale o cierra la aplicación.
- **Cierre de sesión** seguro con diálogo de confirmación.

### 2. 🏠 Pantalla Principal
- Saludo personalizado con el correo o nombre del usuario autenticado.
- **Tarjeta de Meta Anual**:
  - Muestra la meta de libros del año actual, la cantidad de libros terminados y los que faltan para cumplirla.
  - Barra de progreso visual interactiva.
  - Diálogo para definir o modificar la meta (validado: número entero mayor que 0).
- **Libros en Lectura**:
  - Listado de libros en estado *Leyendo* con su porcentaje de avance y páginas leídas.
  - Mensaje descriptivo si no hay lecturas activas.
- **Botones de navegación rápida**: para registrar un libro, ver la biblioteca completa y consultar las estadísticas.

### 3. 📝 Registro y Edición de Libros
- Formulario de registro y edición con los campos:
  - **Título** (obligatorio, de 2 a 60 caracteres).
  - **Autor** (obligatorio, de 2 a 60 caracteres).
  - **4 Géneros Obligatorios**: *Novela*, *Ciencia ficción*, *Historia*, *Desarrollo personal*.
  - **Total de páginas** (entero mayor que 0).
  - **Páginas leídas** (entero entre 0 y el total de páginas).
  - **Estados**: *Por leer*, *Leyendo*, *Terminado*.
  - **Calificación (1 a 5 estrellas)**: sólo se habilita y es obligatoria cuando el estado es *Terminado*.
- Mensajes de error específicos indicando qué campo es inválido y el motivo.
- Registro automático de fecha y timestamp al marcar un libro como *Terminado*.
- Opción para eliminar libros existentes.

### 4. 📚 Biblioteca
- Lista desplazable (`RecyclerView`) con todos los libros registrados por el usuario.
- Información por elemento: título, autor, género con etiqueta de color, estado, porcentaje de avance y calificación en estrellas (si está terminado).
- Chips de filtrado rápido (*Todos, Por leer, Leyendo, Terminado*).
- Mensaje amigable de estado vacío si no hay libros registrados.
- Acceso directo a la edición de cualquier libro al seleccionarlo.

### 5. 📊 Estadísticas Anuales
- Cantidad de libros terminados en el año actual desglosados por los 4 géneros obligatorios (*Novela, Ciencia ficción, Historia, Desarrollo personal*).
- Indicador del **género más leído** del año.
- **Total de páginas leídas** en el año.
- **Libro mejor calificado del año**, aplicando la regla de desempate por fecha (el que se terminó primero).

---

## 🛠️ Tecnologías Utilizadas
- **Lenguaje:** Kotlin
- **Plataforma:** Android SDK 37 (MinSdk: 24)
- **UI:** Material Design 3, CoordinatorLayout, ConstraintLayout, Vector Drawables
- **Autenticación:** Firebase Authentication
- **Base de Datos:** SQLite (`SQLiteOpenHelper`) con filtrado por `userId`
- **Build System:** Gradle 9.6.0 & Android Gradle Plugin 9.4.1

---

## 🚀 Instrucciones de Ejecución

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Jorge-Luis-Lugo-Gonzalez/Desafio03-DSM
