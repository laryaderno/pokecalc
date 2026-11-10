package br.ufms.pokecalcrb.data.api;

/** Erro com mensagem pronta para mostrar ao usuário (sem crash). */
public class ErroAmigavel extends Exception {
    public ErroAmigavel(String mensagem) {
        super(mensagem);
    }
}
