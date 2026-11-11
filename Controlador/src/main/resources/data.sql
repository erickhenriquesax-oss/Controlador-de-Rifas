INSERT INTO tb_tipo_usuario (id_tipo_usuario, descricao)
VALUES (1, 'ADMIN')
ON CONFLICT (id_tipo_usuario) DO NOTHING;

-- O INSERT acima usa id explícito e não avança a sequence; sem isso o primeiro
-- POST /controlador/tipo-usuario tentaria gerar o id 1 de novo e falharia.
SELECT setval(pg_get_serial_sequence('tb_tipo_usuario', 'id_tipo_usuario'),
              (SELECT MAX(id_tipo_usuario) FROM tb_tipo_usuario));
