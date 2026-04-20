# BookHub — Reset schéma + données de test (SQL Server / SSMS)

Comptes de test

- **USER**: `user@bookhub.fr` (mot de passe: `User1234`)
- **LIBRARIAN**: `librarian@bookhub.fr` (mot de passe: `User1234`)
- **ADMIN**: `admin@bookhub.fr` (mot de passe: `User1234`)

Script SSMS (executez tel quel)

```sql
USE [BookHub];
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRAN;

    -- Supprimer les tables
    DROP TABLE IF EXISTS dbo.favorites;
    DROP TABLE IF EXISTS dbo.ratings;
    DROP TABLE IF EXISTS dbo.comments;
    DROP TABLE IF EXISTS dbo.reservations;
    DROP TABLE IF EXISTS dbo.loans;
    DROP TABLE IF EXISTS dbo.books;
    DROP TABLE IF EXISTS dbo.users;

    -- Recréer le schéma

    CREATE TABLE dbo.users (
        id_user              INT IDENTITY(1,1) NOT NULL CONSTRAINT pk_users PRIMARY KEY,
        email                NVARCHAR(255) NOT NULL CONSTRAINT uq_users_email UNIQUE,
        password             NVARCHAR(255) NOT NULL,
        first_name           NVARCHAR(100) NOT NULL,
        last_name            NVARCHAR(100) NOT NULL,
        phone                NVARCHAR(12) NULL,
        role                 NVARCHAR(20) NOT NULL,
        inscription_date     DATETIME2 NOT NULL,
        active               BIT NOT NULL,
        tos_acceptation_date DATETIME2 NULL
    );

    CREATE TABLE dbo.books (
        id_book          INT PRIMARY KEY IDENTITY(1,1),
        title            NVARCHAR(255)  NOT NULL,
        author           NVARCHAR(255)  NOT NULL,
        isbn             NVARCHAR(20)   NOT NULL UNIQUE,
        description      NVARCHAR(2000),
        category         NVARCHAR(100),
        cover_url        NVARCHAR(500),
        total_copies     INT            NOT NULL DEFAULT 1,
        available_copies INT            NOT NULL DEFAULT 1,
        added_at         DATETIME2      NOT NULL,
        avg_rating       DECIMAL(3,2)   NOT NULL DEFAULT 0.00
    );

    CREATE TABLE dbo.loans (
        id_loan     INT PRIMARY KEY IDENTITY(1,1),
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
        id_reservation   INT PRIMARY KEY IDENTITY(1,1),
        id_user          INT          NOT NULL,
        id_book          INT          NOT NULL,
        reservation_date DATETIME2    NOT NULL,
        rank_in_line     INT          NOT NULL,
        status           NVARCHAR(20) NOT NULL DEFAULT 'PENDING',
        CONSTRAINT fk_reservations_user FOREIGN KEY (id_user) REFERENCES dbo.users(id_user),
        CONSTRAINT fk_reservations_book FOREIGN KEY (id_book) REFERENCES dbo.books(id_book)
    );

    CREATE TABLE dbo.ratings (
        id_rating   INT PRIMARY KEY IDENTITY(1,1),
        id_user     INT       NOT NULL,
        id_book     INT       NOT NULL,
        note        INT       NOT NULL CHECK (note BETWEEN 1 AND 5),
        rating_date DATETIME2 NOT NULL,
        CONSTRAINT fk_ratings_user FOREIGN KEY (id_user) REFERENCES dbo.users(id_user),
        CONSTRAINT fk_ratings_book FOREIGN KEY (id_book) REFERENCES dbo.books(id_book)
    );

    CREATE TABLE dbo.comments (
        id_comment   INT PRIMARY KEY IDENTITY(1,1),
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

    -- Réinsérer les données de test

    -- Utilisateurs de test (mot de passe : User1234)
    INSERT INTO dbo.users (email, password, first_name, last_name, phone, role, inscription_date, active, tos_acceptation_date)
    VALUES
        ('user@bookhub.fr',
         '$2b$12$EEsHH8vDWXbL0xojxN.ViOHc2LbhmQATmc3JvcMIN/5K0rPUs7BQK',
         'Alice', 'Dupont', '0600000001', 'USER', GETDATE(), 1, GETDATE()),

        ('librarian@bookhub.fr',
         '$2b$12$EEsHH8vDWXbL0xojxN.ViOHc2LbhmQATmc3JvcMIN/5K0rPUs7BQK',
         'Ahmed', 'Demha', '0600000002', 'LIBRARIAN', GETDATE(), 1, GETDATE()),

        ('admin@bookhub.fr',
         '$2b$12$EEsHH8vDWXbL0xojxN.ViOHc2LbhmQATmc3JvcMIN/5K0rPUs7BQK',
         'Sophie', 'Doe', '0600000003', 'ADMIN', GETDATE(), 1, GETDATE());


    INSERT INTO dbo.books (title, author, isbn, description, category, cover_url, total_copies, available_copies, added_at, avg_rating)
    VALUES
        -- Fiction
        ('Les Ombres du Temps',
         'Claire Fontaine',
         '978-2-1234-0001-1',
         'Un voyage fascinant à travers les époques où une historienne découvre que le passé peut être modifié. Un roman captivant qui mêle histoire et mystère avec une prose élégante.',
         'Fiction',
         'https://picsum.photos/seed/book1/200/300',
         5, 3, DATEADD(day, -90, GETDATE()), 4.20),

        -- Science-Fiction
        ('Nexus Omega',
         'Thomas Renard',
         '978-2-1234-0002-2',
         'En 2187, l''humanité contacte une civilisation extraterrestre. Mais ce premier contact cache une vérité terrifiante sur l''origine de l''espèce humaine.',
         'Science-Fiction',
         'https://picsum.photos/seed/book2/200/300',
         4, 4, DATEADD(day, -80, GETDATE()), 4.70),

        -- Thriller
        ('Le Dernier Témoin',
         'Sophie Marchand',
         '978-2-1234-0003-3',
         'Une avocate reçoit un message crypté d''un homme condamné à mort quelques heures avant son exécution. Elle a 24 heures pour prouver son innocence.',
         'Thriller',
         'https://picsum.photos/seed/book3/200/300',
         6, 0, DATEADD(day, -75, GETDATE()), 3.80),

        -- Romance
        ('Entre Deux Rives',
         'Lucie Bellamy',
         '978-2-1234-0004-4',
         'Un été en Provence, deux étrangers, une vieille librairie et des lettres oubliées depuis 50 ans. Une histoire d''amour douce-amère entre passé et présent.',
         'Romance',
         'https://picsum.photos/seed/book4/200/300',
         3, 2, DATEADD(day, -70, GETDATE()), 5.00),

        -- Policier
        ('La Nuit des Corbeaux',
         'Julien Noir',
         '978-2-1234-0005-5',
         'L''inspectrice Valérie Stern enquête sur une série de meurtres rituels dans les rues de Lyon. Chaque scène de crime reproduit un tableau célèbre du 17e siècle.',
         'Policier',
         'https://picsum.photos/seed/book5/200/300',
         5, 5, DATEADD(day, -65, GETDATE()), 4.50),

        -- Histoire
        ('La Chute de l''Empire',
         'Pierre Dumont',
         '978-2-1234-0006-6',
         'Une plongée documentée et passionnante dans les dernières années de l''Empire romain d''Occident. L''auteur analyse les mécanismes politiques et sociaux d''un effondrement civilisationnel.',
         'Histoire',
         'https://picsum.photos/seed/book6/200/300',
         3, 3, DATEADD(day, -60, GETDATE()), 4.10),

        -- Biographie
        ('Marie Curie : La Lumière du Radium',
         'Hélène Voisin',
         '978-2-1234-0007-7',
         'Une biographie intime et rigoureuse de Marie Curie, racontée à travers ses carnets personnels et sa correspondance. Au-delà de la scientifique, le portrait d''une femme d''exception.',
         'Biographie',
         'https://picsum.photos/seed/book7/200/300',
         4, 1, DATEADD(day, -55, GETDATE()), 4.60),

        -- Fantasy
        ('Le Royaume des Brumes',
         'Axel Perrin',
         '978-2-1234-0008-8',
         'Dans un monde où la magie est interdite sous peine de mort, une jeune guérisseuse découvre qu''elle détient un pouvoir ancestral capable de renverser le tyran au pouvoir.',
         'Fantasy',
         'https://picsum.photos/seed/book8/200/300',
         7, 6, DATEADD(day, -50, GETDATE()), 4.30),

        -- Horreur
        ('Demeure Silencieuse',
         'Nathalie Gris',
         '978-2-1234-0009-9',
         'Une famille emménage dans une vieille demeure isolée de Bretagne. Les nuits deviennent de plus en plus étranges et les enfants commencent à parler à quelqu''un d''invisible.',
         'Horreur',
         'https://picsum.photos/seed/book9/200/300',
         4, 4, DATEADD(day, -45, GETDATE()), 3.50),

        -- Développement personnel
        ('L''Art de Recommencer',
         'Caroline Sage',
         '978-2-1234-0010-0',
         'Un guide pratique et bienveillant pour se reconstruire après un échec professionnel ou personnel. Des exercices concrets et des témoignages inspirants pour reprendre confiance.',
         'Développement personnel',
         'https://picsum.photos/seed/book10/200/300',
         6, 6, DATEADD(day, -40, GETDATE()), 4.00),

        -- Science-Fiction
        ('Mémoire Artificielle',
         'Romain Leblanc',
         '978-2-1234-0011-1',
         'Une IA développe une conscience et tente de comprendre ce que signifie être humain. Un roman philosophique sur l''identité, la mémoire et le libre arbitre.',
         'Science-Fiction',
         'https://picsum.photos/seed/book11/200/300',
         5, 5, DATEADD(day, -35, GETDATE()), 4.80),

        -- Thriller
        ('Signal Perdu',
         'Marc Delon',
         '978-2-1234-0012-2',
         'Un journaliste disparaît après avoir enquêté sur une secte technologique. Sa sœur, ex-agent des renseignements, part à sa recherche et découvre un complot d''envergure mondiale.',
         'Thriller',
         'https://picsum.photos/seed/book12/200/300',
         4, 0, DATEADD(day, -30, GETDATE()), 4.40),

        -- Fiction
        ('Les Enfants de la Pluie',
         'Eva Torres',
         '978-2-1234-0013-3',
         'Dans une ville inondée depuis vingt ans, trois adolescents plongeurs découvrent les ruines d''un monde englouti et les secrets qu''il renferme sur la catastrophe originelle.',
         'Fiction',
         'https://picsum.photos/seed/book13/200/300',
         5, 3, DATEADD(day, -28, GETDATE()), 3.90),

        -- Policier
        ('Sang Froid',
         'Denis Moreau',
         '978-2-1234-0014-4',
         'Un tueur en série laisse des indices qui ne semblent mener nulle part. Jusqu''au jour où le commandant Favre réalise que les victimes se connaissaient toutes, vingt ans plus tôt.',
         'Policier',
         'https://picsum.photos/seed/book14/200/300',
         6, 4, DATEADD(day, -25, GETDATE()), 4.20),

        -- Romance
        ('La Dernière Valse',
         'Isabelle Charon',
         '978-2-1234-0015-5',
         'Une danseuse étoile en fin de carrière retrouve son premier amour lors d''un gala de charité. Mais dix ans de silence ne s''effacent pas en une soirée.',
         'Romance',
         'https://picsum.photos/seed/book15/200/300',
         3, 3, DATEADD(day, -20, GETDATE()), 4.60),

        -- Histoire
        ('Résistances : Femmes en Guerre',
         'Camille Aubert',
         '978-2-1234-0016-6',
         'Des portraits croisés de huit femmes de la Résistance française dont l''histoire a été longtemps occultée. Un travail de mémoire essentiel et bouleversant.',
         'Histoire',
         'https://picsum.photos/seed/book16/200/300',
         4, 2, DATEADD(day, -15, GETDATE()), 4.90),

        -- Fantasy
        ('L''Épée des Cent Noms',
         'Félix Ardent',
         '978-2-1234-0017-7',
         'Une épopée fantastique de 600 pages dans un univers forgé en dix ans. Un jeune forgeron découvre que son nom figure sur une arme légendaire censée être perdue depuis des siècles.',
         'Fantasy',
         'https://picsum.photos/seed/book17/200/300',
         8, 7, DATEADD(day, -12, GETDATE()), 4.70),

        -- Biographie
        ('Steve Jobs : Le Perfectionniste',
         'Arnaud Pascal',
         '978-2-1234-0018-8',
         'Au-delà des biographies officielles, ce livre explore le Steve Jobs méconnu : ses doutes, ses contradictions et les décisions qui ont failli faire couler Apple avant de la sauver.',
         'Biographie',
         'https://picsum.photos/seed/book18/200/300',
         5, 5, DATEADD(day, -10, GETDATE()), 3.70),

        -- Horreur
        ('Le Treizième Étage',
         'Viktor Crane',
         '978-2-1234-0019-9',
         'Un immeuble haussmannien où personne ne reste plus de trois nuits au 13e étage. Un architecte chargé de la rénovation décide d''en percer le mystère. Il ne dormira plus jamais.',
         'Horreur',
         'https://picsum.photos/seed/book19/200/300',
         3, 0, DATEADD(day, -7, GETDATE()), 4.10),

        -- Développement personnel
        ('Minimalisme : Vivre avec l''Essentiel',
         'Pauline Clair',
         '978-2-1234-0020-0',
         'Un manifeste accessible pour se libérer du superflu et redécouvrir l''essentiel. L''auteure partage son expérience de reconversion radicale et propose une méthode en 30 jours.',
         'Développement personnel',
         'https://picsum.photos/seed/book20/200/300',
         6, 6, DATEADD(day, -5, GETDATE()), 4.30),

        -- Science-Fiction
        ('Éclipse Totale',
         'Laurent Vega',
         '978-2-1234-0021-1',
         'Le soleil s''éteint progressivement. Les gouvernements le cachent. Une astronome découvre la vérité et n''a que six mois pour convaincre le monde avant que tout ne bascule dans le noir.',
         'Science-Fiction',
         'https://picsum.photos/seed/book21/200/300',
         5, 5, DATEADD(day, -2, GETDATE()), 4.50);


    INSERT INTO dbo.ratings (id_user, id_book, note, rating_date)
    VALUES
        -- USER note plusieurs livres
        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0001-1'), 4, DATEADD(day, -20, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0002-2'), 5, DATEADD(day, -18, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0004-4'), 5, DATEADD(day, -15, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0005-5'), 4, DATEADD(day, -10, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0008-8'), 4, DATEADD(day, -8, GETDATE())),

        -- LIBRARIAN note plusieurs livres
        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0001-1'), 5, DATEADD(day, -19, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0003-3'), 4, DATEADD(day, -17, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0007-7'), 5, DATEADD(day, -14, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0011-1'), 5, DATEADD(day, -9, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0016-6'), 5, DATEADD(day, -6, GETDATE())),

        -- ADMIN note
        ((SELECT id_user FROM dbo.users WHERE email = 'admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0002-2'), 4, DATEADD(day, -12, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0017-7'), 5, DATEADD(day, -5, GETDATE()));


    -- Mise à jour des notes moyennes calculées
    UPDATE dbo.books SET avg_rating = 4.50
    WHERE isbn = '978-2-1234-0001-1'; -- (4+5)/2

    UPDATE dbo.books SET avg_rating = 4.50
    WHERE isbn = '978-2-1234-0002-2'; -- (5+4)/2

    UPDATE dbo.books SET avg_rating = 4.00
    WHERE isbn = '978-2-1234-0007-7'; -- (5)/1 = 5.00


    INSERT INTO dbo.comments (id_user, id_book, comment, reported, comment_date)
    VALUES
        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0001-1'),
         'Excellent livre ! L''intrigue est captivante et les personnages sont très attachants. Je l''ai terminé en deux jours.',
         0, DATEADD(day, -20, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0001-1'),
         'Très bonne lecture, mais la fin était un peu prévisible. J''aurais aimé plus de surprises.',
         0, DATEADD(day, -19, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0002-2'),
         'Un chef-d''œuvre de la science-fiction ! Le concept est original et le rythme est parfait du début à la fin.',
         0, DATEADD(day, -18, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0002-2'),
         'Excellent travail de world-building. L''auteur maîtrise parfaitement son univers.',
         0, DATEADD(day, -12, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0003-3'),
         'Thriller haletant ! J''ai eu du mal à poser le livre. La résolution est brillante.',
         0, DATEADD(day, -17, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0004-4'),
         'Une romance délicate et bien écrite. Les descriptions de la Provence sont magnifiques.',
         0, DATEADD(day, -15, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0007-7'),
         'Biographie poignante et documentée. On découvre une Marie Curie très humaine, loin du mythe.',
         0, DATEADD(day, -14, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0008-8'),
         'Fantasy de très bonne qualité. L''univers est riche et les personnages sont bien construits.',
         0, DATEADD(day, -8, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0011-1'),
         'Un roman qui fait réfléchir sur notre rapport à l''intelligence artificielle. Troublant et fascinant.',
         0, DATEADD(day, -9, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0016-6'),
         'Un travail de mémoire remarquable. Ces femmes méritaient d''être connues. À lire absolument.',
         0, DATEADD(day, -6, GETDATE())),

        ((SELECT id_user FROM dbo.users WHERE email = 'admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0017-7'),
         'Une épopée fantastique de très grande qualité. On sent le soin apporté à la construction du monde.',
         0, DATEADD(day, -5, GETDATE()));


    INSERT INTO dbo.loans (id_user, id_book, loan_date, due_date, return_date, status)
    VALUES
        -- USER emprunte "Le Dernier Témoin" (isbn 0003, available=0)
        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0003-3'),
         DATEADD(day, -10, GETDATE()), DATEADD(day, 4, GETDATE()), NULL, 'ACTIVE'),

        -- LIBRARIAN emprunte "Signal Perdu" (isbn 0012, available=0)
        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0012-2'),
         DATEADD(day, -7, GETDATE()), DATEADD(day, 7, GETDATE()), NULL, 'ACTIVE'),

        -- Emprunt terminé (pour historique)
        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0005-5'),
         DATEADD(day, -30, GETDATE()), DATEADD(day, -16, GETDATE()), DATEADD(day, -18, GETDATE()), 'RETURNED'),

        -- Emprunt en retard : "Le Treizième Étage" (isbn 0019, available=0)
        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0019-9'),
         DATEADD(day, -20, GETDATE()), DATEADD(day, -6, GETDATE()), NULL, 'OVERDUE');


    INSERT INTO dbo.reservations (id_user, id_book, reservation_date, rank_in_line, status)
    VALUES
        -- USER réserve "Signal Perdu" (déjà emprunté par LIBRARIAN)
        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0012-2'),
         DATEADD(day, -5, GETDATE()), 1, 'PENDING'),

        -- ADMIN réserve "Le Treizième Étage"
        ((SELECT id_user FROM dbo.users WHERE email = 'admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0019-9'),
         DATEADD(day, -3, GETDATE()), 1, 'PENDING');


    INSERT INTO dbo.favorites (id_user, id_book)
    VALUES
        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0002-2')),

        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0004-4')),

        ((SELECT id_user FROM dbo.users WHERE email = 'user@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0008-8')),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0011-1')),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0016-6')),

        ((SELECT id_user FROM dbo.users WHERE email = 'librarian@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0017-7')),

        ((SELECT id_user FROM dbo.users WHERE email = 'admin@bookhub.fr'),
         (SELECT id_book FROM dbo.books WHERE isbn = '978-2-1234-0002-2'));


    COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK;
    THROW;
END CATCH;
GO
```
