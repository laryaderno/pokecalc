package br.ufms.pokecalcrb.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.ufms.pokecalcrb.data.api.ErroAmigavel;
import br.ufms.pokecalcrb.data.db.User;
import br.ufms.pokecalcrb.data.repo.SessionManager;
import br.ufms.pokecalcrb.data.repo.UserRepository;
import br.ufms.pokecalcrb.util.Evento;
import br.ufms.pokecalcrb.util.Executores;

/** Login e cadastro. O BCrypt e o banco rodam em segundo plano. */
public class AuthViewModel extends AndroidViewModel {

    private final UserRepository repo;
    private final SessionManager sessao;

    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);
    private final MutableLiveData<Evento<String>> erro = new MutableLiveData<>();
    private final MutableLiveData<Evento<Boolean>> sucesso = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application app) {
        super(app);
        repo = new UserRepository(app);
        sessao = new SessionManager(app);
    }

    public LiveData<Boolean> getCarregando() { return carregando; }
    public LiveData<Evento<String>> getErro() { return erro; }
    public LiveData<Evento<Boolean>> getSucesso() { return sucesso; }

    public void entrar(String email, String senha) {
        carregando.setValue(true);
        Executores.io().execute(() -> {
            try {
                User u = repo.login(email, senha);
                sessao.iniciar(u.id);
                sucesso.postValue(new Evento<>(true));
            } catch (ErroAmigavel e) {
                erro.postValue(new Evento<>(e.getMessage()));
            } finally {
                carregando.postValue(false);
            }
        });
    }

    /** Cadastra e já deixa o usuário logado. */
    public void cadastrar(String nome, String email, String senha, String confirmacao) {
        carregando.setValue(true);
        Executores.io().execute(() -> {
            try {
                long id = repo.cadastrar(nome, email, senha, confirmacao);
                sessao.iniciar(id);
                sucesso.postValue(new Evento<>(true));
            } catch (ErroAmigavel e) {
                erro.postValue(new Evento<>(e.getMessage()));
            } finally {
                carregando.postValue(false);
            }
        });
    }
}
