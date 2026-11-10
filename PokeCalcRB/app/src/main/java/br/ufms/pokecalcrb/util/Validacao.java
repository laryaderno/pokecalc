package br.ufms.pokecalcrb.util;

import java.util.Locale;
import java.util.regex.Pattern;

/** Validações de formulário (Java puro). Retornam null quando está tudo certo. */
public final class Validacao {

    public static final int SENHA_MIN = 6;
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validacao() { }

    public static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public static String erroNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "Informe o seu nome";
        if (nome.trim().length() < 2) return "O nome deve ter pelo menos 2 letras";
        return null;
    }

    public static String erroEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "Informe o e-mail";
        if (!EMAIL.matcher(email.trim()).matches()) return "E-mail inválido (ex.: ash@pallet.com)";
        return null;
    }

    public static String erroSenha(String senha) {
        if (senha == null || senha.isEmpty()) return "Informe a senha";
        if (senha.length() < SENHA_MIN) return "A senha deve ter pelo menos " + SENHA_MIN + " caracteres";
        return null;
    }

    public static String erroConfirmacao(String senha, String confirmacao) {
        if (confirmacao == null || confirmacao.isEmpty()) return "Confirme a senha";
        if (!confirmacao.equals(senha)) return "As senhas não conferem";
        return null;
    }

    /** Retorna o nível (1 a 100) ou null se for inválido. */
    public static Integer lerNivel(String texto) {
        if (texto == null) return null;
        try {
            int n = Integer.parseInt(texto.trim());
            return (n >= 1 && n <= 100) ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
