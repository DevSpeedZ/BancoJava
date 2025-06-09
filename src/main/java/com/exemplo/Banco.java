package com.exemplo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe principal que gerencia todas as operações bancárias
 * Implementa operações como depósito, transferência, consulta de saldo e histórico
 */
public class Banco {
    private final Criptografia criptografia;
    private static final String DB_URL = "jdbc:sqlite:banco.db";

    public Banco() {
        this.criptografia = new Criptografia();
        inicializarBanco();
    }

    /**
     * Inicializa o banco de dados criando as tabelas necessárias e a conta de administrador
     */
    private void inicializarBanco() {
        try (Connection conn = conectarBanco()) {
            criarTabelas(conn);
            criarAdmin(conn);
        } catch (SQLException e) {
            System.out.println("Erro ao inicializar banco de dados do Brasisco: " + e.getMessage());
        }
    }

    private Connection conectarBanco() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(DB_URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite não encontrado: " + e.getMessage());
        }
    }

    private void criarTabelas(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Primeiro, dropar a tabela historico se ela existir
            stmt.execute("DROP TABLE IF EXISTS historico");
            
            stmt.execute("CREATE TABLE IF NOT EXISTS usuarios (" +
                    "email TEXT PRIMARY KEY," +
                    "nome TEXT NOT NULL," +
                    "senha TEXT NOT NULL," +
                    "saldo REAL NOT NULL," +
                    "is_admin INTEGER DEFAULT 0)");
            
            stmt.execute("CREATE TABLE historico (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email_origem TEXT," +
                    "email_destino TEXT," +
                    "tipo TEXT NOT NULL," +
                    "valor REAL NOT NULL," +
                    "data TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(email_origem) REFERENCES usuarios(email)," +
                    "FOREIGN KEY(email_destino) REFERENCES usuarios(email))");

            stmt.execute("CREATE TABLE IF NOT EXISTS denuncias (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "id_transacao INTEGER NOT NULL," +
                    "email_denunciante TEXT NOT NULL," +
                    "descricao TEXT NOT NULL," +
                    "status TEXT DEFAULT 'PENDENTE'," +
                    "data_denuncia TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(id_transacao) REFERENCES historico(id)," +
                    "FOREIGN KEY(email_denunciante) REFERENCES usuarios(email))");
        }
    }

    private void criarAdmin(Connection conn) throws SQLException {
        String senhaHash = "6f403cce6bb38bd0a424f416cc7250372dd3977d6f4740cd1db4ab569400a8ac";
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT OR IGNORE INTO usuarios (email, nome, senha, saldo, is_admin) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, "admin@banco.com");   
            stmt.setString(2, "Administrador");
            stmt.setString(3, senhaHash);
            stmt.setDouble(4, 0.0);
            stmt.setInt(5, 1);
            stmt.executeUpdate();
        }
    }

    public void adicionarUsuario(String email, String nome, String senha) {
        try (Connection conn = conectarBanco()) {
            String senhaHash = criptografia.criptografar(senha);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO usuarios (email, nome, senha, saldo) VALUES (?, ?, ?, ?)")) {
                stmt.setString(1, email);
                stmt.setString(2, nome);
                stmt.setString(3, senhaHash);
                stmt.setDouble(4, 1000.0);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao adicionar usuário no Brasisco: " + e.getMessage());
        }
    }

    public boolean autenticar(String email, String senha) {
        try (Connection conn = conectarBanco()) {
            String senhaHash = criptografia.criptografar(senha);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM usuarios WHERE email = ? AND senha = ?")) {
                stmt.setString(1, email);
                stmt.setString(2, senhaHash);
                return stmt.executeQuery().next();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao autenticar usuário no Brasisco: " + e.getMessage());
            return false;
        }
    }

    public boolean isAdmin(String email) {
        try (Connection conn = conectarBanco()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT is_admin FROM usuarios WHERE email = ?")) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                return rs.next() && rs.getInt("is_admin") == 1;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar status de administrador no Brasisco: " + e.getMessage());
            return false;
        }
    }

    public String getNomeUsuario(String email) {
        try (Connection conn = conectarBanco()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT nome FROM usuarios WHERE email = ?")) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("nome");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter nome do usuário no Brasisco: " + e.getMessage());
        }
        return null;
    }

    public double consultarSaldo(String email) {
        try (Connection conn = conectarBanco()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT saldo FROM usuarios WHERE email = ?")) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("saldo");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao consultar saldo no Brasisco: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Realiza um depósito na conta do usuário
     * @param email Email do usuário
     * @param valor Valor a ser depositado
     */
    public void depositar(String email, double valor) {
        try (Connection conn = conectarBanco()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE usuarios SET saldo = saldo + ? WHERE email = ?")) {
                    stmt.setDouble(1, valor);
                    stmt.setString(2, email);
                    stmt.executeUpdate();
                }

                registrarTransacao(conn, email, null, "deposito", valor);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao realizar depósito no Brasisco: " + e.getMessage());
        }
    }

    /**
     * Realiza uma transferência entre contas
     * @param emailOrigem Email da conta de origem
     * @param emailDestino Email da conta de destino
     * @param valor Valor a ser transferido
     * @return true se a transferência foi bem sucedida, false caso contrário
     */
    public boolean transferir(String emailOrigem, String emailDestino, double valor) {
        try (Connection conn = conectarBanco()) {
            if (consultarSaldo(emailOrigem) < valor) {
                System.out.println("Saldo insuficiente no Brasisco.");
                return false;
            }

            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE usuarios SET saldo = saldo - ? WHERE email = ?")) {
                    stmt.setDouble(1, valor);
                    stmt.setString(2, emailOrigem);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE usuarios SET saldo = saldo + ? WHERE email = ?")) {
                    stmt.setDouble(1, valor);
                    stmt.setString(2, emailDestino);
                    stmt.executeUpdate();
                }

                int idTransacao = registrarTransacao(conn, emailOrigem, emailDestino, "transferencia", valor);
                if (idTransacao == -1) {
                    throw new SQLException("Erro ao registrar transação");
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao realizar transferência no Brasisco: " + e.getMessage());
            return false;
        }
    }

    /**
     * Registra uma transação no histórico
     * @param conn Conexão com o banco de dados
     * @param emailOrigem Email da conta de origem
     * @param emailDestino Email da conta de destino
     * @param tipo Tipo da transação (deposito, transferencia, etc)
     * @param valor Valor da transação
     * @return ID da transação registrada ou -1 em caso de erro
     */
    private int registrarTransacao(Connection conn, String emailOrigem, String emailDestino, String tipo, double valor) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO historico (email_origem, email_destino, tipo, valor) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, emailOrigem);
            stmt.setString(2, emailDestino);
            stmt.setString(3, tipo);
            stmt.setDouble(4, valor);
            stmt.executeUpdate();
            
            try (Statement stmt2 = conn.createStatement();
                 ResultSet rs = stmt2.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Consulta o histórico completo de transações
     * @return Lista de todas as transações realizadas
     */
    public List<Transacao> consultarHistorico() {
        List<Transacao> historico = new ArrayList<>();
        try (Connection conn = conectarBanco()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT h.*, u1.nome as nome_origem, u2.nome as nome_destino " +
                    "FROM historico h " +
                    "LEFT JOIN usuarios u1 ON h.email_origem = u1.email " +
                    "LEFT JOIN usuarios u2 ON h.email_destino = u2.email " +
                    "ORDER BY h.data DESC")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Transacao t = new Transacao(
                            rs.getString("email_origem"),
                            rs.getString("email_destino"),
                            rs.getString("nome_origem"),
                            rs.getString("nome_destino"),
                            rs.getString("tipo"),
                            rs.getDouble("valor"),
                            rs.getTimestamp("data")
                        );
                        t.setId(rs.getInt("id"));
                        historico.add(t);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao consultar histórico do Brasisco: " + e.getMessage());
        }
        return historico;
    }

    public List<Transacao> consultarHistoricoUsuario(String email) {
        List<Transacao> historico = new ArrayList<>();
        try (Connection conn = conectarBanco()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT h.*, u1.nome as nome_origem, u2.nome as nome_destino, " +
                    "d.id as denuncia_id, d.status as denuncia_status " +
                    "FROM historico h " +
                    "LEFT JOIN usuarios u1 ON h.email_origem = u1.email " +
                    "LEFT JOIN usuarios u2 ON h.email_destino = u2.email " +
                    "LEFT JOIN denuncias d ON h.id = d.id_transacao " +
                    "WHERE h.email_origem = ? OR h.email_destino = ? " +
                    "ORDER BY h.data DESC")) {
                stmt.setString(1, email);
                stmt.setString(2, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Transacao t = new Transacao(
                            rs.getString("email_origem"),
                            rs.getString("email_destino"),
                            rs.getString("nome_origem"),
                            rs.getString("nome_destino"),
                            rs.getString("tipo"),
                            rs.getDouble("valor"),
                            rs.getTimestamp("data")
                        );
                        t.setId(rs.getInt("id"));
                        if (rs.getInt("denuncia_id") > 0) {
                            t.setDenunciaId(rs.getInt("denuncia_id"));
                            t.setDenunciaStatus(rs.getString("denuncia_status"));
                        }
                        historico.add(t);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao consultar histórico do usuário no Brasisco: " + e.getMessage());
        }
        return historico;
    }

    /**
     * Registra uma denúncia relacionada a uma transação
     * @param idTransacao ID da transação denunciada
     * @param emailDenunciante Email do usuário que fez a denúncia
     * @param descricao Descrição da denúncia
     * @return true se a denúncia foi registrada com sucesso
     */
    public boolean registrarDenuncia(int idTransacao, String emailDenunciante, String descricao) {
        try (Connection conn = conectarBanco()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO denuncias (id_transacao, email_denunciante, descricao) VALUES (?, ?, ?)")) {
                stmt.setInt(1, idTransacao);
                stmt.setString(2, emailDenunciante);
                stmt.setString(3, descricao);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao registrar denúncia no Brasisco: " + e.getMessage());
            return false;
        }
    }

    public List<Denuncia> consultarDenuncias() {
        List<Denuncia> denuncias = new ArrayList<>();
        try (Connection conn = conectarBanco()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT d.*, h.email_origem, h.email_destino, h.valor, h.tipo, " +
                    "u1.nome as nome_origem, u2.nome as nome_destino " +
                    "FROM denuncias d " +
                    "JOIN historico h ON d.id_transacao = h.id " +
                    "LEFT JOIN usuarios u1 ON h.email_origem = u1.email " +
                    "LEFT JOIN usuarios u2 ON h.email_destino = u2.email " +
                    "WHERE d.status = 'PENDENTE' " +
                    "ORDER BY d.data_denuncia DESC")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        denuncias.add(new Denuncia(
                            rs.getInt("id"),
                            rs.getInt("id_transacao"),
                            rs.getString("email_denunciante"),
                            rs.getString("descricao"),
                            rs.getString("status"),
                            rs.getTimestamp("data_denuncia"),
                            rs.getString("email_origem"),
                            rs.getString("email_destino"),
                            rs.getString("nome_origem"),
                            rs.getString("nome_destino"),
                            rs.getDouble("valor"),
                            rs.getString("tipo")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao consultar denúncias no Brasisco: " + e.getMessage());
        }
        return denuncias;
    }

    /**
     * Reverte uma transferência e atualiza o status da denúncia
     * @param idTransacao ID da transação a ser revertida
     * @return true se a reversão foi bem sucedida
     */
    public boolean reverterTransferencia(int idTransacao) {
        try (Connection conn = conectarBanco()) {
            conn.setAutoCommit(false);
            try {
                String emailOrigem, emailDestino;
                double valor;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT email_origem, email_destino, valor FROM historico WHERE id = ?")) {
                    stmt.setInt(1, idTransacao);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Transação não encontrada");
                        }
                        emailOrigem = rs.getString("email_origem");
                        emailDestino = rs.getString("email_destino");
                        valor = rs.getDouble("valor");
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE usuarios SET saldo = saldo + ? WHERE email = ?")) {
                    stmt.setDouble(1, valor);
                    stmt.setString(2, emailOrigem);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE usuarios SET saldo = saldo - ? WHERE email = ?")) {
                    stmt.setDouble(1, valor);
                    stmt.setString(2, emailDestino);
                    stmt.executeUpdate();
                }

                registrarTransacao(conn, emailDestino, emailOrigem, "reversao", valor);

                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE denuncias SET status = 'RESOLVIDA' WHERE id_transacao = ?")) {
                    stmt.setInt(1, idTransacao);
                    stmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Erro ao reverter transferência no Brasisco: " + e.getMessage());
            return false;
        }
    }
} 