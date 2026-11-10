package br.ufms.pokecalcrb.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.ufms.pokecalcrb.data.db.User;
import br.ufms.pokecalcrb.data.repo.SessionManager;
import br.ufms.pokecalcrb.data.repo.UserRepository;
import br.ufms.pokecalcrb.util.Executores;

/**
 * Usuário logado, observado pelo banco (Room + LiveData).
 * Compartilhado entre a MainActivity e os fragmentos.
 */
public class SessaoViewModel extends AndroidViewModel {

    private final LiveData<User> usuario;
    private final MutableLiveData<Boolean> sessaoInvalida = new MutableLiveData<>(false);
    private volatile boolean verificado = false;

    public SessaoViewModel(@NonNull Application app) {
        super(app);
        SessionManager sessao = new SessionManager(app);
        UserRepository repo = new UserRepository(app);
        long id = sessao.getUserId();
        usuario = repo.observar(id);

        // Verificação única: a sessão aponta para um usuário que existe?
        Executores.io().execute(() -> {
            User u = repo.buscar(id);
            verificado = true;
            if (u == null) sessaoInvalida.postValue(true);
        });
    }

    public LiveData<User> getUsuario() { return usuario; }
    public LiveData<Boolean> getSessaoInvalida() { return sessaoInvalida; }
    public boolean verificacaoConcluida() { return verificado; }
}
