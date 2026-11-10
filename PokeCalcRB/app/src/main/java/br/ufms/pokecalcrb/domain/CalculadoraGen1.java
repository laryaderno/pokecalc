package br.ufms.pokecalcrb.domain;

import java.util.Locale;

/**
 * Fórmula de dano da Geração 1, em Java puro (sem nada de Android),
 * para poder ser testada com JUnit.
 *
 * Configuração fixa: DV 15 e Stat Exp máximo (Pokémon "treinado ao máximo").
 * Todas as divisões são inteiras (arredondam para baixo), como no jogo.
 */
public final class CalculadoraGen1 {

    public static final int NIVEL_MIN = 1;
    public static final int NIVEL_MAX = 100;
    public static final int DV = 15;
    /** floor(ceil(sqrt(65535)) / 4) = 64: bônus do Stat Exp máximo. */
    public static final int BONUS_STAT_EXP = 64;
    public static final int R_MIN = 217;
    public static final int R_MAX = 255;

    private CalculadoraGen1() { }

    /** Stat = ((Base + 15) x 2 + 64) x Nível / 100 + 5 */
    public static int calcularStat(int base, int nivel) {
        return ((base + DV) * 2 + BONUS_STAT_EXP) * nivel / 100 + 5;
    }

    /** HP = ((Base + 15) x 2 + 64) x Nível / 100 + Nível + 10 */
    public static int calcularHp(int base, int nivel) {
        return ((base + DV) * 2 + BONUS_STAT_EXP) * nivel / 100 + nivel + 10;
    }

    /**
     * Dano de um único golpe, com os stats JÁ calculados.
     * Dano = (((2 x Nível x Crit / 5 + 2) x Poder x Atq / Def) / 50 + 2)
     *        x STAB x Tipo1 x Tipo2 x R / 255
     *
     * @param efetTipo1Decimos efetividade contra o tipo 1 do defensor (0, 5, 10 ou 20)
     * @param efetTipo2Decimos efetividade contra o tipo 2 do defensor (10 se não tiver tipo 2)
     * @param r fator aleatório, de 217 a 255
     */
    public static int calcularDano(int nivel, int poder, int ataque, int defesa,
                                   boolean critico, boolean stab,
                                   int efetTipo1Decimos, int efetTipo2Decimos, int r) {
        if (poder <= 0) return 0;
        if (efetTipo1Decimos == 0 || efetTipo2Decimos == 0) return 0; // "Não afeta..."

        // Regra do 255: se Atq ou Def passar de 255, divide os dois por 4.
        if (ataque > 255 || defesa > 255) {
            ataque = ataque / 4;
            defesa = defesa / 4;
        }
        if (defesa < 1) defesa = 1; // evita divisão por zero

        int crit = critico ? 2 : 1;
        long dano = (2L * nivel * crit) / 5 + 2;
        dano = dano * poder * ataque / defesa;
        dano = dano / 50;
        dano = Math.min(dano, 997) + 2; // limite do jogo original antes do +2

        if (stab) dano = dano + dano / 2;      // x1,5 arredondando para baixo
        dano = dano * efetTipo1Decimos / 10;   // x tipo 1
        dano = dano * efetTipo2Decimos / 10;   // x tipo 2
        dano = dano * r / 255;                 // fator aleatório (217 a 255)

        // Se o golpe afeta o alvo, o mínimo é 1 de dano.
        return (int) Math.max(1, dano);
    }

    /** Calcula todas as saídas da tela a partir dos dois Pokémon, níveis e golpe. */
    public static ResultadoDano calcular(Pokemon atacante, int nivelAtq,
                                         Pokemon defensor, int nivelDef, Golpe golpe) {
        validarNivel(nivelAtq);
        validarNivel(nivelDef);

        ResultadoDano r = new ResultadoDano();
        r.golpeFisico = golpe.tipo.fisico;
        // Físico: Ataque x Defesa. Especial: Special x Special.
        r.ataqueUsado = r.golpeFisico
                ? calcularStat(atacante.ataque, nivelAtq)
                : calcularStat(atacante.special, nivelAtq);
        r.defesaUsada = r.golpeFisico
                ? calcularStat(defensor.defesa, nivelDef)
                : calcularStat(defensor.special, nivelDef);
        r.hpDefensor = calcularHp(defensor.hp, nivelDef);
        r.stab = atacante.temTipo(golpe.tipo);

        int e1 = TabelaTipos.efetividadeDecimos(golpe.tipo, defensor.tipo1);
        int e2 = defensor.tipo2 == null ? 10 : TabelaTipos.efetividadeDecimos(golpe.tipo, defensor.tipo2);
        r.efetividadeCentesimos = e1 * e2;

        r.danoMin = calcularDano(nivelAtq, golpe.poder, r.ataqueUsado, r.defesaUsada, false, r.stab, e1, e2, R_MIN);
        r.danoMax = calcularDano(nivelAtq, golpe.poder, r.ataqueUsado, r.defesaUsada, false, r.stab, e1, e2, R_MAX);
        r.critMin = calcularDano(nivelAtq, golpe.poder, r.ataqueUsado, r.defesaUsada, true, r.stab, e1, e2, R_MIN);
        r.critMax = calcularDano(nivelAtq, golpe.poder, r.ataqueUsado, r.defesaUsada, true, r.stab, e1, e2, R_MAX);

        r.chanceCritico = chanceCritico(atacante.speed);
        return r;
    }

    /** Chance de crítico da Gen 1 em %: (Speed base / 2) / 256. */
    public static double chanceCritico(int speedBase) {
        return (speedBase / 2) * 100.0 / 256.0;
    }

    public static boolean nivelValido(int nivel) {
        return nivel >= NIVEL_MIN && nivel <= NIVEL_MAX;
    }

    public static void validarNivel(int nivel) {
        if (!nivelValido(nivel)) {
            throw new IllegalArgumentException("Nível deve estar entre 1 e 100");
        }
    }

    /** Texto de efetividade exibido na tela. */
    public static String textoEfetividade(int efetividadeCentesimos) {
        if (efetividadeCentesimos == 0) return "Não afeta o defensor...";
        if (efetividadeCentesimos >= 400) return "Super efetivo! (4x)";
        if (efetividadeCentesimos > 100) return "Super efetivo!";
        if (efetividadeCentesimos <= 25) return "Não é muito efetivo... (0,25x)";
        if (efetividadeCentesimos < 100) return "Não é muito efetivo...";
        return "Efetividade normal (1x)";
    }

    /** "Nocauteia em X golpes" usando o dano mínimo e máximo. */
    public static String textoNocaute(int hp, int danoMin, int danoMax) {
        if (danoMax <= 0) return "Não causa dano";
        int melhor = (hp + danoMax - 1) / danoMax;               // arredonda para cima
        int pior = (hp + danoMin - 1) / Math.max(1, danoMin);
        if (melhor == 1 && pior == 1) return "Nocauteia em 1 golpe (OHKO)";
        if (melhor == pior) return "Nocauteia em " + melhor + " golpes";
        return "Nocauteia em " + melhor + " a " + pior + " golpes";
    }

    public static String capitalizar(String nome) {
        if (nome == null || nome.isEmpty()) return "";
        String s = nome.replace('-', ' ');
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}
