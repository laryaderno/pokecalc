package br.ufms.pokecalcrb.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Um cálculo salvo no histórico. Pertence a um usuário (1 usuário : N cálculos). */
@Entity(tableName = "calculos",
        foreignKeys = @ForeignKey(entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("userId")})
public class Calculo {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;

    @NonNull public String atacante = "";
    @NonNull public String defensor = "";
    @NonNull public String golpe = "";

    public int nivelAtq;
    public int nivelDef;

    public int danoMin;
    public int danoMax;
    public int critMin;
    public int critMax;

    public int hpDefensor;
    public String efetividade;

    /** Momento do cálculo (System.currentTimeMillis()). */
    public long data;
}
