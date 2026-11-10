package br.ufms.pokecalcrb.data.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import br.ufms.pokecalcrb.data.db.AppDatabase;
import br.ufms.pokecalcrb.data.db.Calculo;
import br.ufms.pokecalcrb.data.db.CalculoDao;

/** Histórico de cálculos do usuário. */
public class CalculoRepository {

    private final CalculoDao dao;

    public CalculoRepository(Context context) {
        dao = AppDatabase.get(context).calculoDao();
    }

    public LiveData<List<Calculo>> observarDoUsuario(long userId) {
        return dao.observarDoUsuario(userId);
    }

    /** Chamar fora da thread principal. */
    public long salvar(Calculo c) {
        return dao.inserir(c);
    }

    /** Chamar fora da thread principal. */
    public boolean apagar(long id, long userId) {
        return dao.apagar(id, userId) > 0;
    }
}
