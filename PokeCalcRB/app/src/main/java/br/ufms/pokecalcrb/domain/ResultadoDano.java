package br.ufms.pokecalcrb.domain;

/** Tudo o que a tela de resultado precisa mostrar. */
public class ResultadoDano {
    public int danoMin, danoMax;
    public int critMin, critMax;
    public int hpDefensor;
    /** Efetividade total em centésimos: 0, 25, 50, 100, 200 ou 400. */
    public int efetividadeCentesimos;
    public boolean stab;
    public boolean golpeFisico;
    public int ataqueUsado, defesaUsada;
    /** Chance de crítico em porcentagem: (Speed base / 2) / 256. */
    public double chanceCritico;

    public double porcentagemMin() { return hpDefensor == 0 ? 0 : danoMin * 100.0 / hpDefensor; }
    public double porcentagemMax() { return hpDefensor == 0 ? 0 : danoMax * 100.0 / hpDefensor; }
    public double porcentagemCritMin() { return hpDefensor == 0 ? 0 : critMin * 100.0 / hpDefensor; }
    public double porcentagemCritMax() { return hpDefensor == 0 ? 0 : critMax * 100.0 / hpDefensor; }
}
