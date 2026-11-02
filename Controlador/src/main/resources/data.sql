INSERT INTO tb_tipo_usuario (id_tipo_usuario, descricao)
VALUES (1, 'ADMIN')
ON CONFLICT (id_tipo_usuario) DO NOTHING;
