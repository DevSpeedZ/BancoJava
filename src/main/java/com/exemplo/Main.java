package com.exemplo;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Banco banco = new Banco();
        InterfaceBanco interfaceBanco = new InterfaceBanco(banco);
        interfaceBanco.iniciar();
    }
} 