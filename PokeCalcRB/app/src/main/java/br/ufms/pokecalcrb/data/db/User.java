package br.ufms.pokecalcrb.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Tabela de usuários. A senha NUNCA é guardada: só o hash BCrypt (com salt). */
@Entity(tableName = "usuarios", indices = {@Index(value = "email", unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String nome = "";

    @NonNull
    public String email = "";

    /** Hash BCrypt no formato $2a$10$... (o salt fica dentro do próprio hash). */
    @NonNull
    public String senhaHash = "";

    /** Caminho do arquivo da foto no armazenamento interno do app (pode ser null). */
    public String fotoPath;
}
