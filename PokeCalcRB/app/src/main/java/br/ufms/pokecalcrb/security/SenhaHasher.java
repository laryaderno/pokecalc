package br.ufms.pokecalcrb.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Hash de senha com BCrypt. O salt é gerado aleatoriamente a cada hash e fica
 * embutido no próprio resultado ($2a$10$<salt><hash>). Não é reversível:
 * para validar o login, a senha digitada é comparada com o hash guardado.
 */
public final class SenhaHasher {

    /** Custo 10 = 2^10 rodadas. Bom equilíbrio entre segurança e tempo no celular. */
    private static final int CUSTO = 10;

    private SenhaHasher() { }

    public static String gerarHash(String senha) {
        return BCrypt.withDefaults().hashToString(CUSTO, senha.toCharArray());
    }

    public static boolean conferir(String senhaDigitada, String hashSalvo) {
        if (senhaDigitada == null || hashSalvo == null || hashSalvo.isEmpty()) return false;
        return BCrypt.verifyer().verify(senhaDigitada.toCharArray(), hashSalvo).verified;
    }
}
