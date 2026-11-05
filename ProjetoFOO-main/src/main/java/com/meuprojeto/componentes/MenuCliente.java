package com.meuprojeto.componentes;

import com.meuprojeto.dao.ClienteDao;
import com.meuprojeto.dominio.Cliente;
import com.meuprojeto.dominio.Endereco;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

public final class MenuCliente {

    private static final ClienteDao dao = new ClienteDao();

    private MenuCliente() {
    }

    public static void exibir(Scanner scanner) {
        int opcao;

        do {
            System.out.println("\n===== MENU CLIENTE =====");
            System.out.println("1 - Cadastrar cliente");
            System.out.println("2 - Listar clientes");
            System.out.println("3 - Buscar cliente");
            System.out.println("4 - Atualizar cliente");
            System.out.println("5 - Deletar cliente");
            System.out.println("0 - Voltar");
            System.out.print("Escolha uma opção: ");
            opcao = MenuGeral.lerOpcao(scanner);

            switch (opcao) {
                case 1 -> cadastrar(scanner);
                case 2 -> listar();
                case 3 -> buscar(scanner);
                case 4 -> atualizar(scanner);
                case 5 -> deletar(scanner);
                case 0 -> System.out.println("Voltando ao menu geral...");
                default -> System.out.println("Opção inválida!");
            }
        } while (opcao != 0);
    }

    // ---------------------------------------------------------------- CREATE

    private static void cadastrar(Scanner scanner) {
        System.out.println("\n--- Cadastrar cliente ---");

        String nome = Entrada.texto(scanner, "Nome", null);
        LocalDate nascimento = Entrada.data(scanner, "Data de nascimento", null);
        String email = lerEmail(scanner, null);
        String telefone = Entrada.texto(scanner, "Telefone", null);
        String genero = Entrada.texto(scanner, "Gênero", null);
        String cpf = lerCpf(scanner, null, -1);
        Endereco endereco = lerEndereco(scanner, null);
        String criadoPor = Entrada.texto(scanner, "Criado por", null);

        Cliente cliente = new Cliente(nome, nascimento, email, telefone, genero, cpf,
                endereco, LocalDate.now(), criadoPor);
        dao.salvar(cliente);

        System.out.println("Cliente cadastrado com sucesso!");
    }

    // ------------------------------------------------------------------ READ

    private static void listar() {
        List<Cliente> clientes = dao.listar();

        if (clientes.isEmpty()) {
            System.out.println("\nNenhum cliente cadastrado.");
            return;
        }

        System.out.println("\n--- Clientes cadastrados (" + clientes.size() + ") ---");
        for (int i = 0; i < clientes.size(); i++) {
            exibirResumo(i, clientes.get(i));
        }
    }

    private static void buscar(Scanner scanner) {
        List<Cliente> clientes = dao.listar();

        if (clientes.isEmpty()) {
            System.out.println("\nNenhum cliente cadastrado.");
            return;
        }

        System.out.println("\n--- Buscar cliente ---");
        System.out.println("1 - Buscar pelo número");
        System.out.println("2 - Buscar pelo nome");
        System.out.println("3 - Buscar pelo CPF");
        System.out.print("Escolha uma opção: ");
        int tipo = MenuGeral.lerOpcao(scanner);

        switch (tipo) {
            case 1 -> {
                int indice = Entrada.indice(scanner, "Número do cliente", clientes.size());
                if (indice >= 0) {
                    exibirDetalhes(indice, dao.buscar(indice));
                }
            }
            case 2 -> {
                System.out.print("Nome (ou parte dele): ");
                String termo = Entrada.normalizar(scanner.nextLine());
                buscarPor(clientes, c -> Entrada.normalizar(c.getNome()).contains(termo), termo);
            }
            case 3 -> {
                System.out.print("CPF: ");
                String cpf = somenteDigitos(scanner.nextLine());
                buscarPor(clientes, c -> c.getCpf().equals(cpf), cpf);
            }
            default -> System.out.println("Opção inválida!");
        }
    }

    private static void buscarPor(List<Cliente> clientes, Predicate<Cliente> filtro, String termo) {
        if (termo.isEmpty()) {
            System.out.println("Digite algo para buscar.");
            return;
        }

        boolean achou = false;
        for (int i = 0; i < clientes.size(); i++) {
            if (filtro.test(clientes.get(i))) {
                exibirDetalhes(i, clientes.get(i));
                achou = true;
            }
        }
        if (!achou) {
            System.out.println("Nenhum cliente encontrado.");
        }
    }

    // ---------------------------------------------------------------- UPDATE

