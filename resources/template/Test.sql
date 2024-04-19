CREATE TABLE scaffholding;
\c scaffholding;

CREATE TABLE Test(
	id serial primary key not null,
	nom varchar(30),
	ddn date,
	dernierConnexion timestamp,
	poids double precision,
	annee numeric,
	dec decimal
);

CREATE TABLE Account(
	id varchar(7) primary key not null,
	username varchar(30),
	email varchar(50),
	password varchar(50)
);
CREATE SEQUENCE account_sequence START 1;


CREATE TABLE Promotion(
	id varchar(7) primary key not null,
	prom varchar(30)
);
CREATE SEQUENCE promotion_sequence START 1;

CREATE TABLE Groupe(
	id varchar(7) primary key not null,
	nom varchar(30)
);
CREATE SEQUENCE groupe_sequence START 1;

CREATE TABLE Etudiant(
	id varchar(7) primary key not null,
	nom varchar(30),
	idpromotion varchar(7),
	idGroupe varchar(7)
);
CREATE SEQUENCE etudiant_sequence START 1;

ALTER TABLE Etudiant ADD FOREIGN KEY (idpromotion) REFERENCES Promotion(id) ON DELETE SET NULL;
ALTER TABLE Etudiant ADD FOREIGN KEY (idgroupe) REFERENCES Groupe(id) ON DELETE SET NULL;

INSERT INTO Promotion(id,prom) VALUES('PRM0001', 'P1');
INSERT INTO Promotion(id,prom) VALUES('PRM0002', 'P2');

INSERT INTO Groupe(id,nom) VALUES('GRP0001', 'A');
INSERT INTO Groupe(id,nom) VALUES('GRP0002', 'B');

INSERT INTO Etudiant(id,nom,idpromotion) VALUES('ETU0001','Jean','PRM0001');
INSERT INTO Etudiant(id,nom,idpromotion) VALUES('ETU0002','Rasoa','PRM0002');

DELETE FROM Promotion ON DELETE SET NULL WHERE id = 'PRM0001';