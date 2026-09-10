PRAGMA foreign_keys = ON;

-- TABELA SERVIDOR
CREATE TABLE IF NOT EXISTS servidor (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- identificador único do servidor
    nome TEXT NOT NULL, -- nome completo do servidor
    matricula TEXT NOT UNIQUE, -- matrícula do servidor
    cpf TEXT NOT NULL UNIQUE, -- CPF do servidor
    ativo INTEGER NOT NULL DEFAULT 1   -- 0 = INATIVO, 1 = ATIVO
            CHECK (ativo IN (0, 1)),
    criado_em TEXT NOT NULL DEFAULT (strftime('%d/%m/%Y %H:%M:%S','now','localtime')) -- data e hora de criação do registro
);

-- TABELA LANÇAMENTO
-- REGISTRO DIÁRIO DE LANÇAMENTO DE VALE-TRANSPORTE, UM SERVIDOR SÓ PODE TER UM LANÇAMENTO POR DATA
CREATE TABLE IF NOT EXISTS lancamento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    servidor_id INTEGER NOT NULL, -- identificador único do servidor
    data TEXT NOT NULL, -- DD/MM/AAAA (data do lançamento)
    hora_descida TEXT, -- HH:MM (ida)
    hora_entrada TEXT, -- HH:MM (chegada no destino/ida)
    valor_ida NUMERIC(10,2), -- valor do vale-transporte de ida
    hora_saida TEXT, -- HH:MM (volta)
    hora_onibus TEXT, -- HH:MM (horário do onibus de volta)
    valor_volta NUMERIC(10,2), -- valor do vale-transporte de volta
    criado_em TEXT NOT NULL DEFAULT (strftime('%d/%m/%Y %H:%M:%S','now','localtime')), -- data e hora de criação do registro

    CONSTRAINT fk_lancamento_servidor
        FOREIGN KEY (servidor_id)
        REFERENCES servidor (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uq_lancamento_servidor_data
        UNIQUE (servidor_id, data)

    CONSTRAINT ck_lancamento_data_format
        CHECK (data GLOB '[0-3][0-9]/[0-1][0-9]/[0-9][0-9][0-9][0-9]') -- validação do formato da data (DD/MM/AAAA)
);

-- INDICE PARA ACESSO RÁPIDO AOS LANÇAMENTOS POR DATA
CREATE INDEX IF NOT EXISTS idx_lancamento_servidor_data 
    ON lancamento (servidor_id, data);