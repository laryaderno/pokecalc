package br.ufms.pokecalcrb.data.repo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Controle de sessão. Guarda SOMENTE o id do usuário logado (nunca a senha).
 * A sessão continua ativa mesmo fechando o app, até o logout.
 */
public class SessionManager {

    private static final String ARQUIVO = "sessao";
    private static final String CHAVE_USER_ID = "userId";
    public static final long SEM_SESSAO = -1L;

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
    }

    public void iniciar(long userId) {
        prefs.edit().putLong(CHAVE_USER_ID, userId).apply();
    }

    public long getUserId() {
        return prefs.getLong(CHAVE_USER_ID, SEM_SESSAO);
    }

    public boolean estaLogado() {
        return getUserId() != SEM_SESSAO;
    }

    public void encerrar() {
        prefs.edit().remove(CHAVE_USER_ID).apply();
    }
}
