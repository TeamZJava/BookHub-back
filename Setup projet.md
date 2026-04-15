# BookHub — Setup du projet

---

## Prérequis

- **JDK 21**
- **IntelliJ IDEA**
- **SQL Server ou image Docker de SQL Server** installé et démarré en local
- **Postman**

---

## 1. Récupérer le projet

Clone le dépôt Git et ouvre-le dans ton IDE.

---

## 2. Configurer la base de données

### 2.1 Créer le login et la base

Exécute ce script dans SQL Server Management Studio (SSMS) :

```sql
-- Création du login serveur
CREATE LOGIN bookhub_admin
WITH PASSWORD = 'BookHub2026!';

-- Créer la bdd
CREATE DATABASE BookHub;

-- Se placer dessus
USE BookHub;

-- Créer l'utilisateur lié au login
CREATE USER bookhub_admin FOR LOGIN bookhub_admin;

-- Donner les droits complets (équivalent sa)
ALTER SERVER ROLE [sysadmin] ADD MEMBER bookhub_admin;
```

### 2.2 Vérifier la connexion

Il est possible de vérifier directement dans Intellij la connexion via l'onglet "Database" à droite
Ou bien avec SQL Server Studio (pas dispo sur mac lol)

Identifiants :
- **Login** : `bookhub_admin`
- **Mot de passe** : `BookHub2026!`

---

## 3. Configurer le projet

Le fichier `application.properties` est déjà configuré dans le projet.

```properties
# Base de données
spring.datasource.url=jdbc:sqlserver://localhost;databasename=BookHub;integratedSecurity=false;encrypt=false;trustServerCertificate=false
spring.datasource.username=bookhub_admin
spring.datasource.password=BookHub2026!
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Logs Spring Security (debug)
logging.level.org.springframework.security=DEBUG

# Clé secrète JWT
app.jwt.secret=393a3b433f732725296e2b56397133307839403923456a34433e57794a6f2b63
```

> Ne pas toucher a la clé JWT, on doit tous avoir la même

---

## 4. Lancer le projet une première fois

Lancer l'application pour générer la table **`users`**

---

## 5. Insérer l'utilisateur admin

Une fois la table créée, exécute ce script pour insérer le compte admin :

```sql
USE BookHub;

INSERT INTO users (
    email,
    password,
    first_name,
    last_name,
    phone,
    role,
    inscription_date,
    active,
    tos_acceptation_date
)
VALUES (
           'admin@bookhub.fr',
           '{bcrypt}$2a$10$KQHvqgijKFG3joJZMu7NXezoqyfAeFO96QLy2Cne4vCD3weqgTkeq',
           'Admin',
           'BookHub',
           NULL,
           'ADMIN',
           GETDATE(),
           1,
           GETDATE()
       );
```

> Le hash correspond au mot de passe **`password`** encodé en BCrypt.

---

## 6. Tester avec Postman

### Login

| Champ | Valeur |
|-------|--------|
| Méthode | `POST` |
| URL | `http://localhost:8080/api/auth/login` |
| Body | `raw` → `JSON` |

```json
{
    "email": "admin@bookhub.fr",
    "password": "password"
}
```

**Réponse attendue (200 OK) :**

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```


### Test route /api avec le token généré (accessible uniquement via le rôle ADMIN)

Copier le token et le mettre dans le Bearer


| Champ | Valeur                      |
|-------|-----------------------------|
| Méthode | `GET`                       |
| URL | `http://localhost:8080/api` |


**Réponse attendue (200 OK) :**

```
Bienvenue sur l'API du projet BookHub !
```

---
