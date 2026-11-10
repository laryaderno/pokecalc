package br.ufms.pokecalcrb.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CalculoDao {

    @Insert
    long inserir(Calculo calculo);

    /** Histórico do usuário logado, do mais recente para o mais antigo. */
    @Query("SELECT * FROM calculos WHERE userId = :userId ORDER BY data DESC, id DESC")
    LiveData<List<Calculo>> observarDoUsuario(long userId);

    /** Só apaga se o cálculo for do próprio usuário. */
    @Query("DELETE FROM calculos WHERE id = :id AND userId = :userId")
    int apagar(long id, long userId);
}
