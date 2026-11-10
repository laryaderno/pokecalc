package br.ufms.pokecalcrb.domain;

/** Golpe já convertido para os valores da Gen 1. */
public class Golpe {
    public final String nome;
    public final int poder;
    public final Tipo tipo;

    public Golpe(String nome, int poder, Tipo tipo) {
        this.nome = nome;
        this.poder = poder;
        this.tipo = tipo;
    }

    public String nomeExibicao() {
        return CalculadoraGen1.capitalizar(nome);
    }
}
