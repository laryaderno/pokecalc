package br.ufms.pokecalcrb.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Calculo.class, PokemonCache.class, GolpeCache.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instancia;

    public abstract UserDao userDao();
    public abstract CalculoDao calculoDao();
    public abstract CacheDao cacheDao();

    public static AppDatabase get(Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "pokecalc.db").build();
                }
            }
        }
        return instancia;
    }
}
