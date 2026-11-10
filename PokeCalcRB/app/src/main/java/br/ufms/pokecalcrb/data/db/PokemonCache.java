package br.ufms.pokecalcrb.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Cache dos Pokémon já buscados na PokéAPI (economia de energia e rede).
 * Se o Pokémon já está aqui, o app não faz nova requisição.
 */
@Entity(tableName = "pokemon_cache", indices = {@Index(value = "nome", unique = true)})
public class PokemonCache {

    @PrimaryKey
    public int numero;

    @NonNull public String nome = "";

    public int hp;
    public int atk;
    public int def;
    public int special;
    public int speed;

    /** Tipos da Gen 1 separados por vírgula, no formato da API. Ex.: "fire,flying". */
    @NonNull public String tipos = "";

    public String spriteUrl;
}
