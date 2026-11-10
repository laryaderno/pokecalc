package br.ufms.pokecalcrb.ui.calc;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.ufms.pokecalcrb.data.api.ErroAmigavel;
import br.ufms.pokecalcrb.data.db.Calculo;
import br.ufms.pokecalcrb.data.repo.CalculoRepository;
import br.ufms.pokecalcrb.data.repo.PokemonRepository;
import br.ufms.pokecalcrb.data.repo.SessionManager;
import br.ufms.pokecalcrb.domain.CalculadoraGen1;
import br.ufms.pokecalcrb.domain.Golpe;
import br.ufms.pokecalcrb.domain.Pokemon;
import br.ufms.pokecalcrb.domain.ResultadoDano;
import br.ufms.pokecalcrb.util.Evento;
import br.ufms.pokecalcrb.util.Executores;
import br.ufms.pokecalcrb.util.Notificacoes;
import br.ufms.pokecalcrb.util.Validacao;

public class CalculadoraViewModel extends AndroidViewModel {

    public enum Status { OCIOSO, CARREGANDO, SUCESSO, ERRO }

    /** Estado da tela (sobrevive à rotação). */
    public static class Estado {
        public Status status = Status.OCIOSO;
        public String erro;
        public Pokemon atacante, defensor;
        public Golpe golpe;
        public int nivelAtq, nivelDef;
        public ResultadoDano resultado;
        public boolean salvo;
    }

    /** Resultado do "Salvar no histórico". */
    public static class Salvamento {
        public final boolean ok;
        public final boolean notificou;
        public final String mensagem;

        Salvamento(boolean ok, boolean notificou, String mensagem) {
            this.ok = ok;
            this.notificou = notificou;
            this.mensagem = mensagem;
        }
    }

    private final PokemonRepository pokemonRepo;
    private final CalculoRepository calculoRepo;
    private final SessionManager sessao;

    private final MutableLiveData<Estado> estado = new MutableLiveData<>(new Estado());
    private final MutableLiveData<Evento<Salvamento>> salvamento = new MutableLiveData<>();

    public CalculadoraViewModel(@NonNull Application app) {
        super(app);
        pokemonRepo = new PokemonRepository(app);
        calculoRepo = new CalculoRepository(app);
        sessao = new SessionManager(app);
    }

    public LiveData<Estado> getEstado() { return estado; }
    public LiveData<Evento<Salvamento>> getSalvamento() { return salvamento; }

    public void calcular(String atacante, String nivelAtq, String defensor, String nivelDef, String golpe) {
        Integer nAtq = Validacao.lerNivel(nivelAtq);
        Integer nDef = Validacao.lerNivel(nivelDef);
        if (nAtq == null || nDef == null) {
            erro("Nível deve estar entre 1 e 100");
            return;
        }
        Estado carregando = new Estado();
        carregando.status = Status.CARREGANDO;
        estado.setValue(carregando);

        Executores.io().execute(() -> {
            try {
                Pokemon a = pokemonRepo.buscarPokemon(atacante);
                Pokemon d = pokemonRepo.buscarPokemon(defensor);
                Golpe g = pokemonRepo.buscarGolpe(golpe);

                Estado e = new Estado();
                e.status = Status.SUCESSO;
                e.atacante = a;
                e.defensor = d;
                e.golpe = g;
                e.nivelAtq = nAtq;
                e.nivelDef = nDef;
                e.resultado = CalculadoraGen1.calcular(a, nAtq, d, nDef, g);
                estado.postValue(e);
            } catch (ErroAmigavel ex) {
                erroPost(ex.getMessage());
            } catch (RuntimeException ex) {
                erroPost("Não foi possível calcular. Confira os dados e tente de novo.");
            }
        });
    }

    public void salvarNoHistorico() {
        Estado e = estado.getValue();
        if (e == null || e.status != Status.SUCESSO || e.salvo) return;
        long userId = sessao.getUserId();
        if (userId == SessionManager.SEM_SESSAO) return;

        Calculo c = new Calculo();
        c.userId = userId;
        c.atacante = e.atacante.nomeExibicao();
        c.defensor = e.defensor.nomeExibicao();
        c.golpe = e.golpe.nomeExibicao();
        c.nivelAtq = e.nivelAtq;
        c.nivelDef = e.nivelDef;
        c.danoMin = e.resultado.danoMin;
        c.danoMax = e.resultado.danoMax;
        c.critMin = e.resultado.critMin;
        c.critMax = e.resultado.critMax;
        c.hpDefensor = e.resultado.hpDefensor;
        c.efetividade = CalculadoraGen1.textoEfetividade(e.resultado.efetividadeCentesimos);
        c.data = System.currentTimeMillis();
        e.salvo = true;
        estado.setValue(e);

        Executores.io().execute(() -> {
            try {
                calculoRepo.salvar(c);
                String texto = "Cálculo salvo: " + c.atacante + " → " + c.defensor
                        + " (" + c.danoMin + "–" + c.danoMax + " HP)";
                boolean notificou = Notificacoes.notificarCalculoSalvo(getApplication(), texto);
                salvamento.postValue(new Evento<>(new Salvamento(true, notificou, texto)));
            } catch (RuntimeException ex) {
                e.salvo = false;
                estado.postValue(e);
                salvamento.postValue(new Evento<>(new Salvamento(false, false,
                        "Não foi possível salvar o cálculo.")));
            }
        });
    }

    private void erro(String msg) {
        Estado e = new Estado();
        e.status = Status.ERRO;
        e.erro = msg;
        estado.setValue(e);
    }

    private void erroPost(String msg) {
        Estado e = new Estado();
        e.status = Status.ERRO;
        e.erro = msg;
        estado.postValue(e);
    }
}
