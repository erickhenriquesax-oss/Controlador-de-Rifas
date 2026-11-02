package com.sorteio.controlador.enums;

public enum UserRoles {
    ADMIN(1L, "ROLE_ADMIN");

    private final Long codigo;          // id da tabela tb_tipo_usuario
    private final String authority;    // usado pelo Spring Security

    UserRoles(Long codigo, String authority) {
        this.codigo = codigo;
        this.authority = authority;
    }

    public Long getCodigo() { return codigo; }
    public String getAuthority() { return authority; }


    public static UserRoles fromCodigo(Long codigo) {
        for (UserRoles r : values()) {
            if (r.codigo.equals(codigo)) return r;
        }
        throw new IllegalArgumentException("Código de role desconhecido: " + codigo);
    }

    public static UserRoles fromDescricao(String descricao) {
        if (descricao == null) return null;
        switch (descricao.trim().toUpperCase()) {
            case "ADMIN":   return ADMIN;
            default: throw new IllegalArgumentException("Descrição de role desconhecida: " + descricao);
        }
    }
}

