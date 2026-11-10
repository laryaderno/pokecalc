package br.ufms.pokecalcrb.domain;

/**
 * Os 15 tipos que existiam na Geração 1 (Red/Blue).
 * Tipos criados depois (Aço, Sombrio, Fada) não existem aqui.
 */
public enum Tipo {
    NORMAL("normal", "Normal", true),
    LUTADOR("fighting", "Lutador", true),
    VOADOR("flying", "Voador", true),
    VENENOSO("poison", "Venenoso", true),
    TERRESTRE("ground", "Terrestre", true),
    PEDRA("rock", "Pedra", true),
    INSETO("bug", "Inseto", true),
    FANTASMA("ghost", "Fantasma", true),
    FOGO("fire", "Fogo", false),
    AGUA("water", "Água", false),
    PLANTA("grass", "Planta", false),
    ELETRICO("electric", "Elétrico", false),
    PSIQUICO("psychic", "Psíquico", false),
    GELO("ice", "Gelo", false),
    DRAGAO("dragon", "Dragão", false);

    /** Nome usado pela PokéAPI. */
    public final String nomeApi;
    /** Nome exibido para o usuário. */
    public final String nomePt;
    /** Na Gen 1 o TIPO do golpe define se ele é físico (true) ou especial (false). */
    public final boolean fisico;

    Tipo(String nomeApi, String nomePt, boolean fisico) {
        this.nomeApi = nomeApi;
        this.nomePt = nomePt;
        this.fisico = fisico;
    }

    /** Converte o nome da API para o enum. Retorna null para tipos que não existiam na Gen 1. */
    public static Tipo deApi(String nome) {
        if (nome == null) return null;
        for (Tipo t : values()) {
            if (t.nomeApi.equalsIgnoreCase(nome.trim())) return t;
        }
        return null;
    }
}
