# BookHub — Lancement local (back + front)

## Prérequis

- **Java** (version du projet : toolchain Gradle, ex. 21) et **Gradle** installés.
- **Node.js** + **npm** (pour Angular).
- **SQL Server** accessible, avec une base créée (ex. `BookHub`) et un utilisateur SQL autorisé sur cette base.

---

## 1. Backend (`bookhub-api`)

### Installation

- Ouvrir un terminal dans :  
  `BookHub-back-main/bookhub-api/`
- Première fois : Gradle télécharge les dépendances au premier `bootRun` (connexion Internet utile).

### Configuration

Fichier : `bookhub-api/src/main/resources/application.properties`

- **`spring.datasource.url`** : serveur, nom de base, options (`encrypt`, `trustServerCertificate`, etc.).
- **`spring.datasource.username`** / **`spring.datasource.password`** : identifiants SQL Server.
- **`spring.jpa.hibernate.ddl-auto`** : en dev souvent `update` ; pour recréer le schéma une fois, `create` puis repasser à `update` (voir procédure équipe / scripts SQL).

### Lancement

```bash
cd BookHub-back-main/bookhub-api
gradle bootRun
```

- API par défaut : **http://localhost:8080**
- Si Swagger est activé dans le projet : http://localhost:8080/swagger-ui/index.html

---

## 2. Frontend (`BookHub-front-main`)

### Installation

```bash
cd BookHub-front-main
npm install
```

### Configuration

- Les services appellent le back en **http://localhost:8080** (URLs dans `src/app/services/*.ts`). Si le back tourne ailleurs ou sur un autre port, adapter ces URLs.
- **CORS** : le back doit autoriser l’origine du front (**http://localhost:4200**) — déjà prévu côté Spring dans la config sécurité du projet.

### Lancement

```bash
cd BookHub-front-main
npm start
```

ou :

```bash
ng serve
```

- Application : **http://localhost:4200**

---

## 3. Ordre recommandé

1. Démarrer **SQL Server** et vérifier la base + `application.properties`.
2. Lancer le **backend** (`gradle bootRun`).
3. Lancer le **frontend** (`ng serve` / `npm start`).
4. Ouvrir le navigateur sur **http://localhost:4200**, se connecter

---

## 4. Données de test

- Scripts / jeux de données : voir `BookHub-back-main/Reset_et_recreate Jeu_de_Données.md`
