package com.minimarket.security;

enum TestRole {
    CLIENTE("cliente", "cliente123"),
    EMPLEADO("empleado", "empleado123"),
    GERENTE("gerente", "gerente123"),
    ADMIN("admin", "admin123");

    private final String username;
    private final String password;

    TestRole(String username, String password) {
        this.username = username;
        this.password = password;
    }

    String username() {
        return username;
    }

    String password() {
        return password;
    }
}
