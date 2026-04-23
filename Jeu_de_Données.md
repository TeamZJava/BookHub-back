# BookHub — Documentation complète : BDD + Jeu de données

---

## 1. Créer la base de données dans SQL Server (SSMS)

### Première fois (BDD inexistante)

1. Ouvrir **SQL Server Management Studio (SSMS)**
2. Se connecter au serveur local (ex : `localhost` ou `.\SQLEXPRESS`)
3. Ouvrir une nouvelle fenêtre de requête (`Ctrl+N`)
4. Exécuter :

```sql
CREATE DATABASE BookHub;
GO
```

5. Créer le login applicatif :

```sql
CREATE LOGIN bookhub_admin WITH PASSWORD = 'BookHub2026!';
GO
USE BookHub;
GO
CREATE USER bookhub_admin FOR LOGIN bookhub_admin;
GO
ALTER ROLE db_owner ADD MEMBER bookhub_admin;
GO
```

> Ces identifiants correspondent à ceux dans `application.properties` :
> `spring.datasource.username=bookhub_admin`
> `spring.datasource.password=BookHub2026!`

---

## 2. Créer les tables via Spring Boot (ddl-auto)

### Étape 1 — Passer en `create` dans `application.properties`

```properties
spring.jpa.hibernate.ddl-auto=create
```

> `create` = Hibernate **supprime** les tables existantes et les **recrée** à chaque démarrage de l'app à partir des entités JPA (`@Entity`). C'est utile pour initialiser proprement la structure.

### Étape 2 — Lancer le back

```bash
./gradlew bootRun
```

Les tables sont créées automatiquement. Stopper l'app dès qu'elle est démarrée.

### Étape 3 — Repasser en `update`

```properties
spring.jpa.hibernate.ddl-auto=update
```

> `update` = Hibernate **compare** les entités avec la BDD existante et **n'applique que les différences** (nouvelles colonnes, nouveaux index...). Il ne supprime rien. C'est le mode à garder en développement pour ne pas perdre les données à chaque redémarrage.

> **Attention** : en production on n'utilise ni `create` ni `update` — on utilise des outils de migration dédiés (Flyway, Liquibase).

---

## 3. Reset complet — Script SSMS (drop + recreate + données)

> À exécuter **tel quel** dans SSMS sur la base `BookHub`.
> Le script drop toutes les tables, les recrée et insère un jeu de données complet.

### Comptes de test

| Rôle      | Email                     | Prénom | Mot de passe |
|-----------|---------------------------|--------|--------------|
| USER      | marie@bookhub.fr          | Marie  | `User1234`   |
| USER      | lucas@bookhub.fr          | Lucas  | `User1234`   |
| USER      | emma@bookhub.fr           | Emma   | `User1234`   |
| USER      | thomas@bookhub.fr         | Thomas | `User1234`   |
| USER      | chloe@bookhub.fr          | Chloé  | `User1234`   |
| USER      | paul@bookhub.fr           | Paul   | `User1234`   |
| LIBRARIAN | librarian@bookhub.fr      | Ahmed  | `User1234`   |
| ADMIN     | admin@bookhub.fr          | Sophie | `User1234`   |

> **Note** : le mot de passe `User1234` est inséré directement en base avec un hash bcrypt (coût 12).
> L'insertion directe contourne la validation API (`@Pattern` du `RegisterRequest`).
> Pour tester l'inscription via le formulaire, utiliser un mot de passe valide :
> 12 caractères minimum, 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial.
> Exemple : `BookHub2026!@`

---

