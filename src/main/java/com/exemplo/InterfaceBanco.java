package com.exemplo;

import java.util.Scanner;
import java.util.List;

public class InterfaceBanco {
    private final Banco banco;
    private final Scanner scanner;
    private final Autenticacao autenticacao;

    public InterfaceBanco(Banco banco) {
        this.banco = banco;
        this.scanner = new Scanner(System.in);
        this.autenticacao = new Autenticacao(banco, scanner);
    }

    public void iniciar() {
        String usuarioLogado = null;
        while (true) {
            if (usuarioLogado == null) {
                autenticacao.exibirMenuLogin();
                String opcao = scanner.nextLine();
                switch (opcao) {
                    case "1":
                        autenticacao.registro();
                        break;
                    case "2":
                        usuarioLogado = autenticacao.login();
                        break;
                    case "3":
                        System.out.println("Obrigado por usar o Brasisco!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Opção inválida.");
                }
            } else {
                if (banco.isAdmin(usuarioLogado)) {
                    menuAdmin();
                    usuarioLogado = null;
                } else {
                    menuUsuario(usuarioLogado);
                    usuarioLogado = null;
                }
            }
        }
    }

    private void menuAdmin() {
        while (true) {
            System.out.println("\n=== Menu do Administrador - Brasisco ===");
            System.out.println("1. Consultar histórico de transações");
            System.out.println("2. Consultar denúncias pendentes");
            System.out.println("3. Logout");
            System.out.print("Escolha uma opção: ");
            String opcao = scanner.nextLine();
            switch (opcao) {
                case "1":
                    List<Transacao> historico = banco.consultarHistorico();
                    if (historico.isEmpty()) {
                        System.out.println("Nenhuma transação encontrada no sistema.");
                    } else {
                        System.out.println("\n=== Histórico de Transações - Brasisco ===");
                        for (Transacao t : historico) {
                            System.out.println("\n" + t);
                        }
                    }
                    break;
                case "2":
                    List<Denuncia> denuncias = banco.consultarDenuncias();
                    if (denuncias.isEmpty()) {
                        System.out.println("Nenhuma denúncia pendente.");
                    } else {
                        System.out.println("\n=== Denúncias Pendentes - Brasisco ===");
                        for (Denuncia d : denuncias) {
                            System.out.println("\n" + d);
                            System.out.print("Deseja reverter esta transação? (S/N): ");
                            String resposta = scanner.nextLine().toUpperCase();
                            if (resposta.equals("S")) {
                                if (banco.reverterTransferencia(d.getIdTransacao())) {
                                    System.out.println("Transferência revertida com sucesso!");
                                } else {
                                    System.out.println("Erro ao reverter transferência.");
                                }
                            }
                        }
                    }
                    break;
                case "3":
                    System.out.println("Logout realizado.");
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private void menuUsuario(String email) {
        String nomeUsuario = banco.getNomeUsuario(email);
        while (true) {
            System.out.println("\n=== Menu do Usuário: " + nomeUsuario + " - Brasisco ===");
            System.out.println("1. Consultar saldo");
            System.out.println("2. Depositar");
            System.out.println("3. Transferir");
            System.out.println("4. Consultar histórico");
            System.out.println("5. Logout");
            System.out.print("Escolha uma opção: ");
            String opcao = scanner.nextLine();
            switch (opcao) {
                case "1":
                    double saldo = banco.consultarSaldo(email);
                    System.out.println("Seu saldo: R$ " + String.format("%.2f", saldo));
                    break;
                case "2":
                    System.out.print("Valor para depositar: ");
                    double valorDep = Double.parseDouble(scanner.nextLine());
                    banco.depositar(email, valorDep);
                    System.out.println("Depósito realizado com sucesso no Brasisco!");
                    break;
                case "3":
                    System.out.print("Email do usuário destino: ");
                    String destino = scanner.nextLine();
                    System.out.print("Valor para transferir: ");
                    double valorTransf = Double.parseDouble(scanner.nextLine());
                    if (banco.transferir(email, destino, valorTransf)) {
                        System.out.println("Transferência realizada com sucesso no Brasisco!");
                    } else {
                        System.out.println("Erro na transferência.");
                    }
                    break;
                case "4":
                    List<Transacao> historico = banco.consultarHistoricoUsuario(email);
                    if (historico.isEmpty()) {
                        System.out.println("Nenhuma transação encontrada.");
                    } else {
                        System.out.println("\n=== Seu Histórico de Transações - Brasisco ===");
                        for (Transacao t : historico) {
                            System.out.println("\n" + t);
                            if (t.getDenunciaId() == 0 && t.getTipo().equals("transferencia")) {
                                System.out.print("Deseja denunciar esta transação? (S/N): ");
                                String resposta = scanner.nextLine().toUpperCase();
                                if (resposta.equals("S")) {
                                    System.out.print("Digite a descrição da denúncia: ");
                                    String descricao = scanner.nextLine();
                                    if (banco.registrarDenuncia(t.getId(), email, descricao)) {
                                        System.out.println("Denúncia registrada com sucesso!");
                                    } else {
                                        System.out.println("Erro ao registrar denúncia.");
                                    }
                                }
                            }
                        }
                    }
                    break;
                case "5":
                    System.out.println("Logout realizado.");
                    return;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }
} 