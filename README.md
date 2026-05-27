# MINIMARKET PLUS

Backend REST para la gestión de un minimarket, desarrollado con Spring Boot 3 y Spring Security (Arquitectura Stateless con JWT).

## Requisitos
* Java 17
* Maven (incluido via `./mvnw`)

## Ejecución local
```bash
cd minimarket
./mvnw spring-boot:run
```
La aplicación queda disponible en http://localhost:8080.


AUTENTICACION Y PRUEBAS (JWT)
La API utiliza JSON WEB TOKENS (JWT). Para acceder a rutas protegidas, debes enviar el token en la cabcera HTTP.

-  Obtener Token (login):
   
curl -X POST http://localhost:8080/api/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"admin", "password":"admin123"}'

-  Consumir endopoint protegido:

curl -X GET http://localhost:8080/api/ventas \
-H "Authorization: Bearer <TU_TOKEN_AQUI>"

 
 BASE DE DATOS (entorno local)
   Consola H2: http://localhost:8080/h2-console
   JDBC URL: jdbc:h2:mem:minimarketdb
   Usuario: sa
   contraseña: (vacia)


##   USUARIOS DE PRUEBA:  
| Usuario   | Contraseña    | Rol      |
|-----------|---------------|----------|
| admin     | admin123      | ADMIN    |
| gerente   | gerente123    | GERENTE  |
| empleado  | empleado123   | EMPLEADO |
| cliente   | cliente123    | CLIENTE  |


##ROLES Y PERMISOS:

| Recurso | Público | CLIENTE | EMPLEADO | GERENTE | ADMIN |
|---------|---------|---------|----------|---------|-------|
| GET productos / categorías | Si | Si | Si | Si | Si |
| POST/PUT/DELETE productos | — | — | — | Si | Si |
| POST/PUT/DELETE categorías | — | — | — | Si | Si |
| Carrito | — | Si | — | — | Si |
| GET inventario | — | — | Si | Si | Si |
| POST/PUT/DELETE inventario | — | — | — | Si | Si |
| Ventas / detalle ventas | — | — | Si | Si | Si |
| Usuarios | — | — | — | — | Si |
| /public/** | Si | Si | Si | Si | Si |

##TEST:
``` bash
cd minimarket
./mvnw clean test
```
   
 