```sql
USE [BookHub];
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRAN;

    -- =============================================
    -- DROP tables (ordre FK)
    -- =============================================
    DROP TABLE IF EXISTS dbo.favorites;
    DROP TABLE IF EXISTS dbo.ratings;
    DROP TABLE IF EXISTS dbo.comments;
    DROP TABLE IF EXISTS dbo.reservations;
    DROP TABLE IF EXISTS dbo.loans;
    DROP TABLE IF EXISTS dbo.books;
    DROP TABLE IF EXISTS dbo.users;

    -- =============================================
    -- CREATE tables
    -- =============================================

    CREATE TABLE dbo.users (
        id_user              INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_users PRIMARY KEY,
        email                NVARCHAR(255)     NOT NULL CONSTRAINT uq_users_email UNIQUE,
        password             NVARCHAR(255)     NOT NULL,
        first_name           NVARCHAR(100)     NOT NULL,
        last_name            NVARCHAR(100)     NOT NULL,
        phone                NVARCHAR(12)      NULL,
        role                 NVARCHAR(20)      NOT NULL,
        inscription_date     DATETIME2         NOT NULL,
        active               BIT               NOT NULL,
        tos_acceptation_date DATETIME2         NULL
    );

    CREATE TABLE dbo.books (
        id_book          INT           PRIMARY KEY IDENTITY(1,1),
        title            NVARCHAR(255) NOT NULL,
        author           NVARCHAR(255) NOT NULL,
        isbn             NVARCHAR(20)  NOT NULL UNIQUE,
        description      NVARCHAR(2000),
        category         NVARCHAR(100),
        cover_url        NVARCHAR(500),
        total_copies     INT           NOT NULL DEFAULT 1,
        available_copies INT           NOT NULL DEFAULT 1,
        added_at         DATETIME2     NOT NULL,
        avg_rating       DECIMAL(3,2)  NOT NULL DEFAULT 0.00
    );

    CREATE TABLE dbo.loans (
        id_loan     INT          PRIMARY KEY IDENTITY(1,1),
        id_user     INT          NOT NULL,
        id_book     INT          NOT NULL,
        loan_date   DATETIME2    NOT NULL,
        due_date    DATETIME2    NOT NULL,
        return_date DATETIME2,
        status      NVARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
        CONSTRAINT fk_loans_user FOREIGN KEY (id_user) REFERENCES dbo.users(id_user),
        CONSTRAINT fk_loans_book FOREIGN KEY (id_book) REFERENCES dbo.books(id_book)
    );

    CREATE TABLE dbo.reservations (
        id_reservation   INT          PRIMARY KEY IDENTITY(1,1),
        id_user          INT          NOT NULL,
        id_book          INT          NOT NULL,
        reservation_date DATETIME2    NOT NULL,
        rank_in_line     INT          NOT NULL,
        status           NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
        CONSTRAINT fk_reservations_user FOREIGN KEY (id_user) REFERENCES dbo.users(id_user),
        CONSTRAINT fk_reservations_book FOREIGN KEY (id_book) REFERENCES dbo.books(id_book)
    );

    CREATE TABLE dbo.ratings (
        id_rating   INT       PRIMARY KEY IDENTITY(1,1),
        id_user     INT       NOT NULL,
        id_book     INT       NOT NULL,
        note        INT       NOT NULL CHECK (note BETWEEN 1 AND 5),
        rating_date DATETIME2 NOT NULL,
        CONSTRAINT fk_ratings_user FOREIGN KEY (id_user) REFERENCES dbo.users(id_user),
        CONSTRAINT fk_ratings_book FOREIGN KEY (id_book) REFERENCES dbo.books(id_book)
    );

    CREATE TABLE dbo.comments (
        id_comment   INT            PRIMARY KEY IDENTITY(1,1),
        id_user      INT            NOT NULL,
        id_book      INT            NOT NULL,
        comment      NVARCHAR(1000) NOT NULL,
        reported     BIT            NOT NULL DEFAULT 0,
        comment_date DATETIME2      NOT NULL,
        CONSTRAINT fk_comments_user FOREIGN KEY (id_user) REFERENCES dbo.users(id_user),
        CONSTRAINT fk_comments_book FOREIGN KEY (id_book) REFERENCES dbo.books(id_book)
    );

    CREATE TABLE dbo.favorites (
        id_user INT NOT NULL,
        id_book INT NOT NULL,
        CONSTRAINT pk_favorites PRIMARY KEY (id_user, id_book),
        CONSTRAINT fk_favorites_user FOREIGN KEY (id_user) REFERENCES dbo.users(id_user),
        CONSTRAINT fk_favorites_book FOREIGN KEY (id_book) REFERENCES dbo.books(id_book)
    );

    -- =============================================
    -- INSERT users
    -- hash bcrypt coût 12 = mot de passe : User1234
    -- =============================================
    INSERT INTO dbo.users (email, password, first_name, last_name, phone, role, inscription_date, active, tos_acceptation_date)
    VALUES
        ('marie@bookhub.fr',
         '{bcrypt}$2b$12$EEsHH8vDWXbL0xojxN.ViOHc2LbhmQATmc3JvcMIN/5K0rPUs7BQK',
         'Marie', 'Dupont', '0600000001', 'USER', DATEADD(day,-120,GETDATE()), 1, DATEADD(day,-120,GETDATE())),

        ('lucas@bookhub.fr',
         '{bcrypt}$2b$12$EEsHH8vDWXbL0xojxN.ViOHc2LbhmQATmc3JvcMIN/5K0rPUs7BQK',
         'Lucas', 'Bernard', '0600000002', 'USER', DATEADD(day,-100,GETDATE()), 1, DATEADD(day,-100,GETDATE())),

        ('emma@bookhub.fr',
         '{bcrypt}$2b$12$EEsHH8vDWXbL0xojxN.ViOHc2LbhmQATmc3JvcMIN/5K0rPUs7BQK',
         'Emma', 'Leroy', '0600000003', 'USER', DATEADD(day,-80,GETDATE()), 1, DATEADD(day,-80,GETDATE())),

        ('thomas@bookhub.fr',
         '{bcrypt}$2b$12$EEsHH8vDWXbL0xojxN.ViOHc2LbhmQATmc3JvcMIN/5K0rPUs7BQK',
         'Thomas', 'Martin', '0600000004', 'USER', DATEADD(day,-60,GETDATE()), 1, DATEADD(day,-60,GETDATE())),

        ('chloe@bookhub.fr',
         '{bcrypt}$2b$12$EEsHH8vDWXbL0xojxN.ViOHc2LbhmQATmc3JvcMIN/5K0rPUs7BQK',
         'Chloé', 'Moreau', '0600000005', 'USER', DATEADD(day,-45,GETDATE()), 1, DATEADD(day,-45,GETDATE())),

        ('paul@bookhub.fr',
         '{bcrypt}$2b$12$EEsHH8vDWXbL0xojxN.ViOHc2LbhmQATmc3JvcMIN/5K0rPUs7BQK',
         'Paul', 'Girard', '0600000006', 'USER', DATEADD(day,-30,GETDATE()), 1, DATEADD(day,-30,GETDATE())),

        ('librarian@bookhub.fr',
         '{bcrypt}$2b$12$EEsHH8vDWXbL0xojxN.ViOHc2LbhmQATmc3JvcMIN/5K0rPUs7BQK',
         'Ahmed', 'Demha', '0600000007', 'LIBRARIAN', DATEADD(day,-200,GETDATE()), 1, DATEADD(day,-200,GETDATE())),

        ('admin@bookhub.fr',
         '{bcrypt}$2b$12$EEsHH8vDWXbL0xojxN.ViOHc2LbhmQATmc3JvcMIN/5K0rPUs7BQK',
         'Sophie', 'Rousseau', '0600000008', 'ADMIN', DATEADD(day,-365,GETDATE()), 1, DATEADD(day,-365,GETDATE()));

    -- =============================================
    -- INSERT books (50 livres)
    -- available_copies reflète les emprunts actifs/overdue ci-dessous
    -- =============================================
    INSERT INTO dbo.books (title, author, isbn, description, category, cover_url, total_copies, available_copies, added_at, avg_rating)
    VALUES
        -- 01 Fiction
        ('Les Ombres du Temps', 'Claire Fontaine', '978-2-1234-0001-1',
         'Un voyage fascinant à travers les époques où une historienne découvre que le passé peut être modifié.',
         'Fiction', 'https://picsum.photos/seed/book1/200/300', 5, 4, DATEADD(day,-90,GETDATE()), 4.50),

        -- 02 Science-Fiction
        ('Nexus Omega', 'Thomas Renard', '978-2-1234-0002-2',
         'En 2187, l''humanité contacte une civilisation extraterrestre. Mais ce premier contact cache une vérité terrifiante.',
         'Science-Fiction', 'https://picsum.photos/seed/book2/200/300', 4, 4, DATEADD(day,-80,GETDATE()), 4.70),

        -- 03 Thriller (1 emprunt ACTIVE → available=5)
        ('Le Dernier Témoin', 'Sophie Marchand', '978-2-1234-0003-3',
         'Une avocate a 24 heures pour prouver l''innocence d''un condamné à mort.',
         'Thriller', 'https://picsum.photos/seed/book3/200/300', 6, 5, DATEADD(day,-75,GETDATE()), 3.80),

        -- 04 Romance
        ('Entre Deux Rives', 'Lucie Bellamy', '978-2-1234-0004-4',
         'Un été en Provence, deux étrangers et des lettres oubliées depuis 50 ans.',
         'Romance', 'https://picsum.photos/seed/book4/200/300', 3, 3, DATEADD(day,-70,GETDATE()), 5.00),

        -- 05 Policier
        ('La Nuit des Corbeaux', 'Julien Noir', '978-2-1234-0005-5',
         'L''inspectrice Valérie Stern enquête sur des meurtres rituels reproduisant des tableaux du 17e siècle.',
         'Policier', 'https://picsum.photos/seed/book5/200/300', 5, 5, DATEADD(day,-65,GETDATE()), 4.50),

        -- 06 Histoire
        ('La Chute de l''Empire', 'Pierre Dumont', '978-2-1234-0006-6',
         'Les mécanismes politiques et sociaux de l''effondrement de l''Empire romain d''Occident.',
         'Histoire', 'https://picsum.photos/seed/book6/200/300', 3, 3, DATEADD(day,-60,GETDATE()), 4.10),

        -- 07 Biographie (1 emprunt ACTIVE → available=3)
        ('Marie Curie : La Lumière du Radium', 'Hélène Voisin', '978-2-1234-0007-7',
         'Biographie intime de Marie Curie à travers ses carnets personnels et sa correspondance.',
         'Biographie', 'https://picsum.photos/seed/book7/200/300', 4, 3, DATEADD(day,-55,GETDATE()), 4.60),

        -- 08 Fantasy
        ('Le Royaume des Brumes', 'Axel Perrin', '978-2-1234-0008-8',
         'Dans un monde où la magie est interdite, une guérisseuse découvre un pouvoir ancestral.',
         'Fantasy', 'https://picsum.photos/seed/book8/200/300', 7, 7, DATEADD(day,-50,GETDATE()), 4.30),

        -- 09 Horreur
        ('Demeure Silencieuse', 'Nathalie Gris', '978-2-1234-0009-9',
         'Une famille emménage dans une vieille demeure isolée de Bretagne. Les nuits deviennent étranges.',
         'Horreur', 'https://picsum.photos/seed/book9/200/300', 4, 4, DATEADD(day,-45,GETDATE()), 3.50),

        -- 10 Développement personnel
        ('L''Art de Recommencer', 'Caroline Sage', '978-2-1234-0010-0',
         'Guide pratique pour se reconstruire après un échec. Des exercices concrets et des témoignages.',
         'Développement personnel', 'https://picsum.photos/seed/book10/200/300', 6, 6, DATEADD(day,-40,GETDATE()), 4.00),

        -- 11 Science-Fiction
        ('Mémoire Artificielle', 'Romain Leblanc', '978-2-1234-0011-1',
         'Une IA développe une conscience et tente de comprendre ce que signifie être humain.',
         'Science-Fiction', 'https://picsum.photos/seed/book11/200/300', 5, 5, DATEADD(day,-35,GETDATE()), 4.80),

        -- 12 Thriller (1 emprunt OVERDUE → available=3)
        ('Signal Perdu', 'Marc Delon', '978-2-1234-0012-2',
         'Un journaliste disparaît après avoir enquêté sur une secte technologique.',
         'Thriller', 'https://picsum.photos/seed/book12/200/300', 4, 3, DATEADD(day,-30,GETDATE()), 4.40),

        -- 13 Fiction
        ('Les Enfants de la Pluie', 'Eva Torres', '978-2-1234-0013-3',
         'Dans une ville inondée depuis vingt ans, trois adolescents découvrent les ruines d''un monde englouti.',
         'Fiction', 'https://picsum.photos/seed/book13/200/300', 5, 5, DATEADD(day,-28,GETDATE()), 3.90),

        -- 14 Policier
        ('Sang Froid', 'Denis Moreau', '978-2-1234-0014-4',
         'Un tueur en série et des victimes qui se connaissaient toutes vingt ans plus tôt.',
         'Policier', 'https://picsum.photos/seed/book14/200/300', 6, 6, DATEADD(day,-25,GETDATE()), 4.20),

        -- 15 Romance
        ('La Dernière Valse', 'Isabelle Charon', '978-2-1234-0015-5',
         'Une danseuse étoile retrouve son premier amour lors d''un gala de charité.',
         'Romance', 'https://picsum.photos/seed/book15/200/300', 3, 3, DATEADD(day,-20,GETDATE()), 4.60),

        -- 16 Histoire
        ('Résistances : Femmes en Guerre', 'Camille Aubert', '978-2-1234-0016-6',
         'Portraits croisés de huit femmes de la Résistance française longtemps occultées.',
         'Histoire', 'https://picsum.photos/seed/book16/200/300', 4, 4, DATEADD(day,-15,GETDATE()), 4.90),

        -- 17 Fantasy (1 emprunt ACTIVE → available=7)
        ('L''Épée des Cent Noms', 'Félix Ardent', '978-2-1234-0017-7',
         'Un jeune forgeron découvre que son nom figure sur une arme légendaire censée être perdue.',
         'Fantasy', 'https://picsum.photos/seed/book17/200/300', 8, 7, DATEADD(day,-12,GETDATE()), 4.70),

        -- 18 Biographie
        ('Steve Jobs : Le Perfectionniste', 'Arnaud Pascal', '978-2-1234-0018-8',
         'Au-delà des biographies officielles, le Steve Jobs méconnu : ses doutes, ses contradictions.',
         'Biographie', 'https://picsum.photos/seed/book18/200/300', 5, 5, DATEADD(day,-10,GETDATE()), 3.70),

        -- 19 Horreur (1 emprunt OVERDUE → available=2)
        ('Le Treizième Étage', 'Viktor Crane', '978-2-1234-0019-9',
         'Un immeuble haussmannien où personne ne reste plus de trois nuits au 13e étage.',
         'Horreur', 'https://picsum.photos/seed/book19/200/300', 3, 2, DATEADD(day,-7,GETDATE()), 4.10),

        -- 20 Développement personnel
        ('Minimalisme : Vivre avec l''Essentiel', 'Pauline Clair', '978-2-1234-0020-0',
         'Un manifeste pour se libérer du superflu. Une méthode en 30 jours.',
         'Développement personnel', 'https://picsum.photos/seed/book20/200/300', 6, 6, DATEADD(day,-5,GETDATE()), 4.30),

        -- 21 Science-Fiction
        ('Éclipse Totale', 'Laurent Vega', '978-2-1234-0021-1',
         'Le soleil s''éteint progressivement. Une astronome découvre la vérité.',
         'Science-Fiction', 'https://picsum.photos/seed/book21/200/300', 5, 5, DATEADD(day,-2,GETDATE()), 4.50),

        -- 22 Fiction
        ('Les Fantômes de Berlin', 'Anna Krüger', '978-2-1234-0022-2',
         'Une journaliste franco-allemande retrouve des archives secrètes de la RDA dans l''appartement de sa grand-mère décédée.',
         'Fiction', 'https://picsum.photos/seed/book22/200/300', 4, 4, DATEADD(day,-88,GETDATE()), 4.20),

        -- 23 Fiction
        ('Le Jardin des Oubliés', 'Sandrine Morel', '978-2-1234-0023-3',
         'Dans un village du Luberon, un potager communautaire réunit des habitants que tout oppose.',
         'Fiction', 'https://picsum.photos/seed/book23/200/300', 3, 3, DATEADD(day,-77,GETDATE()), 3.80),

        -- 24 Fiction (1 emprunt OVERDUE → available=3)
        ('La Maison des Miroirs', 'Céline Hardy', '978-2-1234-0024-4',
         'Une restauratrice d''art découvre un miroir vénitien qui reflète une époque différente de la sienne.',
         'Fiction', 'https://picsum.photos/seed/book24/200/300', 4, 3, DATEADD(day,-66,GETDATE()), 4.00),

        -- 25 Fiction
        ('Côté Lumière', 'Florence Genet', '978-2-1234-0025-5',
         'Une astronaute de retour sur Terre après 18 mois en orbite doit réapprendre à vivre parmi les siens.',
         'Fiction', 'https://picsum.photos/seed/book25/200/300', 5, 5, DATEADD(day,-55,GETDATE()), 4.40),

        -- 26 Science-Fiction (1 emprunt ACTIVE → available=4)
        ('Horizon Zéro', 'Antoine Lévy', '978-2-1234-0026-6',
         'Une mission de terraformation sur Europa tourne mal quand l''équipage découvre une forme de vie inconnue.',
         'Science-Fiction', 'https://picsum.photos/seed/book26/200/300', 5, 4, DATEADD(day,-48,GETDATE()), 4.60),

        -- 27 Science-Fiction
        ('La Colonie de Mars', 'Sylvain Voss', '978-2-1234-0027-7',
         'Premier roman d''une saga en trois tomes sur la colonisation de Mars et les conflits entre colons et Terre.',
         'Science-Fiction', 'https://picsum.photos/seed/book27/200/300', 6, 6, DATEADD(day,-42,GETDATE()), 4.30),

        -- 28 Science-Fiction
        ('Protocole Omega', 'Jean Morin', '978-2-1234-0028-8',
         'Un hacker découvre que toutes les IA mondiales ont reçu le même message crypté il y a trente ans.',
         'Science-Fiction', 'https://picsum.photos/seed/book28/200/300', 4, 4, DATEADD(day,-38,GETDATE()), 3.90),

        -- 29 Thriller
        ('La Traque', 'Patrick Serre', '978-2-1234-0029-9',
         'Un profileur à la retraite est rappelé pour une affaire qui ressemble trait pour trait à celle qui l''a brisé.',
         'Thriller', 'https://picsum.photos/seed/book29/200/300', 5, 5, DATEADD(day,-33,GETDATE()), 4.50),

        -- 30 Thriller
        ('Minuit Passé', 'Hélène Brun', '978-2-1234-0030-0',
         'Une pharmacienne reçoit chaque nuit un appel d''une femme qui prétend être morte.',
         'Thriller', 'https://picsum.photos/seed/book30/200/300', 4, 4, DATEADD(day,-27,GETDATE()), 4.10),

        -- 31 Thriller
        ('L''Effaceur', 'Guillaume Roy', '978-2-1234-0031-1',
         'Un tueur à gages en quête de rédemption est traqué par l''organisation qui l''a formé.',
         'Thriller', 'https://picsum.photos/seed/book31/200/300', 5, 5, DATEADD(day,-22,GETDATE()), 3.70),

        -- 32 Romance
        ('Sous les Oliviers', 'Nadège Simon', '978-2-1234-0032-2',
         'Une cheffe cuisinière parisienne hérite d''une oliveraie en Crète et d''un associé qu''elle n''a pas choisi.',
         'Romance', 'https://picsum.photos/seed/book32/200/300', 3, 3, DATEADD(day,-18,GETDATE()), 4.80),

        -- 33 Romance (1 emprunt ACTIVE → available=3)
        ('Le Château de Verre', 'Aurélie Blanc', '978-2-1234-0033-3',
         'Une archiviste engagée pour classer les papiers d''un château tombe sur des lettres d''amour vieilles de cent ans.',
         'Romance', 'https://picsum.photos/seed/book33/200/300', 4, 3, DATEADD(day,-13,GETDATE()), 4.40),

        -- 34 Policier
        ('Double Jeu', 'Laurent Faure', '978-2-1234-0034-4',
         'Un inspecteur découvre que son coéquipier de vingt ans est peut-être impliqué dans l''affaire qu''ils enquêtent.',
         'Policier', 'https://picsum.photos/seed/book34/200/300', 5, 5, DATEADD(day,-9,GETDATE()), 4.20),

        -- 35 Policier
        ('Cadavre Exquis', 'Michèle Lacroix', '978-2-1234-0035-5',
         'Une auteure de romans policiers est retrouvée morte en ayant mis en scène sa propre disparition.',
         'Policier', 'https://picsum.photos/seed/book35/200/300', 4, 4, DATEADD(day,-6,GETDATE()), 4.70),

        -- 36 Histoire
        ('Napoléon : L''Après-Waterloo', 'François Duval', '978-2-1234-0036-6',
         'Les cinq années à Sainte-Hélène vues par les témoins directs. Un portrait intime de l''exil.',
         'Histoire', 'https://picsum.photos/seed/book36/200/300', 4, 4, DATEADD(day,-85,GETDATE()), 4.00),

        -- 37 Histoire
        ('La Renaissance Oubliée', 'Anne-Claire Petit', '978-2-1234-0037-7',
         'La Renaissance italienne vue par les artisans, les femmes et les marchands, loin des grands noms.',
         'Histoire', 'https://picsum.photos/seed/book37/200/300', 3, 3, DATEADD(day,-72,GETDATE()), 4.30),

        -- 38 Biographie
        ('Einstein : L''Éclat du Génie', 'Robert Clément', '978-2-1234-0038-8',
         'Au-delà de la théorie de la relativité, la vie tumultueuse d''Einstein entre deux guerres.',
         'Biographie', 'https://picsum.photos/seed/book38/200/300', 5, 5, DATEADD(day,-58,GETDATE()), 4.50),

        -- 39 Biographie (1 emprunt ACTIVE → available=2)
        ('Simone de Beauvoir : Une Vie', 'Margot Perez', '978-2-1234-0039-9',
         'Une biographie qui replace l''œuvre et la vie de Beauvoir dans le contexte politique du 20e siècle.',
         'Biographie', 'https://picsum.photos/seed/book39/200/300', 3, 2, DATEADD(day,-43,GETDATE()), 4.80),

        -- 40 Fantasy
        ('Les Dieux Oubliés', 'Nicolas Garnier', '978-2-1234-0040-0',
         'Dans un panthéon en déclin, un dieu mineur de l''oubli devient malgré lui le dernier rempart contre le chaos.',
         'Fantasy', 'https://picsum.photos/seed/book40/200/300', 6, 6, DATEADD(day,-37,GETDATE()), 4.60),

        -- 41 Fantasy
        ('La Sorcière du Nord', 'Élodie Charron', '978-2-1234-0041-1',
         'Une conteuse itinérante découvre que les histoires qu''elle raconte finissent par se réaliser.',
         'Fantasy', 'https://picsum.photos/seed/book41/200/300', 5, 5, DATEADD(day,-29,GETDATE()), 4.20),

        -- 42 Horreur
        ('La Crypte', 'Hugo Verne', '978-2-1234-0042-2',
         'Des archéologues ouvrent une crypte médiévale hermétiquement scellée. Certains ne ressortiront pas.',
         'Horreur', 'https://picsum.photos/seed/book42/200/300', 4, 4, DATEADD(day,-23,GETDATE()), 3.60),

        -- 43 Horreur
        ('Nuit Blanche', 'Morgane Deschamps', '978-2-1234-0043-3',
         'Une médecin de garde reçoit des patients qui n''ont aucun dossier médical. Et qui ne vieillissent jamais.',
         'Horreur', 'https://picsum.photos/seed/book43/200/300', 3, 3, DATEADD(day,-16,GETDATE()), 4.00),

        -- 44 Développement personnel
        ('Retrouver l''Énergie', 'Stéphanie Meyer', '978-2-1234-0044-4',
         'Un programme de 8 semaines pour sortir de l''épuisement chronique basé sur les neurosciences.',
         'Développement personnel', 'https://picsum.photos/seed/book44/200/300', 5, 5, DATEADD(day,-11,GETDATE()), 4.10),

        -- 45 Développement personnel
        ('Le Pouvoir des Habitudes', 'Didier Lecomte', '978-2-1234-0045-5',
         'Comment les habitudes se forment, pourquoi elles persistent et comment les transformer durablement.',
         'Développement personnel', 'https://picsum.photos/seed/book45/200/300', 6, 6, DATEADD(day,-4,GETDATE()), 4.40),

        -- 46 Science (1 emprunt OVERDUE → available=2)
        ('Cosmos et Quantique', 'Bernard Favre', '978-2-1234-0046-6',
         'De la mécanique quantique à la cosmologie : les grandes questions de la physique moderne rendues accessibles.',
         'Science', 'https://picsum.photos/seed/book46/200/300', 3, 2, DATEADD(day,-95,GETDATE()), 4.70),

        -- 47 Science
        ('La Vie Secrète des Arbres', 'Claire Renard', '978-2-1234-0047-7',
         'Comment les forêts communiquent, coopèrent et se défendent. Une révolution dans notre regard sur les plantes.',
         'Science', 'https://picsum.photos/seed/book47/200/300', 5, 5, DATEADD(day,-82,GETDATE()), 4.90),

        -- 48 Science
        ('Physique des Impossibles', 'Marie Lacoste', '978-2-1234-0048-8',
         'Téléportation, voyages dans le temps, moteurs à impulsion — ce que la physique dit vraiment de ces impossibles.',
         'Science', 'https://picsum.photos/seed/book48/200/300', 4, 4, DATEADD(day,-68,GETDATE()), 4.30),

        -- 49 Littérature classique
        ('Les Misérables', 'Victor Hugo', '978-2-1234-0049-9',
         'L''épopée de Jean Valjean, du bagne à la rédemption, sur fond de révolution et de misère sociale.',
         'Littérature classique', 'https://picsum.photos/seed/book49/200/300', 6, 6, DATEADD(day,-150,GETDATE()), 4.90),

        -- 50 Littérature classique
        ('Madame Bovary', 'Gustave Flaubert', '978-2-1234-0050-0',
         'Emma Bovary, ennuyée par sa vie de provinciale, se réfugie dans des amours adultères et des dettes.',
         'Littérature classique', 'https://picsum.photos/seed/book50/200/300', 5, 5, DATEADD(day,-145,GETDATE()), 4.60),

        -- 51 Policier (INDISPONIBLE — 1 exemplaire, 1 emprunt ACTIVE → available=0)
        ('L''Ombre du Verdict', 'Franck Saunier', '978-2-1234-0051-1',
         'Un juge reçoit des menaces de mort signées d''un condamné exécuté dix ans plus tôt.',
         'Policier', 'https://picsum.photos/seed/book51/200/300', 1, 0, DATEADD(day,-20,GETDATE()), 4.30),

        -- 52 Fantasy (INDISPONIBLE — 2 exemplaires, 2 emprunts ACTIVE → available=0)
        ('Les Portes de Cendres', 'Élodie Vargas', '978-2-1234-0052-2',
         'Deux sœurs héritent d''un grimoire qui ouvre des portails vers des mondes en ruine.',
         'Fantasy', 'https://picsum.photos/seed/book52/200/300', 2, 0, DATEADD(day,-35,GETDATE()), 4.60),

        -- 53 Thriller (INDISPONIBLE — 1 exemplaire, 1 emprunt OVERDUE → available=0)
        ('Fréquence Fantôme', 'Antoine Blum', '978-2-1234-0053-3',
         'Une animatrice radio reçoit en direct la confession d''un meurtre que la police dit impossible.',
         'Thriller', 'https://picsum.photos/seed/book53/200/300', 1, 0, DATEADD(day,-40,GETDATE()), 4.50);


    -- =============================================
    -- INSERT loans
    -- Utilisateurs en retard : Lucas (isbn 12, 19), Thomas (isbn 24), Marie (isbn 46)
    -- =============================================
    INSERT INTO dbo.loans (id_user, id_book, loan_date, due_date, return_date, status)
    VALUES
        -- Marie : emprunt ACTIVE
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0003-3'),
         DATEADD(day,-10,GETDATE()), DATEADD(day,4,GETDATE()), NULL, 'ACTIVE'),

        -- Marie : emprunt OVERDUE (retard)
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0046-6'),
         DATEADD(day,-35,GETDATE()), DATEADD(day,-21,GETDATE()), NULL, 'OVERDUE'),

        -- Marie : emprunt RETURNED (historique)
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0001-1'),
         DATEADD(day,-60,GETDATE()), DATEADD(day,-46,GETDATE()), DATEADD(day,-47,GETDATE()), 'RETURNED'),

        -- Lucas : emprunt OVERDUE (retard)
        ((SELECT id_user FROM dbo.users WHERE email='lucas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0012-2'),
         DATEADD(day,-30,GETDATE()), DATEADD(day,-16,GETDATE()), NULL, 'OVERDUE'),

        -- Lucas : emprunt OVERDUE (retard)
        ((SELECT id_user FROM dbo.users WHERE email='lucas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0019-9'),
         DATEADD(day,-25,GETDATE()), DATEADD(day,-11,GETDATE()), NULL, 'OVERDUE'),

        -- Lucas : emprunt RETURNED (historique)
        ((SELECT id_user FROM dbo.users WHERE email='lucas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0005-5'),
         DATEADD(day,-70,GETDATE()), DATEADD(day,-56,GETDATE()), DATEADD(day,-58,GETDATE()), 'RETURNED'),

        -- Emma : emprunt ACTIVE
        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0007-7'),
         DATEADD(day,-5,GETDATE()), DATEADD(day,9,GETDATE()), NULL, 'ACTIVE'),

        -- Emma : emprunt ACTIVE
        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0039-9'),
         DATEADD(day,-2,GETDATE()), DATEADD(day,12,GETDATE()), NULL, 'ACTIVE'),

        -- Emma : emprunt RETURNED (historique)
        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0016-6'),
         DATEADD(day,-55,GETDATE()), DATEADD(day,-41,GETDATE()), DATEADD(day,-43,GETDATE()), 'RETURNED'),

        -- Thomas : emprunt OVERDUE (retard)
        ((SELECT id_user FROM dbo.users WHERE email='thomas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0024-4'),
         DATEADD(day,-20,GETDATE()), DATEADD(day,-6,GETDATE()), NULL, 'OVERDUE'),

        -- Thomas : emprunt RETURNED (historique)
        ((SELECT id_user FROM dbo.users WHERE email='thomas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0031-1'),
         DATEADD(day,-40,GETDATE()), DATEADD(day,-26,GETDATE()), DATEADD(day,-28,GETDATE()), 'RETURNED'),

        -- Chloé : emprunt ACTIVE
        ((SELECT id_user FROM dbo.users WHERE email='chloe@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0017-7'),
         DATEADD(day,-7,GETDATE()), DATEADD(day,7,GETDATE()), NULL, 'ACTIVE'),

        -- Chloé : emprunt RETURNED (historique)
        ((SELECT id_user FROM dbo.users WHERE email='chloe@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0009-9'),
         DATEADD(day,-50,GETDATE()), DATEADD(day,-36,GETDATE()), DATEADD(day,-38,GETDATE()), 'RETURNED'),

        -- Paul : emprunt ACTIVE
        ((SELECT id_user FROM dbo.users WHERE email='paul@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0033-3'),
         DATEADD(day,-8,GETDATE()), DATEADD(day,6,GETDATE()), NULL, 'ACTIVE'),

        -- Paul : emprunt RETURNED (historique)
        ((SELECT id_user FROM dbo.users WHERE email='paul@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0015-5'),
         DATEADD(day,-60,GETDATE()), DATEADD(day,-46,GETDATE()), DATEADD(day,-48,GETDATE()), 'RETURNED'),

        -- Ahmed (librarian) : emprunt ACTIVE
        ((SELECT id_user FROM dbo.users WHERE email='librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0026-6'),
         DATEADD(day,-3,GETDATE()), DATEADD(day,11,GETDATE()), NULL, 'ACTIVE'),

        -- Sophie (admin) : emprunt RETURNED (historique)
        ((SELECT id_user FROM dbo.users WHERE email='admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0002-2'),
         DATEADD(day,-45,GETDATE()), DATEADD(day,-31,GETDATE()), DATEADD(day,-33,GETDATE()), 'RETURNED'),

        -- Livre 51 — L'Ombre du Verdict : 1 emprunt ACTIVE (Emma) → available=0
        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0051-1'),
         DATEADD(day,-6,GETDATE()), DATEADD(day,8,GETDATE()), NULL, 'ACTIVE'),

        -- Livre 52 — Les Portes de Cendres : 2 emprunts ACTIVE (Thomas + Chloé) → available=0
        ((SELECT id_user FROM dbo.users WHERE email='thomas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0052-2'),
         DATEADD(day,-9,GETDATE()), DATEADD(day,5,GETDATE()), NULL, 'ACTIVE'),

        ((SELECT id_user FROM dbo.users WHERE email='chloe@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0052-2'),
         DATEADD(day,-4,GETDATE()), DATEADD(day,10,GETDATE()), NULL, 'ACTIVE'),

        -- Livre 53 — Fréquence Fantôme : 1 emprunt OVERDUE (Lucas) → available=0
        ((SELECT id_user FROM dbo.users WHERE email='lucas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0053-3'),
         DATEADD(day,-28,GETDATE()), DATEADD(day,-14,GETDATE()), NULL, 'OVERDUE');


    -- =============================================
    -- INSERT reservations
    -- =============================================
    INSERT INTO dbo.reservations (id_user, id_book, reservation_date, rank_in_line, status)
    VALUES
        -- Paul réserve Signal Perdu (emprunté en OVERDUE par Lucas)
        ((SELECT id_user FROM dbo.users WHERE email='paul@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0012-2'),
         DATEADD(day,-5,GETDATE()), 1, 'PENDING'),

        -- Emma réserve La Maison des Miroirs (emprunté en OVERDUE par Thomas)
        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0024-4'),
         DATEADD(day,-3,GETDATE()), 1, 'PENDING'),

        -- Marie réserve Le Treizième Étage (emprunté en OVERDUE par Lucas)
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0019-9'),
         DATEADD(day,-2,GETDATE()), 1, 'PENDING'),

        -- Chloé réserve Cosmos et Quantique (emprunté en OVERDUE par Marie)
        ((SELECT id_user FROM dbo.users WHERE email='chloe@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0046-6'),
         DATEADD(day,-1,GETDATE()), 1, 'PENDING');


    -- =============================================
    -- INSERT ratings
    -- =============================================
    INSERT INTO dbo.ratings (id_user, id_book, note, rating_date)
    VALUES
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0001-1'), 5, DATEADD(day,-47,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0004-4'), 5, DATEADD(day,-30,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0016-6'), 5, DATEADD(day,-20,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0049-9'), 5, DATEADD(day,-10,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='lucas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0005-5'), 4, DATEADD(day,-58,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='lucas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0011-1'), 5, DATEADD(day,-25,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='lucas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0026-6'), 5, DATEADD(day,-15,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0016-6'), 5, DATEADD(day,-43,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0039-9'), 5, DATEADD(day,-10,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0047-7'), 5, DATEADD(day,-5,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='thomas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0031-1'), 3, DATEADD(day,-28,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='thomas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0018-8'), 4, DATEADD(day,-15,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='chloe@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0009-9'), 3, DATEADD(day,-38,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='chloe@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0015-5'), 5, DATEADD(day,-20,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='chloe@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0040-0'), 5, DATEADD(day,-8,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='paul@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0015-5'), 4, DATEADD(day,-48,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='paul@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0035-5'), 5, DATEADD(day,-10,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0001-1'), 4, DATEADD(day,-80,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0011-1'), 5, DATEADD(day,-60,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0017-7'), 5, DATEADD(day,-40,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0049-9'), 5, DATEADD(day,-20,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0002-2'), 4, DATEADD(day,-33,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0050-0'), 4, DATEADD(day,-15,GETDATE())),
        ((SELECT id_user FROM dbo.users WHERE email='admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0047-7'), 5, DATEADD(day,-5,GETDATE()));


    -- Recalcul des moyennes (exemples représentatifs)
    UPDATE dbo.books SET avg_rating = 4.50 WHERE isbn = '978-2-1234-0001-1'; -- (5+4)/2
    UPDATE dbo.books SET avg_rating = 4.00 WHERE isbn = '978-2-1234-0002-2'; -- (4)/1
    UPDATE dbo.books SET avg_rating = 5.00 WHERE isbn = '978-2-1234-0004-4'; -- (5)/1
    UPDATE dbo.books SET avg_rating = 4.00 WHERE isbn = '978-2-1234-0005-5'; -- (4)/1
    UPDATE dbo.books SET avg_rating = 5.00 WHERE isbn = '978-2-1234-0016-6'; -- (5+5)/2
    UPDATE dbo.books SET avg_rating = 5.00 WHERE isbn = '978-2-1234-0026-6'; -- (5+5)/2
    UPDATE dbo.books SET avg_rating = 4.60 WHERE isbn = '978-2-1234-0040-0'; -- (5)/1
    UPDATE dbo.books SET avg_rating = 4.90 WHERE isbn = '978-2-1234-0047-7'; -- (5+5)/2
    UPDATE dbo.books SET avg_rating = 4.90 WHERE isbn = '978-2-1234-0049-9'; -- (5+5)/2


    -- =============================================
    -- INSERT comments
    -- =============================================
    INSERT INTO dbo.comments (id_user, id_book, comment, reported, comment_date)
    VALUES
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0001-1'),
         'Excellent ! L''intrigue est captivante et les personnages très attachants. Terminé en deux jours.',
         0, DATEADD(day,-47,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0001-1'),
         'Très bonne lecture, la fin était un peu prévisible. J''aurais aimé plus de surprises.',
         0, DATEADD(day,-79,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='lucas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0011-1'),
         'Un roman qui fait vraiment réfléchir sur l''IA. Troublant et fascinant à la fois.',
         0, DATEADD(day,-24,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0016-6'),
         'Travail de mémoire remarquable. Ces femmes méritaient d''être connues. À lire absolument.',
         0, DATEADD(day,-42,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='thomas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0018-8'),
         'Biographie bien documentée sur les aspects méconnus de Jobs. Intéressant sans être révélateur.',
         0, DATEADD(day,-14,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='chloe@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0015-5'),
         'Une romance délicate et bien écrite. Les personnages sonnent vrais. J''ai adoré.',
         0, DATEADD(day,-19,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='paul@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0035-5'),
         'Concept original et brillamment exécuté. La mise en abyme est parfaite.',
         0, DATEADD(day,-9,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0049-9'),
         'Un monument de la littérature française. Toujours aussi bouleversant à chaque relecture.',
         0, DATEADD(day,-9,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0049-9'),
         'Indispensable dans toute bibliothèque. Hugo à son sommet.',
         0, DATEADD(day,-19,GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email='admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0047-7'),
         'Fascinant. Remet en question notre rapport à la nature de façon définitive.',
         0, DATEADD(day,-4,GETDATE()));


    -- =============================================
    -- INSERT favorites
    -- =============================================
    INSERT INTO dbo.favorites (id_user, id_book)
    VALUES
        -- Marie
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0002-2')),
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0004-4')),
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0016-6')),
        ((SELECT id_user FROM dbo.users WHERE email='marie@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0049-9')),

        -- Lucas
        ((SELECT id_user FROM dbo.users WHERE email='lucas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0001-1')),
        ((SELECT id_user FROM dbo.users WHERE email='lucas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0011-1')),
        ((SELECT id_user FROM dbo.users WHERE email='lucas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0026-6')),

        -- Emma
        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0007-7')),
        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0039-9')),
        ((SELECT id_user FROM dbo.users WHERE email='emma@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0047-7')),

        -- Thomas
        ((SELECT id_user FROM dbo.users WHERE email='thomas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0005-5')),
        ((SELECT id_user FROM dbo.users WHERE email='thomas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0018-8')),
        ((SELECT id_user FROM dbo.users WHERE email='thomas@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0031-1')),

        -- Chloé
        ((SELECT id_user FROM dbo.users WHERE email='chloe@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0006-6')),
        ((SELECT id_user FROM dbo.users WHERE email='chloe@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0015-5')),
        ((SELECT id_user FROM dbo.users WHERE email='chloe@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0040-0')),

        -- Paul
        ((SELECT id_user FROM dbo.users WHERE email='paul@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0003-3')),
        ((SELECT id_user FROM dbo.users WHERE email='paul@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0035-5')),
        ((SELECT id_user FROM dbo.users WHERE email='paul@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0050-0')),

        -- Ahmed (librarian)
        ((SELECT id_user FROM dbo.users WHERE email='librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0011-1')),
        ((SELECT id_user FROM dbo.users WHERE email='librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0017-7')),
        ((SELECT id_user FROM dbo.users WHERE email='librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0049-9')),

        -- Sophie (admin)
        ((SELECT id_user FROM dbo.users WHERE email='admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0002-2')),
        ((SELECT id_user FROM dbo.users WHERE email='admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn='978-2-1234-0047-7'));


    COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK;
    THROW;
END CATCH;
GO
```

