-- ============================================================
--  AGENDA PERSONNEL -- Script Oracle SQL complet
--  Tables : UTILISATEUR, CATEGORIE, EVENEMENT, TACHE
--  Auteur  : TP Noté POO AV 2026
-- ============================================================

-- ------------------------------------------------------------
--  0. Nettoyage (à exécuter si re-création complète)
-- ------------------------------------------------------------
BEGIN
    FOR t IN (SELECT table_name FROM user_tables
              WHERE table_name IN ('TACHE','EVENEMENT','CATEGORIE','UTILISATEUR'))
    LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
    END LOOP;

    FOR s IN (SELECT sequence_name FROM user_sequences
              WHERE sequence_name IN ('SEQ_UTIL','SEQ_CAT','SEQ_EVEN','SEQ_TACHE'))
    LOOP
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
    END LOOP;
END;
/

-- ============================================================
--  1. SEQUENCES
-- ============================================================

CREATE SEQUENCE SEQ_UTIL
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE SEQ_CAT
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE SEQ_EVEN
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE SEQUENCE SEQ_TACHE
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- ============================================================
--  2. TABLE UTILISATEUR
-- ============================================================

CREATE TABLE UTILISATEUR (
    ID_UTIL       NUMBER          CONSTRAINT PK_UTIL PRIMARY KEY,
    NOM           VARCHAR2(50)    NOT NULL,
    PRENOM        VARCHAR2(50)    NOT NULL,
    EMAIL         VARCHAR2(100)   NOT NULL,
    MOT_DE_PASSE  VARCHAR2(255)   NOT NULL,   -- stocker un hash (SHA-256 etc.)
    DATE_CREATION DATE            DEFAULT SYSDATE,
    -- Contrainte : email unique par utilisateur
    CONSTRAINT UQ_UTIL_EMAIL UNIQUE (EMAIL),
    -- Contrainte : email doit contenir un @
    CONSTRAINT CHK_UTIL_EMAIL CHECK (EMAIL LIKE '%@%')
);

-- Trigger auto-incrément via séquence
CREATE OR REPLACE TRIGGER TRG_UTIL_ID
BEFORE INSERT ON UTILISATEUR
FOR EACH ROW
BEGIN
    IF :NEW.ID_UTIL IS NULL THEN
        :NEW.ID_UTIL := SEQ_UTIL.NEXTVAL;
    END IF;
END;
/

-- ============================================================
--  3. TABLE CATEGORIE
-- ============================================================

CREATE TABLE CATEGORIE (
    ID_CAT   NUMBER         CONSTRAINT PK_CAT PRIMARY KEY,
    LIBELLE  VARCHAR2(50)   NOT NULL,
    COULEUR  VARCHAR2(20)   DEFAULT '#3B82F6',  -- code hexadécimal CSS
    ICONE    VARCHAR2(50),                       -- nom d'icône (ex. 'calendar', 'briefcase')
    -- Contrainte : libellé unique
    CONSTRAINT UQ_CAT_LIBELLE UNIQUE (LIBELLE)
);

CREATE OR REPLACE TRIGGER TRG_CAT_ID
BEFORE INSERT ON CATEGORIE
FOR EACH ROW
BEGIN
    IF :NEW.ID_CAT IS NULL THEN
        :NEW.ID_CAT := SEQ_CAT.NEXTVAL;
    END IF;
END;
/

-- ============================================================
--  4. TABLE EVENEMENT
-- ============================================================

CREATE TABLE EVENEMENT (
    ID_EVEN      NUMBER          CONSTRAINT PK_EVEN PRIMARY KEY,
    TITRE        VARCHAR2(100)   NOT NULL,
    DATE_EVEN    DATE            NOT NULL,
    HEURE        VARCHAR2(5),                    -- format 'HH:MM'
    DESCRIPTION  CLOB,
    IMPORTANT    NUMBER(1)       DEFAULT 0,      -- 0 = normal, 1 = important
    DATE_CREATION DATE           DEFAULT SYSDATE,
    -- Clés étrangères
    ID_CAT       NUMBER,
    ID_UTIL      NUMBER          NOT NULL,
    -- Contrainte : flag IMPORTANT ne peut valoir que 0 ou 1
    CONSTRAINT CHK_EVEN_IMPORTANT CHECK (IMPORTANT IN (0, 1)),
    -- Contrainte : format heure HH:MM (optionnel mais recommandé)
    CONSTRAINT CHK_EVEN_HEURE CHECK (
        HEURE IS NULL OR REGEXP_LIKE(HEURE, '^([01][0-9]|2[0-3]):[0-5][0-9]$')
    ),
    -- FK vers CATEGORIE
    CONSTRAINT FK_EVEN_CAT
        FOREIGN KEY (ID_CAT)
        REFERENCES CATEGORIE(ID_CAT)
        ON DELETE SET NULL,
    -- FK vers UTILISATEUR
    CONSTRAINT FK_EVEN_UTIL
        FOREIGN KEY (ID_UTIL)
        REFERENCES UTILISATEUR(ID_UTIL)
        ON DELETE CASCADE
);

CREATE OR REPLACE TRIGGER TRG_EVEN_ID
BEFORE INSERT ON EVENEMENT
FOR EACH ROW
BEGIN
    IF :NEW.ID_EVEN IS NULL THEN
        :NEW.ID_EVEN := SEQ_EVEN.NEXTVAL;
    END IF;
