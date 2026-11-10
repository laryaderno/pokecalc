package br.ufms.pokecalcrb.util;

/**
 * Envolve um valor do LiveData que deve ser tratado uma única vez
 * (ex.: mostrar uma mensagem ou navegar), mesmo após girar a tela.
 */
public class Evento<T> {
    private final T conteudo;
    private boolean tratado = false;

    public Evento(T conteudo) {
        this.conteudo = conteudo;
    }

    /** Devolve o conteúdo só na primeira vez; depois devolve null. */
    public T pegarSeNaoTratado() {
        if (tratado) return null;
        tratado = true;
        return conteudo;
    }
}
