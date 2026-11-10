package br.ufms.pokecalcrb.domain;

/** Pokémon com stats BASE da Gen 1 (HP, Ataque, Defesa, Special, Speed). */
public class Pokemon {
    public final int numero;
    public final String nome;
    public final int hp, ataque, defesa, special, speed;
    public final Tipo tipo1;
    /** Pode ser null quando o Pokémon tem um tipo só. */
    public final Tipo tipo2;
    public final String spriteUrl;

    public Pokemon(int numero, String nome, int hp, int ataque, int defesa, int special, int speed,
                   Tipo tipo1, Tipo tipo2, String spriteUrl) {
        this.numero = numero;
        this.nome = nome;
        this.hp = hp;
        this.ataque = ataque;
        this.defesa = defesa;
        this.special = special;
        this.speed = speed;
        this.tipo1 = tipo1;
        this.tipo2 = tipo2;
        this.spriteUrl = spriteUrl;
    }

    public boolean temTipo(Tipo t) {
        return t != null && (t == tipo1 || t == tipo2);
    }

    /** Nome com a primeira letra maiúscula, para exibição. */
    public String nomeExibicao() {
        return CalculadoraGen1.capitalizar(nome);
    }

    public String tiposExibicao() {
        return tipo2 == null ? tipo1.nomePt : tipo1.nomePt + " / " + tipo2.nomePt;
    }
}
