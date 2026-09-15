PRAGMA foreign_keys = ON;

-- TABELA SERVIDOR
CREATE TABLE IF NOT EXISTS servidor (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- identificador único do servidor
    nome TEXT NOT NULL, -- nome completo do servidor
    matricula TEXT NOT NULL UNIQUE, -- matrícula do servidor
    cpf TEXT NOT NULL UNIQUE, -- CPF do servidor
    ativo INTEGER NOT NULL DEFAULT 1   -- 0 = INATIVO, 1 = ATIVO
            CHECK (ativo IN (0, 1)),
    criado_em TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now','localtime')) -- data e hora de criação (ISO)
);

-- tabela: lancamento
-- registro diário de lançamento de vale-transporte; um servidor só pode ter um lançamento por data
CREATE TABLE IF NOT EXISTS lancamento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    servidor_id INTEGER NOT NULL, -- identificador único do servidor
    data TEXT NOT NULL, -- YYYY-MM-DD (ISO — data do lançamento)
    hora_descida TEXT NOT NULL, -- HH:mm (ida)
    hora_entrada TEXT NOT NULL, -- HH:mm (chegada no destino/ida)
    valor_ida NUMERIC(10,2) NOT NULL, -- valor do vale-transporte de ida
    hora_saida TEXT NOT NULL, -- HH:mm (volta)
    hora_onibus TEXT NOT NULL, -- HH:mm (horário do onibus de volta)
    valor_volta NUMERIC(10,2) NOT NULL, -- valor do vale-transporte de volta
    criado_em TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now','localtime')), -- data e hora de criação (ISO)

    CONSTRAINT fk_lancamento_servidor
        FOREIGN KEY (servidor_id)
        REFERENCES servidor (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uq_lancamento_servidor_data
        UNIQUE (servidor_id, data),

    CONSTRAINT ck_lancamento_data_format
        CHECK (data GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]')  -- valida o formato ISO (YYYY-MM-DD)
);

-- INDICE PARA ACESSO RÁPIDO AOS LANÇAMENTOS POR DATA
CREATE INDEX IF NOT EXISTS idx_lancamento_servidor_data 
    ON lancamento (servidor_id, data);