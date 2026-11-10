package br.ufms.pokecalcrb.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long inserir(User user);

    @Update
    void atualizar(User user);

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    User buscarPorId(long id);

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    LiveData<User> observarPorId(long id);

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    User buscarPorEmail(String email);
}
