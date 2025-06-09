package com.exemplo;

import java.sql.Timestamp;

public class Denuncia {
    private final int id;
    private final int idTransacao;
    private final String emailDenunciante;
    private final String descricao;
    private final String status;
    private final Timestamp dataDenuncia;
    private final String emailOrigem;
    private final String emailDestino;
    private final String nomeOrigem;
    private final String nomeDestino;
    private final double valor;
    private final String tipo;

    public Denuncia(int id, int idTransacao, String emailDenunciante, String descricao, 
                   String status, Timestamp dataDenuncia, String emailOrigem, String emailDestino,
                   String nomeOrigem, String nomeDestino, double valor, String tipo) {
        this.id = id;
        this.idTransacao = idTransacao;
        this.emailDenunciante = emailDenunciante;
        this.descricao = descricao;
        this.status = status;
        this.dataDenuncia = dataDenuncia;
        this.emailOrigem = emailOrigem;
        this.emailDestino = emailDestino;
        this.nomeOrigem = nomeOrigem;
        this.nomeDestino = nomeDestino;
        this.valor = valor;
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID da Denúncia: ").append(id).append("\n");
        sb.append("ID da Transação: ").append(idTransacao).append("\n");
        sb.append("Denunciante: ").append(emailDenunciante).append("\n");
        sb.append("Descrição: ").append(descricao).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Data da Denúncia: ").append(dataDenuncia).append("\n");
        sb.append("\nDetalhes da Transação:\n");
        sb.append("Tipo: ").append(tipo).append("\n");
        sb.append("Valor: R$ ").append(String.format("%.2f", valor)).append("\n");
        sb.append("De: ").append(nomeOrigem).append(" (").append(emailOrigem).append(")\n");
        sb.append("Para: ").append(nomeDestino).append(" (").append(emailDestino).append(")\n");
        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public int getIdTransacao() {
        return idTransacao;
    }

    public String getStatus() {
        return status;
    }
} 