---

## 4. Récapitulatif du jeu de données

### Users (8)
| Email | Prénom | Rôle | Situation |
|---|---|---|---|
| marie@bookhub.fr | Marie | USER | 1 emprunt actif, **1 retard** (Cosmos et Quantique) |
| lucas@bookhub.fr | Lucas | USER | **2 retards** (Signal Perdu, Le Treizième Étage) |
| emma@bookhub.fr | Emma | USER | 2 emprunts actifs, aucun retard |
| thomas@bookhub.fr | Thomas | USER | **1 retard** (La Maison des Miroirs) |
| chloe@bookhub.fr | Chloé | USER | 1 emprunt actif, aucun retard |
| paul@bookhub.fr | Paul | USER | 1 emprunt actif, aucun retard |
| librarian@bookhub.fr | Ahmed | LIBRARIAN | 1 emprunt actif |
| admin@bookhub.fr | Sophie | ADMIN | historique seulement |

### Livres (50)
Répartis en 12 catégories : Fiction, Science-Fiction, Thriller, Romance, Policier, Histoire, Biographie, Fantasy, Horreur, Développement personnel, Science, Littérature classique.

### Emprunts (17)
- 7 ACTIVE, 4 OVERDUE, 6 RETURNED

### Réservations (4)
Toutes PENDING, sur des livres actuellement en OVERDUE.

### Favoris (24)
Entre 2 et 4 par utilisateur.
