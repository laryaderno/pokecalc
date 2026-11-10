package br.ufms.pokecalcrb.ui.perfil;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import br.ufms.pokecalcrb.data.api.ErroAmigavel;
import br.ufms.pokecalcrb.data.db.User;
import br.ufms.pokecalcrb.data.repo.SessionManager;
import br.ufms.pokecalcrb.data.repo.UserRepository;
import br.ufms.pokecalcrb.util.Evento;
import br.ufms.pokecalcrb.util.Executores;

public class PerfilViewModel extends AndroidViewModel {

    /** Resultado de uma ação do perfil (mensagem + se deu certo). */
    public static class Resultado {
        public final boolean ok;
        public final String mensagem;

        Resultado(boolean ok, String mensagem) {
            this.ok = ok;
            this.mensagem = mensagem;
        }
    }

    private final UserRepository repo;
    private final long userId;
    private final LiveData<User> usuario;
    private final MutableLiveData<Boolean> carregando = new MutableLiveData<>(false);
    private final MutableLiveData<Evento<Resultado>> resultadoDados = new MutableLiveData<>();
    private final MutableLiveData<Evento<Resultado>> resultadoSenha = new MutableLiveData<>();
    private final MutableLiveData<Evento<Resultado>> resultadoFoto = new MutableLiveData<>();

    public PerfilViewModel(@NonNull Application app) {
        super(app);
        repo = new UserRepository(app);
        userId = new SessionManager(app).getUserId();
        usuario = repo.observar(userId);
    }

    public LiveData<User> getUsuario() { return usuario; }
    public LiveData<Boolean> getCarregando() { return carregando; }
    public LiveData<Evento<Resultado>> getResultadoDados() { return resultadoDados; }
    public LiveData<Evento<Resultado>> getResultadoSenha() { return resultadoSenha; }
    public LiveData<Evento<Resultado>> getResultadoFoto() { return resultadoFoto; }

    public void salvarDados(String nome, String email) {
        carregando.setValue(true);
        Executores.io().execute(() -> {
            try {
                repo.atualizarDados(userId, nome, email);
                resultadoDados.postValue(new Evento<>(new Resultado(true, "Perfil atualizado")));
            } catch (ErroAmigavel e) {
                resultadoDados.postValue(new Evento<>(new Resultado(false, e.getMessage())));
            } finally {
                carregando.postValue(false);
            }
        });
    }

    public void trocarSenha(String atual, String nova, String confirmacao) {
        carregando.setValue(true);
        Executores.io().execute(() -> {
            try {
                repo.trocarSenha(userId, atual, nova, confirmacao);
                resultadoSenha.postValue(new Evento<>(new Resultado(true, "Senha alterada")));
            } catch (ErroAmigavel e) {
                resultadoSenha.postValue(new Evento<>(new Resultado(false, e.getMessage())));
            } finally {
                carregando.postValue(false);
            }
        });
    }

    /** Pasta interna do app onde as fotos ficam (outros apps não acessam). */
    public File pastaFotos() {
        File dir = new File(getApplication().getFilesDir(), "fotos");
        //noinspection ResultOfMethodCallIgnored
        dir.mkdirs();
        return dir;
    }

    public File novoArquivoFoto() {
        return new File(pastaFotos(), "perfil_" + userId + "_" + System.currentTimeMillis() + ".jpg");
    }

    /** Foto tirada pela câmera: o arquivo já está na pasta interna, só grava o caminho no Room. */
    public void usarFotoDaCamera(File arquivo) {
        Executores.io().execute(() -> {
            try {
                if (!arquivo.exists() || arquivo.length() == 0) {
                    throw new ErroAmigavel("A câmera não retornou nenhuma foto");
                }
                repo.atualizarFoto(userId, arquivo.getAbsolutePath());
                resultadoFoto.postValue(new Evento<>(new Resultado(true, "Foto atualizada")));
            } catch (ErroAmigavel e) {
                resultadoFoto.postValue(new Evento<>(new Resultado(false, e.getMessage())));
            }
        });
    }

    /** Foto escolhida na galeria: copia para a pasta interna e grava o caminho no Room. */
    public void usarFotoDaGaleria(Uri uri) {
        Executores.io().execute(() -> {
            File destino = novoArquivoFoto();
            try (InputStream in = getApplication().getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(destino)) {
                if (in == null) throw new IOException("sem dados");
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            } catch (IOException | SecurityException e) {
                //noinspection ResultOfMethodCallIgnored
                destino.delete();
                resultadoFoto.postValue(new Evento<>(new Resultado(false, "Não foi possível ler a imagem escolhida")));
                return;
            }
            try {
                repo.atualizarFoto(userId, destino.getAbsolutePath());
                resultadoFoto.postValue(new Evento<>(new Resultado(true, "Foto atualizada")));
            } catch (ErroAmigavel e) {
                resultadoFoto.postValue(new Evento<>(new Resultado(false, e.getMessage())));
            }
        });
    }
}
