package br.ufms.pokecalcrb.data.repo;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.lifecycle.LiveData;

import java.io.File;

import br.ufms.pokecalcrb.data.api.ErroAmigavel;
import br.ufms.pokecalcrb.data.db.AppDatabase;
import br.ufms.pokecalcrb.data.db.User;
import br.ufms.pokecalcrb.data.db.UserDao;
import br.ufms.pokecalcrb.security.SenhaHasher;
import br.ufms.pokecalcrb.util.Validacao;

/**
 * Regras de conta: cadastro, login, edição de perfil, troca de senha e foto.
 * Os métodos sem LiveData acessam o banco e o BCrypt, então devem ser
 * chamados FORA da thread principal (os ViewModels usam Executores.io()).
 */
public class UserRepository {

    private final UserDao dao;

    public UserRepository(Context context) {
        dao = AppDatabase.get(context).userDao();
    }

    public LiveData<User> observar(long id) {
        return dao.observarPorId(id);
    }

    public User buscar(long id) {
        return dao.buscarPorId(id);
    }

    public long cadastrar(String nome, String email, String senha, String confirmacao) throws ErroAmigavel {
        String erro = primeiroErro(Validacao.erroNome(nome), Validacao.erroEmail(email),
                Validacao.erroSenha(senha), Validacao.erroConfirmacao(senha, confirmacao));
        if (erro != null) throw new ErroAmigavel(erro);

        String emailNorm = Validacao.normalizarEmail(email);
        if (dao.buscarPorEmail(emailNorm) != null) {
            throw new ErroAmigavel("Este e-mail já está cadastrado");
        }
        User u = new User();
        u.nome = nome.trim();
        u.email = emailNorm;
        u.senhaHash = SenhaHasher.gerarHash(senha); // nunca guarda a senha em texto
        try {
            return dao.inserir(u);
        } catch (SQLiteConstraintException e) {
            throw new ErroAmigavel("Este e-mail já está cadastrado");
        }
    }

    public User login(String email, String senha) throws ErroAmigavel {
        String erro = primeiroErro(Validacao.erroEmail(email),
                (senha == null || senha.isEmpty()) ? "Informe a senha" : null);
        if (erro != null) throw new ErroAmigavel(erro);

        User u = dao.buscarPorEmail(Validacao.normalizarEmail(email));
        // Mesma mensagem para e-mail inexistente e senha errada (não revela quais e-mails existem).
        if (u == null || !SenhaHasher.conferir(senha, u.senhaHash)) {
            throw new ErroAmigavel("E-mail ou senha incorretos");
        }
        return u;
    }

    public void atualizarDados(long id, String nome, String email) throws ErroAmigavel {
        String erro = primeiroErro(Validacao.erroNome(nome), Validacao.erroEmail(email));
        if (erro != null) throw new ErroAmigavel(erro);

        User u = dao.buscarPorId(id);
        if (u == null) throw new ErroAmigavel("Sessão inválida. Entre novamente.");

        String emailNorm = Validacao.normalizarEmail(email);
        User dono = dao.buscarPorEmail(emailNorm);
        if (dono != null && dono.id != id) throw new ErroAmigavel("Este e-mail já está em uso por outra conta");

        u.nome = nome.trim();
        u.email = emailNorm;
        dao.atualizar(u);
    }

    public void trocarSenha(long id, String senhaAtual, String novaSenha, String confirmacao) throws ErroAmigavel {
        User u = dao.buscarPorId(id);
        if (u == null) throw new ErroAmigavel("Sessão inválida. Entre novamente.");
        if (senhaAtual == null || senhaAtual.isEmpty()) throw new ErroAmigavel("Informe a senha atual");
        if (!SenhaHasher.conferir(senhaAtual, u.senhaHash)) throw new ErroAmigavel("A senha atual está incorreta");

        String erro = primeiroErro(Validacao.erroSenha(novaSenha), Validacao.erroConfirmacao(novaSenha, confirmacao));
        if (erro != null) throw new ErroAmigavel(erro);

        // Novo hash com novo salt. A senha antiga não fica guardada em lugar nenhum.
        u.senhaHash = SenhaHasher.gerarHash(novaSenha);
        dao.atualizar(u);
    }

    /** Salva o caminho da nova foto e apaga o arquivo da foto anterior. */
    public void atualizarFoto(long id, String novoCaminho) throws ErroAmigavel {
        User u = dao.buscarPorId(id);
        if (u == null) throw new ErroAmigavel("Sessão inválida. Entre novamente.");
        String antigo = u.fotoPath;
        u.fotoPath = novoCaminho;
        dao.atualizar(u);
        if (antigo != null && !antigo.equals(novoCaminho)) {
            //noinspection ResultOfMethodCallIgnored
            new File(antigo).delete();
        }
    }

    private static String primeiroErro(String... erros) {
        for (String e : erros) if (e != null) return e;
        return null;
    }
}
