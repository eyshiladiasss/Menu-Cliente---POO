package com.meuprojeto.componentes;

import com.meuprojeto.dao.ProdutoDao;
import com.meuprojeto.dominio.Eletrodomestrico;
import com.meuprojeto.dominio.Moveis;
import com.meuprojeto.dominio.Produto;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

public final class MenuProduto {

    private static final ProdutoDao dao = new ProdutoDao();

    private MenuProduto() {
    }

    public static void exibir(Scanner scanner) {
        int opcao;

        do {
            System.out.println("\n===== MENU PRODUTO =====");
            System.out.println("1 - Cadastrar produto");
            System.out.println("2 - Listar produtos");
            System.out.println("3 - Buscar produto");
            System.out.println("4 - Atualizar produto");
            System.out.println("5 - Deletar produto");
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
        System.out.println("\n--- Cadastrar produto ---");
        System.out.println("Tipo do produto:");
        System.out.println("1 - Móveis");
        System.out.println("2 - Eletrodoméstico");
        System.out.println("0 - Cancelar");
        System.out.print("Escolha uma opção: ");
        int tipo = MenuGeral.lerOpcao(scanner);

        if (tipo == 0) {
            System.out.println("Operação cancelada.");
            return;
        }
        if (tipo != 1 && tipo != 2) {
            System.out.println("Opção inválida!");
            return;
        }

        String marca = Entrada.texto(scanner, "Marca", null);
        String nome = Entrada.texto(scanner, "Nome", null);
        String modelo = Entrada.texto(scanner, "Modelo", null);
        int quantidade = Entrada.inteiro(scanner, "Quantidade em estoque", null, 0);
        String descricao = Entrada.texto(scanner, "Descrição", null);
        String criadoPor = Entrada.texto(scanner, "Criado por", null);
        LocalDate hoje = LocalDate.now();

        Produto produto;
        if (tipo == 1) {
            String composicao = Entrada.texto(scanner, "Composição", null);
            String materiais = Entrada.texto(scanner, "Materiais", null);
            int qtdItens = Entrada.inteiro(scanner, "Quantidade de itens", null, 1);
            produto = new Moveis(marca, nome, modelo, quantidade, descricao, hoje, criadoPor,
                    composicao, materiais, qtdItens);
        } else {
            int voltagem = Entrada.inteiro(scanner, "Voltagem (V)", null, 1);
            int potencia = Entrada.inteiro(scanner, "Potência (W)", null, 1);
            String especificacoes = Entrada.texto(scanner, "Especificações", null);
            produto = new Eletrodomestrico(marca, nome, modelo, quantidade, descricao, hoje, criadoPor,
                    voltagem, potencia, especificacoes);
        }

        dao.salvar(produto);
        System.out.println("Produto cadastrado com sucesso!");
    }

    // ------------------------------------------------------------------ READ

    private static void listar() {
        List<Produto> produtos = dao.listar();

        if (produtos.isEmpty()) {
            System.out.println("\nNenhum produto cadastrado.");
            return;
        }

        System.out.println("\n--- Produtos cadastrados (" + produtos.size() + ") ---");
        for (int i = 0; i < produtos.size(); i++) {
            exibirResumo(i, produtos.get(i));
        }
    }

    private static void buscar(Scanner scanner) {
        List<Produto> produtos = dao.listar();

        if (produtos.isEmpty()) {
            System.out.println("\nNenhum produto cadastrado.");
            return;
        }

        System.out.println("\n--- Buscar produto ---");
        System.out.println("1 - Buscar pelo número");
        System.out.println("2 - Buscar pelo nome / marca / modelo");
        System.out.print("Escolha uma opção: ");
        int tipo = MenuGeral.lerOpcao(scanner);

        switch (tipo) {
            case 1 -> {
                int indice = Entrada.indice(scanner, "Número do produto", produtos.size());
                if (indice >= 0) {
                    exibirDetalhes(indice, dao.buscar(indice));
                }
            }
            case 2 -> {
                System.out.print("Nome, marca ou modelo (ou parte): ");
                String termo = Entrada.normalizar(scanner.nextLine());
                buscarPor(produtos, p -> Entrada.normalizar(p.getNome()).contains(termo)
                        || Entrada.normalizar(p.getMarca()).contains(termo)
                        || Entrada.normalizar(p.getModelo()).contains(termo), termo);
            }
            default -> System.out.println("Opção inválida!");
        }
    }

    private static void buscarPor(List<Produto> produtos, Predicate<Produto> filtro, String termo) {
        if (termo.isEmpty()) {
            System.out.println("Digite algo para buscar.");
            return;
        }

        boolean achou = false;
        for (int i = 0; i < produtos.size(); i++) {
            if (filtro.test(produtos.get(i))) {
                exibirDetalhes(i, produtos.get(i));
                achou = true;
            }
        }
        if (!achou) {
            System.out.println("Nenhum produto encontrado.");
        }
    }

    // ---------------------------------------------------------------- UPDATE

    private static void atualizar(Scanner scanner) {
        List<Produto> produtos = dao.listar();

        if (produtos.isEmpty()) {
            System.out.println("\nNenhum produto cadastrado.");
            return;
        }

        listar();
        System.out.println();
        int indice = Entrada.indice(scanner, "Número do produto a atualizar", produtos.size());
        if (indice < 0) {
            return;
        }

        Produto atual = dao.buscar(indice);
        System.out.println("\n--- Atualizar produto (ENTER mantém o valor atual) ---");

        String marca = Entrada.texto(scanner, "Marca", atual.getMarca());
        String nome = Entrada.texto(scanner, "Nome", atual.getNome());
        String modelo = Entrada.texto(scanner, "Modelo", atual.getModelo());
        int quantidade = Entrada.inteiro(scanner, "Quantidade em estoque", atual.getQuantidade(), 0);
        String descricao = Entrada.texto(scanner, "Descrição", atual.getDescricacao());

        // o tipo do produto é mantido; só os campos específicos de cada tipo são pedidos
        Produto novo;
        if (atual instanceof Moveis m) {
            String composicao = Entrada.texto(scanner, "Composição", m.getComposicao());
            String materiais = Entrada.texto(scanner, "Materiais", m.getMateriais());
            int qtdItens = Entrada.inteiro(scanner, "Quantidade de itens", m.getQtdItens(), 1);
            novo = new Moveis(marca, nome, modelo, quantidade, descricao,
                    atual.getDataCriacao(), atual.getCriadoPor(), composicao, materiais, qtdItens);
        } else if (atual instanceof Eletrodomestrico e) {
            int voltagem = Entrada.inteiro(scanner, "Voltagem (V)", e.getVoltagem(), 1);
            int potencia = Entrada.inteiro(scanner, "Potência (W)", e.getPotencial(), 1);
            String especificacoes = Entrada.texto(scanner, "Especificações", e.getEspecificacoes());
            novo = new Eletrodomestrico(marca, nome, modelo, quantidade, descricao,
                    atual.getDataCriacao(), atual.getCriadoPor(), voltagem, potencia, especificacoes);
        } else {
            System.out.println("Tipo de produto não suportado para atualização.");
            return;
        }

        dao.atualizar(indice, novo);
        System.out.println("Produto atualizado com sucesso!");
    }

    // ---------------------------------------------------------------- DELETE

    private static void deletar(Scanner scanner) {
        List<Produto> produtos = dao.listar();

        if (produtos.isEmpty()) {
            System.out.println("\nNenhum produto cadastrado.");
            return;
        }

        listar();
        System.out.println();
        int indice = Entrada.indice(scanner, "Número do produto a deletar", produtos.size());
        if (indice < 0) {
            return;
        }

        Produto produto = dao.buscar(indice);
        if (Entrada.confirmar(scanner, "Deseja realmente deletar \"" + produto.getNome() + "\"?")) {
            dao.deletar(indice);
            System.out.println("Produto deletado com sucesso!");
        } else {
            System.out.println("Operação cancelada.");
        }
    }

    // --------------------------------------------------------------- AUXILIARES

    private static String tipoDe(Produto p) {
        if (p instanceof Moveis) {
            return "Móveis";
        }
        if (p instanceof Eletrodomestrico) {
            return "Eletrodoméstico";
        }
        return "Produto";
    }

    private static void exibirResumo(int indice, Produto p) {
        System.out.println("[" + (indice + 1) + "] " + p.getNome()
                + " | " + tipoDe(p)
                + " | Marca: " + p.getMarca()
                + " | Modelo: " + p.getModelo()
                + " | Qtd: " + p.getQuantidade());
    }

    private static void exibirDetalhes(int indice, Produto p) {
        System.out.println("\n[" + (indice + 1) + "] " + p.getNome() + " (" + tipoDe(p) + ")");
        Entrada.linha("Marca", p.getMarca());
        Entrada.linha("Modelo", p.getModelo());
        Entrada.linha("Quantidade", p.getQuantidade());
        Entrada.linha("Descrição", p.getDescricacao());

        if (p instanceof Moveis m) {
            Entrada.linha("Composição", m.getComposicao());
            Entrada.linha("Materiais", m.getMateriais());
            Entrada.linha("Qtd de itens", m.getQtdItens());
        } else if (p instanceof Eletrodomestrico e) {
            Entrada.linha("Voltagem", e.getVoltagem() + " V");
            Entrada.linha("Potência", e.getPotencial() + " W");
            Entrada.linha("Especificações", e.getEspecificacoes());
        }

        Entrada.linha("Cadastrado em", Entrada.formatarData(p.getDataCriacao()) + " por " + p.getCriadoPor());
    }
}
