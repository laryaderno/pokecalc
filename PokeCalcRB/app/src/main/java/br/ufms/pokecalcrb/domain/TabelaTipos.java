package br.ufms.pokecalcrb.domain;

import java.util.Arrays;

/**
 * Tabela de efetividade da Geração 1, embutida no app (não vem da API).
 * Os valores estão em "décimos" para evitar ponto flutuante:
 * 0 = não afeta, 5 = 0,5x, 10 = 1x, 20 = 2x.
 *
 * Peculiaridades da Gen 1 mantidas de propósito:
 *  - Fantasma NÃO afeta Psíquico (bug do jogo original);
 *  - Inseto x Venenoso = 2x e Venenoso x Inseto = 2x;
 *  - Gelo x Fogo = 1x (só virou 0,5x na Gen 2).
 */
public final class TabelaTipos {

    private static final int[][] TABELA = new int[Tipo.values().length][Tipo.values().length];

    static {
        for (int[] linha : TABELA) Arrays.fill(linha, 10);

        def(Tipo.NORMAL, Tipo.PEDRA, 5);
        def(Tipo.NORMAL, Tipo.FANTASMA, 0);

        def(Tipo.LUTADOR, Tipo.NORMAL, 20);
        def(Tipo.LUTADOR, Tipo.VOADOR, 5);
        def(Tipo.LUTADOR, Tipo.VENENOSO, 5);
        def(Tipo.LUTADOR, Tipo.PEDRA, 20);
        def(Tipo.LUTADOR, Tipo.INSETO, 5);
        def(Tipo.LUTADOR, Tipo.FANTASMA, 0);
        def(Tipo.LUTADOR, Tipo.PSIQUICO, 5);
        def(Tipo.LUTADOR, Tipo.GELO, 20);

        def(Tipo.VOADOR, Tipo.LUTADOR, 20);
        def(Tipo.VOADOR, Tipo.PEDRA, 5);
        def(Tipo.VOADOR, Tipo.INSETO, 20);
        def(Tipo.VOADOR, Tipo.PLANTA, 20);
        def(Tipo.VOADOR, Tipo.ELETRICO, 5);

        def(Tipo.VENENOSO, Tipo.VENENOSO, 5);
        def(Tipo.VENENOSO, Tipo.TERRESTRE, 5);
        def(Tipo.VENENOSO, Tipo.PEDRA, 5);
        def(Tipo.VENENOSO, Tipo.INSETO, 20);
        def(Tipo.VENENOSO, Tipo.FANTASMA, 5);
        def(Tipo.VENENOSO, Tipo.PLANTA, 20);

        def(Tipo.TERRESTRE, Tipo.VOADOR, 0);
        def(Tipo.TERRESTRE, Tipo.VENENOSO, 20);
        def(Tipo.TERRESTRE, Tipo.PEDRA, 20);
        def(Tipo.TERRESTRE, Tipo.INSETO, 5);
        def(Tipo.TERRESTRE, Tipo.FOGO, 20);
        def(Tipo.TERRESTRE, Tipo.PLANTA, 5);
        def(Tipo.TERRESTRE, Tipo.ELETRICO, 20);

        def(Tipo.PEDRA, Tipo.LUTADOR, 5);
        def(Tipo.PEDRA, Tipo.VOADOR, 20);
        def(Tipo.PEDRA, Tipo.TERRESTRE, 5);
        def(Tipo.PEDRA, Tipo.INSETO, 20);
        def(Tipo.PEDRA, Tipo.FOGO, 20);
        def(Tipo.PEDRA, Tipo.GELO, 20);

        def(Tipo.INSETO, Tipo.LUTADOR, 5);
        def(Tipo.INSETO, Tipo.VOADOR, 5);
        def(Tipo.INSETO, Tipo.VENENOSO, 20);
        def(Tipo.INSETO, Tipo.FANTASMA, 5);
        def(Tipo.INSETO, Tipo.FOGO, 5);
        def(Tipo.INSETO, Tipo.PLANTA, 20);
        def(Tipo.INSETO, Tipo.PSIQUICO, 20);

        def(Tipo.FANTASMA, Tipo.NORMAL, 0);
        def(Tipo.FANTASMA, Tipo.FANTASMA, 20);
        def(Tipo.FANTASMA, Tipo.PSIQUICO, 0);

        def(Tipo.FOGO, Tipo.PEDRA, 5);
        def(Tipo.FOGO, Tipo.INSETO, 20);
        def(Tipo.FOGO, Tipo.FOGO, 5);
        def(Tipo.FOGO, Tipo.AGUA, 5);
        def(Tipo.FOGO, Tipo.PLANTA, 20);
        def(Tipo.FOGO, Tipo.GELO, 20);
        def(Tipo.FOGO, Tipo.DRAGAO, 5);

        def(Tipo.AGUA, Tipo.TERRESTRE, 20);
        def(Tipo.AGUA, Tipo.PEDRA, 20);
        def(Tipo.AGUA, Tipo.FOGO, 20);
        def(Tipo.AGUA, Tipo.AGUA, 5);
        def(Tipo.AGUA, Tipo.PLANTA, 5);
        def(Tipo.AGUA, Tipo.DRAGAO, 5);

        def(Tipo.PLANTA, Tipo.VOADOR, 5);
        def(Tipo.PLANTA, Tipo.VENENOSO, 5);
        def(Tipo.PLANTA, Tipo.TERRESTRE, 20);
        def(Tipo.PLANTA, Tipo.PEDRA, 20);
        def(Tipo.PLANTA, Tipo.INSETO, 5);
        def(Tipo.PLANTA, Tipo.FOGO, 5);
        def(Tipo.PLANTA, Tipo.AGUA, 20);
        def(Tipo.PLANTA, Tipo.PLANTA, 5);
        def(Tipo.PLANTA, Tipo.DRAGAO, 5);

        def(Tipo.ELETRICO, Tipo.VOADOR, 20);
        def(Tipo.ELETRICO, Tipo.TERRESTRE, 0);
        def(Tipo.ELETRICO, Tipo.AGUA, 20);
        def(Tipo.ELETRICO, Tipo.PLANTA, 5);
        def(Tipo.ELETRICO, Tipo.ELETRICO, 5);
        def(Tipo.ELETRICO, Tipo.DRAGAO, 5);

        def(Tipo.PSIQUICO, Tipo.LUTADOR, 20);
        def(Tipo.PSIQUICO, Tipo.VENENOSO, 20);
        def(Tipo.PSIQUICO, Tipo.PSIQUICO, 5);

        def(Tipo.GELO, Tipo.VOADOR, 20);
        def(Tipo.GELO, Tipo.TERRESTRE, 20);
        def(Tipo.GELO, Tipo.AGUA, 5);
        def(Tipo.GELO, Tipo.PLANTA, 20);
        def(Tipo.GELO, Tipo.GELO, 5);
        def(Tipo.GELO, Tipo.DRAGAO, 20);

        def(Tipo.DRAGAO, Tipo.DRAGAO, 20);
    }

    private TabelaTipos() { }

    private static void def(Tipo ataque, Tipo defesa, int decimos) {
        TABELA[ataque.ordinal()][defesa.ordinal()] = decimos;
    }

    /** Efetividade em décimos (0, 5, 10 ou 20) de um golpe contra UM tipo do defensor. */
    public static int efetividadeDecimos(Tipo golpe, Tipo defensor) {
        if (golpe == null || defensor == null) return 10;
        return TABELA[golpe.ordinal()][defensor.ordinal()];
    }
}