    private static void atualizar(Scanner scanner) {
        List<Cliente> clientes = dao.listar();

        if (clientes.isEmpty()) {
            System.out.println("\nNenhum cliente cadastrado.");
            return;
        }

        listar();
        System.out.println();
        int indice = Entrada.indice(scanner, "Número do cliente a atualizar", clientes.size());
        if (indice < 0) {
            return;
        }

        Cliente atual = dao.buscar(indice);
        System.out.println("\n--- Atualizar cliente (ENTER mantém o valor atual) ---");

        Cliente novo = new Cliente();
        novo.setNome(Entrada.texto(scanner, "Nome", atual.getNome()));
        novo.setDataNascimento(Entrada.data(scanner, "Data de nascimento", atual.getDataNascimento()));
        novo.setEmail(lerEmail(scanner, atual.getEmail()));
        novo.setTelefone(Entrada.texto(scanner, "Telefone", atual.getTelefone()));
        novo.setGenero(Entrada.texto(scanner, "Gênero", atual.getGenero()));
        novo.setCpf(lerCpf(scanner, atual.getCpf(), indice));
        novo.setEndereco(lerEndereco(scanner, atual.getEndereco()));

        // dados de auditoria não mudam na atualização
        novo.setDataCriacao(atual.getDataCriacao());
        novo.setCriadoPor(atual.getCriadoPor());

        dao.atualizar(indice, novo);
        System.out.println("Cliente atualizado com sucesso!");
    }

    // ---------------------------------------------------------------- DELETE

    private static void deletar(Scanner scanner) {
        List<Cliente> clientes = dao.listar();

        if (clientes.isEmpty()) {
            System.out.println("\nNenhum cliente cadastrado.");
            return;
        }

        listar();
        System.out.println();
        int indice = Entrada.indice(scanner, "Número do cliente a deletar", clientes.size());
        if (indice < 0) {
            return;
        }

        Cliente cliente = dao.buscar(indice);
        if (Entrada.confirmar(scanner, "Deseja realmente deletar \"" + cliente.getNome() + "\"?")) {
            dao.deletar(indice);
            System.out.println("Cliente deletado com sucesso!");
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    // --------------------------------------------------------------- AUXILIARES

    private static String lerEmail(Scanner scanner, String atual) {
        return Entrada.texto(scanner, "E-mail", atual,
                v -> v.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"),
                "E-mail inválido. Exemplo: nome@dominio.com");
    }

    /**
     * Lê o CPF (11 dígitos) e impede duplicidade.
     * indiceIgnorado = posição do próprio cliente na atualização (para não acusar duplicidade
     * consigo mesmo); use -1 no cadastro.
     */
    private static String lerCpf(Scanner scanner, String atual, int indiceIgnorado) {
        while (true) {
            String entrada = Entrada.texto(scanner, "CPF (somente números)",
                    atual == null ? null : formatarCpf(atual));
            String cpf = somenteDigitos(entrada);

            if (cpf.length() != 11) {
                System.out.println("CPF inválido. Deve ter 11 dígitos.");
                continue;
            }
            if (cpfJaCadastrado(cpf, indiceIgnorado)) {
                System.out.println("Já existe um cliente com esse CPF.");
                continue;
            }
            return cpf;
        }
    }

    private static boolean cpfJaCadastrado(String cpf, int indiceIgnorado) {
        List<Cliente> clientes = dao.listar();
        for (int i = 0; i < clientes.size(); i++) {
            if (i != indiceIgnorado && clientes.get(i).getCpf().equals(cpf)) {
                return true;
            }
        }
        return false;
    }

    private static Endereco lerEndereco(Scanner scanner, Endereco atual) {
        System.out.println("Endereço:");
        String rua = Entrada.texto(scanner, "  Rua", atual == null ? null : atual.getRua());
        int numero = Entrada.inteiro(scanner, "  Número", atual == null ? null : atual.getNumero(), 0);
        String cidade = Entrada.texto(scanner, "  Cidade", atual == null ? null : atual.getCidade());
        return new Endereco(rua, numero, cidade);
    }

    private static String somenteDigitos(String texto) {
        return texto.replaceAll("\\D", "");
    }

    private static String formatarCpf(String cpf) {
        if (cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    private static void exibirResumo(int indice, Cliente c) {
        System.out.println("[" + (indice + 1) + "] " + c.getNome()
                + " | CPF: " + formatarCpf(c.getCpf())
                + " | " + c.getEmail()
                + " | " + c.getTelefone());
    }

    private static void exibirDetalhes(int indice, Cliente c) {
        System.out.println("\n[" + (indice + 1) + "] " + c.getNome());
        Entrada.linha("CPF", formatarCpf(c.getCpf()));
        Entrada.linha("Nascimento", Entrada.formatarData(c.getDataNascimento()));
        Entrada.linha("Gênero", c.getGenero());
        Entrada.linha("E-mail", c.getEmail());
        Entrada.linha("Telefone", c.getTelefone());
        Entrada.linha("Endereço", c.getEndereco());
        Entrada.linha("Cadastrado em", Entrada.formatarData(c.getDataCriacao()) + " por " + c.getCriadoPor());
    }
}
