package br.ufms.pokecalcrb.util;

/**
 * Nomes da Gen 1 no formato da PokéAPI, usados SÓ no autocompletar (usabilidade).
 * Os dados (stats, tipos, poder) sempre vêm da API ou do cache.
 * Gerado a partir de /pokemon?limit=151 e /move?limit=165 (os golpes 1 a 165 são os da Gen 1).
 */
public final class NomesGen1 {

    private NomesGen1() { }

    public static final String[] POKEMON = {
            "bulbasaur", "ivysaur", "venusaur", "charmander", "charmeleon", "charizard", "squirtle", "wartortle",
            "blastoise", "caterpie", "metapod", "butterfree", "weedle", "kakuna", "beedrill", "pidgey",
            "pidgeotto", "pidgeot", "rattata", "raticate", "spearow", "fearow", "ekans", "arbok",
            "pikachu", "raichu", "sandshrew", "sandslash", "nidoran-f", "nidorina", "nidoqueen", "nidoran-m",
            "nidorino", "nidoking", "clefairy", "clefable", "vulpix", "ninetales", "jigglypuff", "wigglytuff",
            "zubat", "golbat", "oddish", "gloom", "vileplume", "paras", "parasect", "venonat",
            "venomoth", "diglett", "dugtrio", "meowth", "persian", "psyduck", "golduck", "mankey",
            "primeape", "growlithe", "arcanine", "poliwag", "poliwhirl", "poliwrath", "abra", "kadabra",
            "alakazam", "machop", "machoke", "machamp", "bellsprout", "weepinbell", "victreebel", "tentacool",
            "tentacruel", "geodude", "graveler", "golem", "ponyta", "rapidash", "slowpoke", "slowbro",
            "magnemite", "magneton", "farfetchd", "doduo", "dodrio", "seel", "dewgong", "grimer",
            "muk", "shellder", "cloyster", "gastly", "haunter", "gengar", "onix", "drowzee",
            "hypno", "krabby", "kingler", "voltorb", "electrode", "exeggcute", "exeggutor", "cubone",
            "marowak", "hitmonlee", "hitmonchan", "lickitung", "koffing", "weezing", "rhyhorn", "rhydon",
            "chansey", "tangela", "kangaskhan", "horsea", "seadra", "goldeen", "seaking", "staryu",
            "starmie", "mr-mime", "scyther", "jynx", "electabuzz", "magmar", "pinsir", "tauros",
            "magikarp", "gyarados", "lapras", "ditto", "eevee", "vaporeon", "jolteon", "flareon",
            "porygon", "omanyte", "omastar", "kabuto", "kabutops", "aerodactyl", "snorlax", "articuno",
            "zapdos", "moltres", "dratini", "dragonair", "dragonite", "mewtwo", "mew",
    };

    public static final String[] GOLPES = {
            "pound", "karate-chop", "double-slap", "comet-punch", "mega-punch", "pay-day", "fire-punch", "ice-punch",
            "thunder-punch", "scratch", "vice-grip", "guillotine", "razor-wind", "swords-dance", "cut", "gust",
            "wing-attack", "whirlwind", "fly", "bind", "slam", "vine-whip", "stomp", "double-kick",
            "mega-kick", "jump-kick", "rolling-kick", "sand-attack", "headbutt", "horn-attack", "fury-attack", "horn-drill",
            "tackle", "body-slam", "wrap", "take-down", "thrash", "double-edge", "tail-whip", "poison-sting",
            "twineedle", "pin-missile", "leer", "bite", "growl", "roar", "sing", "supersonic",
            "sonic-boom", "disable", "acid", "ember", "flamethrower", "mist", "water-gun", "hydro-pump",
            "surf", "ice-beam", "blizzard", "psybeam", "bubble-beam", "aurora-beam", "hyper-beam", "peck",
            "drill-peck", "submission", "low-kick", "counter", "seismic-toss", "strength", "absorb", "mega-drain",
            "leech-seed", "growth", "razor-leaf", "solar-beam", "poison-powder", "stun-spore", "sleep-powder", "petal-dance",
            "string-shot", "dragon-rage", "fire-spin", "thunder-shock", "thunderbolt", "thunder-wave", "thunder", "rock-throw",
            "earthquake", "fissure", "dig", "toxic", "confusion", "psychic", "hypnosis", "meditate",
            "agility", "quick-attack", "rage", "teleport", "night-shade", "mimic", "screech", "double-team",
            "recover", "harden", "minimize", "smokescreen", "confuse-ray", "withdraw", "defense-curl", "barrier",
            "light-screen", "haze", "reflect", "focus-energy", "bide", "metronome", "mirror-move", "self-destruct",
            "egg-bomb", "lick", "smog", "sludge", "bone-club", "fire-blast", "waterfall", "clamp",
            "swift", "skull-bash", "spike-cannon", "constrict", "amnesia", "kinesis", "soft-boiled", "high-jump-kick",
            "glare", "dream-eater", "poison-gas", "barrage", "leech-life", "lovely-kiss", "sky-attack", "transform",
            "bubble", "dizzy-punch", "spore", "flash", "psywave", "splash", "acid-armor", "crabhammer",
            "explosion", "fury-swipes", "bonemerang", "rest", "rock-slide", "hyper-fang", "sharpen", "conversion",
            "tri-attack", "super-fang", "slash", "substitute", "struggle",
    };
}
