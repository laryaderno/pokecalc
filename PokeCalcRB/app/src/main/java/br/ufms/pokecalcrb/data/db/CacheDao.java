package br.ufms.pokecalcrb.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface CacheDao {

    @Query("SELECT * FROM pokemon_cache WHERE numero = :numero LIMIT 1")
    PokemonCache pokemonPorNumero(int numero);

    @Query("SELECT * FROM pokemon_cache WHERE nome = :nome LIMIT 1")
    PokemonCache pokemonPorNome(String nome);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarPokemon(PokemonCache p);

    @Query("SELECT * FROM golpe_cache WHERE nome = :nome LIMIT 1")
    GolpeCache golpePorNome(String nome);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarGolpe(GolpeCache g);
}