END;
/

-- Index pour accélérer les recherches fréquentes
CREATE INDEX IDX_EVEN_DATE   ON EVENEMENT(DATE_EVEN);
CREATE INDEX IDX_EVEN_UTIL   ON EVENEMENT(ID_UTIL);
CREATE INDEX IDX_EVEN_CAT    ON EVENEMENT(ID_CAT);
CREATE INDEX IDX_EVEN_TITRE  ON EVENEMENT(TITRE);

-- ============================================================
--  5. TABLE TACHE
-- ============================================================

CREATE TABLE TACHE (
    ID_TACHE     NUMBER          CONSTRAINT PK_TACHE PRIMARY KEY,
    TITRE        VARCHAR2(100)   NOT NULL,
    DESCRIPTION  VARCHAR2(500),
    DEADLINE     DATE,
    PRIORITE     VARCHAR2(10)    DEFAULT 'MOYENNE',
    STATUT       VARCHAR2(20)    DEFAULT 'A_FAIRE',
    DATE_CREATION DATE           DEFAULT SYSDATE,
    -- Clés étrangères
    ID_EVEN      NUMBER,                         -- tâche liée à un événement (optionnel)
    ID_UTIL      NUMBER          NOT NULL,
    -- Contrainte : valeurs autorisées pour PRIORITE
    CONSTRAINT CHK_TACHE_PRIORITE CHECK (
        PRIORITE IN ('HAUTE', 'MOYENNE', 'BASSE')
    ),
    -- Contrainte : valeurs autorisées pour STATUT (style Kanban)
    CONSTRAINT CHK_TACHE_STATUT CHECK (
        STATUT IN ('A_FAIRE', 'EN_COURS', 'TERMINE')
    ),
    -- FK vers EVENEMENT
    CONSTRAINT FK_TACHE_EVEN
        FOREIGN KEY (ID_EVEN)
        REFERENCES EVENEMENT(ID_EVEN)
        ON DELETE SET NULL,
    -- FK vers UTILISATEUR
    CONSTRAINT FK_TACHE_UTIL
        FOREIGN KEY (ID_UTIL)
        REFERENCES UTILISATEUR(ID_UTIL)
        ON DELETE CASCADE
);

CREATE OR REPLACE TRIGGER TRG_TACHE_ID
BEFORE INSERT ON TACHE
FOR EACH ROW
BEGIN
    IF :NEW.ID_TACHE IS NULL THEN
        :NEW.ID_TACHE := SEQ_TACHE.NEXTVAL;
    END IF;
END;
/

-- Index pour filtrage rapide par statut et utilisateur
CREATE INDEX IDX_TACHE_STATUT   ON TACHE(STATUT);
CREATE INDEX IDX_TACHE_UTIL     ON TACHE(ID_UTIL);
CREATE INDEX IDX_TACHE_DEADLINE ON TACHE(DEADLINE);

-- ============================================================
--  6. DONNEES D'INITIALISATION (catégories par défaut)
-- ============================================================

INSERT INTO CATEGORIE (LIBELLE, COULEUR, ICONE) VALUES ('Réunion',       '#3B82F6', 'briefcase');
INSERT INTO CATEGORIE (LIBELLE, COULEUR, ICONE) VALUES ('RDV Médical',   '#EF4444', 'heart');
INSERT INTO CATEGORIE (LIBELLE, COULEUR, ICONE) VALUES ('Personnel',     '#10B981', 'user');
INSERT INTO CATEGORIE (LIBELLE, COULEUR, ICONE) VALUES ('Formation',     '#F59E0B', 'book');
INSERT INTO CATEGORIE (LIBELLE, COULEUR, ICONE) VALUES ('Autre',         '#6B7280', 'tag');

COMMIT;

-- ============================================================
--  7. REQUETES UTILES (exemples pour la couche DAO)
-- ============================================================

-- Recherche multi-critères (titre + catégorie + plage de dates)
-- SELECT e.*, c.LIBELLE AS CAT_LIBELLE, c.COULEUR
-- FROM   EVENEMENT e
-- LEFT JOIN CATEGORIE c ON e.ID_CAT = c.ID_CAT
-- WHERE  e.ID_UTIL = :id_util
--   AND  UPPER(e.TITRE) LIKE UPPER('%' || :titre || '%')
--   AND  e.ID_CAT    = NVL(:id_cat, e.ID_CAT)
--   AND  e.DATE_EVEN BETWEEN NVL(:date_debut, DATE '0001-01-01')
--                        AND NVL(:date_fin,   DATE '9999-12-31')
-- ORDER BY e.DATE_EVEN, e.HEURE;

-- Événements importants à venir (7 prochains jours)
-- SELECT * FROM EVENEMENT
-- WHERE  ID_UTIL  = :id_util
--   AND  IMPORTANT = 1
--   AND  DATE_EVEN BETWEEN SYSDATE AND SYSDATE + 7
-- ORDER BY DATE_EVEN;

-- Tâches par statut (vue Kanban)
-- SELECT * FROM TACHE
-- WHERE  ID_UTIL = :id_util
--   AND  STATUT  = :statut        -- 'A_FAIRE' | 'EN_COURS' | 'TERMINE'
-- ORDER BY
--   CASE PRIORITE WHEN 'HAUTE' THEN 1 WHEN 'MOYENNE' THEN 2 ELSE 3 END,
--   DEADLINE NULLS LAST;
