CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE orcamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    os_id UUID NOT NULL,
    saga_id UUID NOT NULL,
    data_emissao TIMESTAMP NOT NULL,
    data_validade TIMESTAMP NOT NULL,
    valor_total_materiais DECIMAL(19,2) NOT NULL,
    valor_total_mao_de_obra DECIMAL(19,2) NOT NULL,
    valor_impostos DECIMAL(19,2) NOT NULL,
    valor_total DECIMAL(19,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orcamentos_os_id ON orcamentos(os_id);
CREATE INDEX idx_orcamentos_saga_id ON orcamentos(saga_id);
CREATE INDEX idx_orcamentos_status ON orcamentos(status);

CREATE TABLE pagamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orcamento_id UUID NOT NULL REFERENCES orcamentos(id),
    mp_preference_id VARCHAR(255),
    mp_payment_id VARCHAR(100),
    mp_init_point VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pagamentos_orcamento_id ON pagamentos(orcamento_id);
CREATE INDEX idx_pagamentos_status ON pagamentos(status);
