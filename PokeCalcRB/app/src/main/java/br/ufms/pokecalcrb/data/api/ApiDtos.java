package br.ufms.pokecalcrb.data.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** Classes que espelham o JSON da PokéAPI (só os campos que o app usa). */
public final class ApiDtos {

    private ApiDtos() { }

    /** Referência com nome e URL, ex.: {"name": "fire", "url": ".../type/10/"}. */
    public static class Ref {
        public String name;
        public String url;

        /** Extrai o número do fim da URL (".../version-group/3/" -> 3). */
        public int idDaUrl() {
            if (url == null) return Integer.MAX_VALUE;
            String s = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
            try {
                return Integer.parseInt(s.substring(s.lastIndexOf('/') + 1));
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        }
    }

    // ---------- GET /pokemon/{nome ou id} ----------

    public static class PokemonResponse {
        public int id;
        public String name;
        public List<StatSlot> stats;
        public List<TypeSlot> types;
        @SerializedName("past_types") public List<PastTypes> pastTypes;
        @SerializedName("past_stats") public List<PastStats> pastStats;
        public Sprites sprites;
    }

    public static class StatSlot {
        @SerializedName("base_stat") public int baseStat;
        public Ref stat;
    }

    public static class TypeSlot {
        public int slot;
        public Ref type;
    }

    /** Tipos que o Pokémon tinha ATÉ a geração indicada (ex.: Clefairy era Normal até a Gen 5). */
    public static class PastTypes {
        public Ref generation;
        public List<TypeSlot> types;
    }

    /** Stats que o Pokémon tinha ATÉ a geração indicada (é daqui que vem o "special" da Gen 1). */
    public static class PastStats {
        public Ref generation;
        public List<StatSlot> stats;
    }

    public static class Sprites {
        @SerializedName("front_default") public String frontDefault;
        public Versions versions;
    }

    public static class Versions {
        @SerializedName("generation-i") public GenerationI generationI;
    }

    public static class GenerationI {
        @SerializedName("red-blue") public RedBlue redBlue;
    }

    public static class RedBlue {
        @SerializedName("front_transparent") public String frontTransparent;
        @SerializedName("front_default") public String frontDefault;
    }

    // ---------- GET /move/{nome} ----------

    public static class MoveResponse {
        public int id;
        public String name;
        public Integer power;
        public Ref type;
        public Ref generation;
        @SerializedName("past_values") public List<PastValue> pastValues;
    }

    /** Valores que o golpe tinha ANTES do version group indicado (ex.: Bite era Normal antes de Gold/Silver). */
    public static class PastValue {
        public Integer power;
        public Ref type;
        @SerializedName("version_group") public Ref versionGroup;
    }
}
