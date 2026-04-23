# BookHub-back - API REST

## Présentation

API a été réalisée dans le cadre d'un projet de groupe au sein de l'école ENI.

Elle doit être couplée avec le front Angular BookHub-front.

API REST développée avec Spring Boot.

L'application permet de gérer une bibliothèque en ligne et couvre les fonctionnalités principales d’un système de gestion de bibliothèque :

- gestion des utilisateurs (authentification JWT)
- gestion des livres
- emprunts et retours
- réservations

---

## ⚙️ Installation & lancement

### 1. Cloner le projet dans un dossier

```bash
git clone https://github.com/TeamZJava/BookHub-back.git
cd bookhub-back
```

---

### 2. Configuration de la base de données

Suivre les indications dans le fichier [Jeu de données](Jeu_de_Données.md) pour initialiser la base.

### 3. Configuration `application.properties`

Le fichier se trouve dans `bookhub-api/src/main/resources/application.properties`.
Vérifie que :

```properties
# Connexion SQL Server
spring.datasource.url=jdbc:sqlserver://localhost;databasename=BookHub;integratedSecurity=false;encrypt=false;trustServerCertificate=false
spring.datasource.username=bookhub_admin
spring.datasource.password=BookHub2026!

# Hibernate — utiliser "create" pour la première initialisation, puis repasser en "update"
spring.jpa.hibernate.ddl-auto=update
```

### 4. Lancer l'application

Dans ton IDE, fais un clic droit sur la classe principale, puis sélectionne **Run**.

Ou sinon, dans ton terminal :

```bash
./gradlew bootRun
```

L'API démarre sur `http://localhost:8080`.

---

## Stack technique

| Technologie       | Version | Rôle                            |
| ----------------- | ------- | ------------------------------- |
| Java              | 21      | Langage                         |
| Spring Boot       | 4.0.5   | Framework principal             |
| Spring Security   | —       | Authentification & autorisation |
| Spring Data JPA   | —       | Accès base de données           |
| SQL Server        | 2019+   | Base de données                 |
| JWT (jjwt)        | 0.13.0  | Gestion des tokens              |
| MapStruct         | 1.5.5   | Mapping entités ↔ DTO           |
| Lombok            | —       | Réduction du boilerplate        |
| SpringDoc OpenAPI | 3.0.2   | Documentation Swagger           |
| Gradle            | —       | Build                           |
| JUnit / H2        | —       | Tests (base en mémoire)         |

---

## Architecture

```
bookhub-api/src/main/java/fr/eni/bookhub/
├── bo/                  # Entités JPA (User, Book, Loan, Reservation, ...)
│   └── enums/           # Enums (Role, LoanStatus, ReservationStatus)
├── dal/                 # Repositories Spring Data JPA
├── bll/                 # Services métier (interfaces + implémentations)
├── controller/          # Contrôleurs REST
├── dto/                 # Objets de transfert (request / response)
├── mapper/              # MapStruct (entités ↔ DTO)
├── security/            # Configuration Spring Security + filtre JWT
│   └── jwt/             # JwtService, JwtAuthenticationFilter
└── errors/              # Exceptions métier + handler global
```

---

## Endpoints principaux

### Authentification — `/api/auth`

### Utilisateurs — `/api/users`

### Livres — `/api/books`

### Emprunts — `/api/loans`

### Réservations — `/api/reservations`

### Favoris — `/api/favorites`

## Documentation Swagger pour plus de détails

Une fois l'application démarrée, la documentation interactive est disponible à :

```
http://localhost:8080/swagger-ui/index.html
```

---

## Sécurité

L'API utilise **JWT (JSON Web Token)** en mode stateless.

- À la connexion, le back génère un token signé contenant l'email et le rôle de l'utilisateur.
- Le token est valable **24h**.
- Chaque requête protégée doit inclure le header : `Authorization: Bearer <token>`
- Le filtre `JwtAuthenticationFilter` valide le token à chaque requête avant d'atteindre les contrôleurs.

### Rôles

| Rôle        | Droits                                                       |
| ----------- | ------------------------------------------------------------ |
| `USER`      | Catalogue, emprunts, réservations, favoris, profil           |
| `LIBRARIAN` | USER + gestion des livres, validation des retours, dashboard |
| `ADMIN`     | LIBRARIAN + gestion des comptes utilisateurs                 |

---

## Comptes de test

| Rôle      | Email                | Mot de passe |
| --------- | -------------------- | ------------ |
| USER      | marie@bookhub.fr     | `User1234`   |
| USER      | lucas@bookhub.fr     | `User1234`   |
| LIBRARIAN | librarian@bookhub.fr | `User1234`   |
| ADMIN     | admin@bookhub.fr     | `User1234`   |
