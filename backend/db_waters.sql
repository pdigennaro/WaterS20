CREATE TABLE Dispositivi (
  ID_DISPOSITIVO CHAR(6),
  Data_Ultima_Attivita date NOT NULL,
  Zona_Operativita varchar(50) NOT NULL,
  PRIMARY KEY (ID_DISPOSITIVO)
) ENGINE=InnoDB;

CREATE TABLE Utenti (
  ID_UTENTE INTEGER AUTO_INCREMENT,
  Username varchar(150) NOT NULL,
  Password varchar(64) NOT NULL,
  Nome char(25) NOT NULL,
  Cognome char(25) NOT NULL,
  Email varchar(35) NOT NULL,
  Telefono varchar(15) NOT NULL,
  Struttura_appartenenza varchar(50) NOT NULL,
  PRIMARY KEY (ID_UTENTE)
) ENGINE=InnoDB;

CREATE TABLE GPS (
  ID_COORDINATE INTEGER AUTO_INCREMENT,
  ID_DISPOSITIVO_GPS CHAR(6),
  Latitudine real NOT NULL,
  Longitudine real NOT NULL,
  Timestamp bigint NOT NULL,
  PRIMARY KEY (ID_COORDINATE),
  FOREIGN KEY (ID_DISPOSITIVO_GPS) REFERENCES Dispositivi(ID_DISPOSITIVO)
) ENGINE=InnoDB;

CREATE TABLE Rilevamenti (
  ID_RILEVAMENTO INTEGER AUTO_INCREMENT,
  ID_DISPOSITIVO CHAR(6),
  Nome_parametro varchar(15) NOT NULL,
  Valore_parametro real NOT NULL,
  Timestamp bigint NOT NULL,
  PRIMARY KEY (ID_RILEVAMENTO),
  FOREIGN KEY (ID_DISPOSITIVO) REFERENCES Dispositivi(ID_DISPOSITIVO)
) ENGINE=InnoDB;
