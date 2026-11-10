package br.ufms.pokecalcrb.ui.historico;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import br.ufms.pokecalcrb.data.db.Calculo;
import br.ufms.pokecalcrb.data.repo.CalculoRepository;
import br.ufms.pokecalcrb.data.repo.SessionManager;
import br.ufms.pokecalcrb.util.Executores;

public class HistoricoViewModel extends AndroidViewModel {

    private final CalculoRepository repo;
    private final long userId;
    private final LiveData<List<Calculo>> itens;

    public HistoricoViewModel(@NonNull Application app) {
        super(app);
        repo = new CalculoRepository(app);
        userId = new SessionManager(app).getUserId();
        // Só os cálculos do usuário logado, do mais recente ao mais antigo.
        itens = repo.observarDoUsuario(userId);
    }

    public LiveData<List<Calculo>> getItens() { return itens; }

    public void apagar(Calculo c) {
        Executores.io().execute(() -> repo.apagar(c.id, userId));
    }
}
