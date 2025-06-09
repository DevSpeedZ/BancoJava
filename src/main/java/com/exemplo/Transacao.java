package com.exemplo;

import java.sql.Timestamp;

public class Transacao {
    private int id;
    private final String emailOrigem;
    private final String emailDestino;
    private final String nomeOrigem;
    private final String nomeDestino;
    private final String tipo;
    private final double valor;
    private final Timestamp data;
    private int denunciaId;
    private String denunciaStatus;

    public Transacao(String emailOrigem, String emailDestino, String nomeOrigem, String nomeDestino, 
                    String tipo, double valor, Timestamp data) {
        this.emailOrigem = emailOrigem;
        this.emailDestino = emailDestino;
        this.nomeOrigem = nomeOrigem;
        this.nomeDestino = nomeDestino;
        this.tipo = tipo;
        this.valor = valor;
        this.data = data;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDenunciaId(int denunciaId) {
        this.denunciaId = denunciaId;
    }

    public void setDenunciaStatus(String denunciaStatus) {
        this.denunciaStatus = denunciaStatus;
    }

    public int getId() {
        return id;
    }

    public int getDenunciaId() {
        return denunciaId;
    }

    public String getDenunciaStatus() {
        return denunciaStatus;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(id).append("\n");
        sb.append("Data: ").append(data).append("\n");
        sb.append("Tipo: ").append(tipo).append("\n");
        sb.append("Valor: R$ ").append(String.format("%.2f", valor)).append("\n");
        
        if (emailOrigem != null) {
            sb.append("De: ").append(nomeOrigem).append(" (").append(emailOrigem).append(")\n");
        }
        if (emailDestino != null) {
            sb.append("Para: ").append(nomeDestino).append(" (").append(emailDestino).append(")\n");
        }
        
        if (denunciaId > 0) {
            sb.append("\nStatus da Denúncia: ").append(denunciaStatus).append("\n");
        }
        
        return sb.toString();
    }
} 