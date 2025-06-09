package com.exemplo;

import java.util.Scanner;

public class Autenticacao {
    private final Banco banco;
    private final Scanner scanner;

    public Autenticacao(Banco banco, Scanner scanner) {
        this.banco = banco;
        this.scanner = scanner;
    }

    public String login() {
        System.out.print("Digite seu email: ");
        String email = scanner.nextLine();
        System.out.print("Digite sua senha: ");
        String senha = scanner.nextLine();

        if (banco.autenticar(email, senha)) {
            String nomeUsuario = banco.getNomeUsuario(email);
            System.out.println("Login realizado com sucesso! Bem-vindo ao Brasisco, " + nomeUsuario + "!");
            return email;
        } else {
            System.out.println("Email ou senha incorretos.");
            return null;
        }
    }

    public void registro() {
        System.out.print("Digite seu email: ");
        String email = scanner.nextLine();
        System.out.print("Digite seu nome: ");
        String nome = scanner.nextLine();
        System.out.print("Digite sua senha: ");
        String senha = scanner.nextLine();

        banco.adicionarUsuario(email, nome, senha);
        System.out.println("Usuário registrado com sucesso no Brasisco! Saldo inicial: R$ 1000,00");
    }

    public void exibirMenuLogin() {
        System.out.println("\n=== Bem-vindo ao Brasisco ===");
        System.out.println("1. Registrar");
        System.out.println("2. Login");
        System.out.println("3. Sair");
        System.out.print("Escolha uma opção: ");
    }
} 