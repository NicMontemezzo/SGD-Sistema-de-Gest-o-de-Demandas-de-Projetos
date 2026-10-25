-- ATENÇÃO: a tabela empresa é do Integrante 3. Alinhe as colunas com ele antes do PR.
CREATE TABLE empresa (
    id      BIGSERIAL PRIMARY KEY,
    nome    VARCHAR(150) NOT NULL,
    cnpj    VARCHAR(18)  NOT NULL UNIQUE,
    versao  BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE demanda (
    id           BIGSERIAL PRIMARY KEY,
    titulo       VARCHAR(120)  NOT NULL,
    descricao    VARCHAR(2000) NOT NULL,
    tecnologias  VARCHAR(500),
    empresa_id   BIGINT        NOT NULL,
    situacao     VARCHAR(20)   NOT NULL DEFAULT 'DISPONIVEL',
    versao       BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT fk_demanda_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT uk_demanda_titulo_empresa UNIQUE (titulo, empresa_id)
);

CREATE INDEX idx_demanda_empresa ON demanda (empresa_id);
