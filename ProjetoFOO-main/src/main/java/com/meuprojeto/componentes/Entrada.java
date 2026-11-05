package com.meuprojeto.componentes;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Métodos auxiliares para ler e validar dados digitados no console.
 *
 * Em todos os métodos, o parâmetro "atual" serve para a ATUALIZAÇÃO:
 * se for diferente de null, o usuário pode apertar ENTER para manter o valor atual.
 * Se for null (cadastro), o campo é obrigatório.
 */
final class Entrada {

    static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Entrada() {
    }

    static String texto(Scanner scanner, String rotulo, String atual) {
        return texto(scanner, rotulo, atual, valor -> true, "");
    }

    static String texto(Scanner scanner, String rotulo, String atual,
                        Predicate<String> validador, String mensagemErro) {
        while (true) {
            System.out.print(prompt(rotulo, atual));
            String valor = scanner.nextLine().trim();

            if (valor.isEmpty()) {
                if (atual != null) {
                    return atual;
                }
                System.out.println("Campo obrigatório!");
                continue;
            }
            if (!validador.test(valor)) {
                System.out.println(mensagemErro);
                continue;
            }
            return valor;
        }
    }

    static int inteiro(Scanner scanner, String rotulo, Integer atual, int minimo) {
        while (true) {
            System.out.print(prompt(rotulo, atual == null ? null : String.valueOf(atual)));
            String valor = scanner.nextLine().trim();

            if (valor.isEmpty()) {
                if (atual != null) {
                    return atual;
                }
                System.out.println("Campo obrigatório!");
                continue;
            }
            try {
                int numero = Integer.parseInt(valor);
                if (numero < minimo) {
                    System.out.println("Digite um número maior ou igual a " + minimo + ".");
                    continue;
                }
                return numero;
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    static LocalDate data(Scanner scanner, String rotulo, LocalDate atual) {
        while (true) {
            System.out.print(prompt(rotulo + " (dd/mm/aaaa)", atual == null ? null : formatarData(atual)));
            String valor = scanner.nextLine().trim();

            if (valor.isEmpty()) {
                if (atual != null) {
                    return atual;
                }
                System.out.println("Campo obrigatório!");
                continue;
            }
            try {
                LocalDate data = LocalDate.parse(valor, FORMATO_DATA);
                if (data.isAfter(LocalDate.now())) {
                    System.out.println("A data não pode estar no futuro.");
                    continue;
                }
                return data;
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato dd/mm/aaaa (ex.: 25/12/1990).");
            }
        }
    }

    /**
     * Pergunta "Número do ..." e devolve o índice (base 0) da lista,
     * ou -1 se o valor for inválido / fora do intervalo.
     */
    static int indice(Scanner scanner, String rotulo, int tamanhoLista) {
        System.out.print(rotulo + " (número mostrado na listagem): ");
        try {
            int numero = Integer.parseInt(scanner.nextLine().trim());
            if (numero < 1 || numero > tamanhoLista) {
                System.out.println("Número inexistente! Escolha entre 1 e " + tamanhoLista + ".");
                return -1;
            }
            return numero - 1;
        } catch (NumberFormatException e) {
            System.out.println("Digite apenas o número do item.");
            return -1;
        }
    }

    static boolean confirmar(Scanner scanner, String pergunta) {
        System.out.print(pergunta + " (s/n): ");
        String resposta = scanner.nextLine().trim().toLowerCase();
        return resposta.equals("s") || resposta.equals("sim");
    }

    /** Remove acentos e coloca em minúsculas, para buscas como "joao" encontrarem "João". */
    static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }

    /** Imprime uma linha "Rótulo: valor" com os valores alinhados. */
    static void linha(String rotulo, Object valor) {
        System.out.printf("   %-17s %s%n", rotulo + ":", valor);
    }

    static String formatarData(LocalDate data) {
        return data == null ? "-" : data.format(FORMATO_DATA);
    }

    private static String prompt(String rotulo, String atual) {
        if (atual == null) {
            return rotulo + ": ";
        }
        return rotulo + " [" + atual + "] (ENTER mantém): ";
    }
}
