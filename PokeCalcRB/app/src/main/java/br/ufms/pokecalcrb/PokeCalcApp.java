package br.ufms.pokecalcrb;

import android.app.Application;

import br.ufms.pokecalcrb.util.Notificacoes;

public class PokeCalcApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // O canal precisa existir antes de qualquer notificação (Android 8+).
        Notificacoes.criarCanal(this);
    }
}
