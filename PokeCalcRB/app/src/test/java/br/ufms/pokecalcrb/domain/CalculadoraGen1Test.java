package br.ufms.pokecalcrb.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Testes da fórmula (rodam no PC, sem emulador: botão direito > Run 'CalculadoraGen1Test'). */
public class CalculadoraGen1Test {

    // Exemplo do enunciado: nível 50, Atq 100, Def 100, poder 100, sem STAB, efetividade 1.
    @Test
    public void exemploDoEnunciado_normal_39a46() {
        assertEquals(39, CalculadoraGen1.calcularDano(50, 100, 100, 100, false, false, 10, 10, 217));
        assertEquals(46, CalculadoraGen1.calcularDano(50, 100, 100, 100, false, false, 10, 10, 255));
    }

    @Test
    public void exemploDoEnunciado_critico_73a86() {
        assertEquals(73, CalculadoraGen1.calcularDano(50, 100, 100, 100, true, false, 10, 10, 217));
        assertEquals(86, CalculadoraGen1.calcularDano(50, 100, 100, 100, true, false, 10, 10, 255));
    }

    @Test
    public void golpeSemEfeito_causaZero() {
        int e = TabelaTipos.efetividadeDecimos(Tipo.NORMAL, Tipo.FANTASMA);
        assertEquals(0, e);
        assertEquals(0, CalculadoraGen1.calcularDano(50, 100, 100, 100, false, false, e, 10, 255));
    }

    @Test
    public void stab_multiplicaPorUmEMeio() {
        // 46 sem STAB -> 46 + 23 = 69 com STAB (R = 255)
        assertEquals(69, CalculadoraGen1.calcularDano(50, 100, 100, 100, false, true, 10, 10, 255));
    }

    @Test
    public void superEfetivo_dobra() {
        assertEquals(92, CalculadoraGen1.calcularDano(50, 100, 100, 100, false, false, 20, 10, 255));
    }

    @Test
    public void regraDo255_divideAtaqueEDefesaPor4() {
        // 300/4 = 75 e 280/4 = 70 -> igual a calcular direto com 75 e 70
        int comRegra = CalculadoraGen1.calcularDano(100, 100, 300, 280, false, false, 10, 10, 255);
        int direto = CalculadoraGen1.calcularDano(100, 100, 75, 70, false, false, 10, 10, 255);
        assertEquals(direto, comRegra);
    }

    @Test
    public void stats_comDv15EStatExpMaximo() {
        // Base 100 no nível 100: (115*2+64)*100/100 + 5 = 299
        assertEquals(299, CalculadoraGen1.calcularStat(100, 100));
        // HP base 100 no nível 50: 294*50/100 + 50 + 10 = 207
        assertEquals(207, CalculadoraGen1.calcularHp(100, 50));
    }

    @Test
    public void tabelaTipos_peculiaridadesDaGen1() {
        assertEquals(0, TabelaTipos.efetividadeDecimos(Tipo.FANTASMA, Tipo.PSIQUICO));
        assertEquals(20, TabelaTipos.efetividadeDecimos(Tipo.INSETO, Tipo.VENENOSO));
        assertEquals(20, TabelaTipos.efetividadeDecimos(Tipo.VENENOSO, Tipo.INSETO));
        assertEquals(10, TabelaTipos.efetividadeDecimos(Tipo.GELO, Tipo.FOGO));
        assertEquals(0, TabelaTipos.efetividadeDecimos(Tipo.TERRESTRE, Tipo.VOADOR));
    }

    @Test
    public void calculoCompleto_charizardContraVenusaur() {
        // Stats base Gen 1: Charizard 78/84/78/85/100 (Fogo/Voador), Venusaur 80/82/83/100/80 (Planta/Venenoso)
        Pokemon charizard = new Pokemon(6, "charizard", 78, 84, 78, 85, 100, Tipo.FOGO, Tipo.VOADOR, null);
        Pokemon venusaur = new Pokemon(3, "venusaur", 80, 82, 83, 100, 80, Tipo.PLANTA, Tipo.VENENOSO, null);
        Golpe flamethrower = new Golpe("flamethrower", 95, Tipo.FOGO);

        ResultadoDano r = CalculadoraGen1.calcular(charizard, 50, venusaur, 50, flamethrower);
        assertFalse(r.golpeFisico);   // Fogo é especial na Gen 1
        assertTrue(r.stab);
        assertEquals(200, r.efetividadeCentesimos);
        assertEquals("Super efetivo!", CalculadoraGen1.textoEfetividade(r.efetividadeCentesimos));
        // Special 85 nv 50 = 264*50/100+5 = 137; Special 100 nv 50 = 294*50/100+5 = 152
        // HP 80 nv 50 = 254*50/100 + 50 + 10 = 187
        assertEquals(137, r.ataqueUsado);
        assertEquals(152, r.defesaUsada);
        assertEquals(187, r.hpDefensor);
        assertTrue(r.danoMin <= r.danoMax);
        assertTrue(r.critMin > r.danoMax);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nivelZero_ehInvalido() {
        CalculadoraGen1.validarNivel(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nivel101_ehInvalido() {
        CalculadoraGen1.validarNivel(101);
    }

    @Test
    public void textoNocaute() {
        assertEquals("Nocauteia em 2 a 3 golpes", CalculadoraGen1.textoNocaute(100, 39, 60));
        assertEquals("Nocauteia em 1 golpe (OHKO)", CalculadoraGen1.textoNocaute(50, 60, 70));
        assertEquals("Não causa dano", CalculadoraGen1.textoNocaute(50, 0, 0));
    }
}
