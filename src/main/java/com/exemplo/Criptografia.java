package com.exemplo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class Criptografia {
    
    /**
     * Gera um hash SHA-256 para a senha fornecida
     * @param senha A senha em texto puro
     * @return O hash SHA-256 da senha em formato hexadecimal, ou null se houver erro
     */
    public static String criptografar(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(senha.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Erro ao criptografar senha: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica se uma senha corresponde ao hash armazenado
     * @param senha A senha em texto puro
     * @param hashArmazenado O hash armazenado no banco de dados
     * @return true se a senha corresponder ao hash, false caso contrário
     */
    public static boolean verificarSenha(String senha, String hashArmazenado) {
        String hashSenha = criptografar(senha);
        return hashSenha != null && hashSenha.equals(hashArmazenado);
    }
} 