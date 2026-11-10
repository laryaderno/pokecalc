package br.ufms.pokecalcrb.data.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufms.pokecalcrb.domain.Golpe;
import br.ufms.pokecalcrb.domain.Pokemon;
import br.ufms.pokecalcrb.domain.Tipo;

/**
 * Converte a resposta atual da PokéAPI para os valores da Gen 1, usando os campos
 * past_types, past_stats e past_values. Tipos que não existiam (Aço, Sombrio, Fada)
 * são ignorados. Java puro: pode ser testado sem Android.
 */
public final class ConversorGen1 {

    public static final int ULTIMO_POKEMON_GEN1 = 151;

    private ConversorGen1() { }

    public static Pokemon paraPokemon(ApiDtos.PokemonResponse r) throws ErroAmigavel {
        if (r == null || r.stats == null || r.types == null) {
            throw new ErroAmigavel("Resposta inválida da PokéAPI.");
        }
        if (r.id < 1 || r.id > ULTIMO_POKEMON_GEN1) {
            throw new ErroAmigavel("Só são aceitos Pokémon da Gen 1 (#1 a #151).");
        }

        // 1) Stats atuais
        Map<String, Integer> stats = new HashMap<>();
        for (ApiDtos.StatSlot s : r.stats) {
            if (s.stat != null) stats.put(s.stat.name, s.baseStat);
        }
        // 2) Sobrescreve com os valores antigos. Para cada stat vale a entrada
        //    da geração MAIS ANTIGA que o contém (é o valor que valia na Gen 1).
        Map<String, Integer> genDoValor = new HashMap<>();
        if (r.pastStats != null) {
            for (ApiDtos.PastStats ps : r.pastStats) {
                int gen = ps.generation == null ? Integer.MAX_VALUE : ps.generation.idDaUrl();
                if (ps.stats == null) continue;
                for (ApiDtos.StatSlot s : ps.stats) {
                    if (s.stat == null) continue;
                    Integer atual = genDoValor.get(s.stat.name);
                    if (atual == null || gen < atual) {
                        genDoValor.put(s.stat.name, gen);
                        stats.put(s.stat.name, s.baseStat);
                    }
                }
            }
        }
        int hp = valor(stats, "hp");
        int atk = valor(stats, "attack");
        int def = valor(stats, "defense");
        int speed = valor(stats, "speed");
        // Na Gen 1 existia um único "Special". Se a API não trouxer, usa o Sp. Attack.
        int special = stats.containsKey("special") ? stats.get("special") : valor(stats, "special-attack");

        // 3) Tipos: a entrada de past_types da geração mais antiga, senão os atuais.
        List<ApiDtos.TypeSlot> slots = r.types;
        if (r.pastTypes != null && !r.pastTypes.isEmpty()) {
            ApiDtos.PastTypes maisAntigo = null;
            for (ApiDtos.PastTypes pt : r.pastTypes) {
                if (pt.types == null || pt.generation == null) continue;
                if (maisAntigo == null || pt.generation.idDaUrl() < maisAntigo.generation.idDaUrl()) {
                    maisAntigo = pt;
                }
            }
            if (maisAntigo != null) slots = maisAntigo.types;
        }
        List<Tipo> tipos = new ArrayList<>();
        for (ApiDtos.TypeSlot ts : ordenarPorSlot(slots)) {
            Tipo t = ts.type == null ? null : Tipo.deApi(ts.type.name);
            if (t != null && !tipos.contains(t)) tipos.add(t); // ignora tipos novos
        }
        if (tipos.isEmpty()) tipos.add(Tipo.NORMAL);

        return new Pokemon(r.id, r.name, hp, atk, def, special, speed,
                tipos.get(0), tipos.size() > 1 ? tipos.get(1) : null, sprite(r));
    }

    public static Golpe paraGolpe(ApiDtos.MoveResponse m) throws ErroAmigavel {
        if (m == null || m.type == null) throw new ErroAmigavel("Resposta inválida da PokéAPI.");
        if (m.generation == null || !"generation-i".equals(m.generation.name)) {
            throw new ErroAmigavel("O golpe \"" + m.name + "\" não existia na Gen 1.");
        }

        Integer poder = m.power;
        String tipo = m.type.name;
        // Para cada campo, vale o past_value do version group mais antigo que o define.
        int vgPoder = Integer.MAX_VALUE, vgTipo = Integer.MAX_VALUE;
        if (m.pastValues != null) {
            for (ApiDtos.PastValue pv : m.pastValues) {
                int vg = pv.versionGroup == null ? Integer.MAX_VALUE : pv.versionGroup.idDaUrl();
                if (pv.power != null && vg < vgPoder) { vgPoder = vg; poder = pv.power; }
                if (pv.type != null && vg < vgTipo) { vgTipo = vg; tipo = pv.type.name; }
            }
        }

        if (poder == null || poder <= 0) {
            throw new ErroAmigavel("\"" + m.name + "\" não causa dano pela fórmula (golpe de status ou de dano fixo).");
        }
        Tipo t = Tipo.deApi(tipo);
        if (t == null) throw new ErroAmigavel("O tipo do golpe \"" + m.name + "\" não existia na Gen 1.");
        return new Golpe(m.name, poder, t);
    }

    /**
     * Padroniza o que o usuário digitou para o formato da API.
     * "Mr. Mime" -> "mr-mime", "Farfetch'd" -> "farfetchd", "Nidoran♀" -> "nidoran-f", " 25 " -> "25".
     */
    public static String normalizarEntrada(String texto) {
        if (texto == null) return "";
        String s = texto.trim().toLowerCase(java.util.Locale.ROOT);
        s = s.replace("♀", "-f").replace("♂", "-m");
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.replace("'", "").replace("’", "").replace(".", " ");
        s = s.trim().replaceAll("[\\s_]+", "-");
        s = s.replaceAll("-+", "-");
        if (s.equals("nidoran-femea")) s = "nidoran-f";
        if (s.equals("nidoran-macho")) s = "nidoran-m";
        return s;
    }

    private static int valor(Map<String, Integer> stats, String nome) {
        Integer v = stats.get(nome);
        return v == null ? 0 : v;
    }

    private static List<ApiDtos.TypeSlot> ordenarPorSlot(List<ApiDtos.TypeSlot> slots) {
        List<ApiDtos.TypeSlot> copia = new ArrayList<>(slots);
        copia.sort((a, b) -> Integer.compare(a.slot, b.slot));
        return copia;
    }

    private static String sprite(ApiDtos.PokemonResponse r) {
        if (r.sprites == null) return null;
        if (r.sprites.versions != null && r.sprites.versions.generationI != null
                && r.sprites.versions.generationI.redBlue != null) {
            ApiDtos.RedBlue rb = r.sprites.versions.generationI.redBlue;
            if (rb.frontTransparent != null) return rb.frontTransparent;
            if (rb.frontDefault != null) return rb.frontDefault;
        }
        return r.sprites.frontDefault;
    }
}
