PRAGMA foreign_keys = ON;

-- tabela: servidor
CREATE TABLE IF NOT EXISTS servidor (
    id INTEGER PRIMARY KEY AUTOINCREMENT, -- identificador único do servidor
    nome TEXT NOT NULL, -- nome completo do servidor
    matricula TEXT NOT NULL UNIQUE, -- matrícula do servidor
    cpf TEXT NOT NULL UNIQUE, -- CPF do servidor (único)
    ativo INTEGER NOT NULL DEFAULT 1 -- 0 = inativo, 1 = ativo
            CHECK (ativo IN (0, 1)),
    criado_em TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now','localtime')) -- data e hora de criação (ISO)
);

-- tabela: lancamento
-- registro diário de lançamento de vale-transporte; um servidor só pode ter um lançamento por data.
-- as informações de horários e valores ficam na tabela `trecho` (N trechos por lançamento).
CREATE TABLE IF NOT EXISTS lancamento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    servidor_id INTEGER NOT NULL, -- id do servidor (FK)
    data TEXT NOT NULL, -- YYYY-MM-DD (ISO — data do lançamento)
    criado_em TEXT NOT NULL DEFAULT (strftime('%Y-%m-%d %H:%M:%S','now','localtime')), -- data e hora de criação (ISO)

    CONSTRAINT fk_lancamento_servidor
        FOREIGN KEY (servidor_id)
        REFERENCES servidor (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uq_lancamento_servidor_data
        UNIQUE (servidor_id, data),

    CONSTRAINT ck_lancamento_data_format
        CHECK (data GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]') -- valida o formato ISO (YYYY-MM-DD)
);

-- tabela: trecho
-- cada trecho representa um par (hora de referência, hora comparada) + valor, dentro de um lançamento.
-- a coluna `ordem` preserva a sequência em que os trechos foram informados (ida, volta, conexões...).
CREATE TABLE IF NOT EXISTS trecho (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    lancamento_id INTEGER NOT NULL, -- id do lançamento (FK)
    ordem INTEGER NOT NULL, -- 1, 2, 3... preserva a ordem dos trechos
    hora_referencia TEXT NOT NULL, -- HH:mm (ISO)
    hora_comparada TEXT NOT NULL, -- HH:mm (ISO)
    valor NUMERIC(10,2) NOT NULL, -- valor pago no trecho

    CONSTRAINT fk_trecho_lancamento
        FOREIGN KEY (lancamento_id)
        REFERENCES lancamento (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT uq_trecho_lancamento_ordem
        UNIQUE (lancamento_id, ordem)
);

-- índices
CREATE INDEX IF NOT EXISTS idx_lancamento_servidor_data
    ON lancamento (servidor_id, data);

CREATE INDEX IF NOT EXISTS idx_trecho_lancamento
    ON trecho (lancamento_id, ordem);