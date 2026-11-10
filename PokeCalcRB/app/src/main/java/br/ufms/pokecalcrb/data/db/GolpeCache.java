package br.ufms.pokecalcrb.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Cache dos golpes já buscados na PokéAPI (valores da Gen 1). */
@Entity(tableName = "golpe_cache")
public class GolpeCache {

    @PrimaryKey
    @NonNull
    public String nome = "";

    public int poder;

    /** Tipo da Gen 1 no formato da API. Ex.: "fire". */
    @NonNull public String tipo = "";
}
