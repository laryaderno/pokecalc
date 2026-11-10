package br.ufms.pokecalcrb.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Threads em segundo plano para banco, rede e BCrypt (nunca na thread principal). */
public final class Executores {

    private static final ExecutorService IO = Executors.newFixedThreadPool(3);

    private Executores() { }

    public static ExecutorService io() {
        return IO;
    }
}
