package br.ufms.pokecalcrb.data.repo;

import android.content.Context;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import br.ufms.pokecalcrb.data.api.ApiDtos;
import br.ufms.pokecalcrb.data.api.ConversorGen1;
import br.ufms.pokecalcrb.data.api.ErroAmigavel;
import br.ufms.pokecalcrb.data.api.PokeApiService;
import br.ufms.pokecalcrb.data.db.AppDatabase;
import br.ufms.pokecalcrb.data.db.CacheDao;
import br.ufms.pokecalcrb.data.db.GolpeCache;
import br.ufms.pokecalcrb.data.db.PokemonCache;
import br.ufms.pokecalcrb.domain.Golpe;
import br.ufms.pokecalcrb.domain.Pokemon;
import br.ufms.pokecalcrb.domain.Tipo;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Busca Pokémon e golpes. Primeiro olha o cache no Room; só faz requisição HTTP
 * se não tiver. Isso economiza bateria e dados e deixa funcionar offline
 * para o que já foi consultado. Chamar FORA da thread principal.
 */
public class PokemonRepository {

    private static PokeApiService api;
    private final CacheDao cache;

    public PokemonRepository(Context context) {
        cache = AppDatabase.get(context).cacheDao();
        synchronized (PokemonRepository.class) {
            if (api == null) api = PokeApiService.criar();
        }
    }

    public Pokemon buscarPokemon(String entrada) throws ErroAmigavel {
        String chave = ConversorGen1.normalizarEntrada(entrada);
        if (chave.isEmpty()) throw new ErroAmigavel("Informe o nome ou o número do Pokémon");

        // Número digitado: valida a faixa antes de gastar uma requisição.
        PokemonCache c;
        if (chave.matches("\\d+")) {
            int numero;
            try {
                numero = Integer.parseInt(chave);
            } catch (NumberFormatException e) {
                numero = -1;
            }
            if (numero < 1 || numero > ConversorGen1.ULTIMO_POKEMON_GEN1) {
                throw new ErroAmigavel("Número inválido: use de 1 a 151 (Gen 1)");
            }
            chave = String.valueOf(numero);
            c = cache.pokemonPorNumero(numero);
        } else {
            c = cache.pokemonPorNome(chave);
        }
        if (c != null) return deCache(c);

        ApiDtos.PokemonResponse r = executar(api.pokemon(chave),
                "Pokémon \"" + entrada.trim() + "\" não encontrado");
        Pokemon p = ConversorGen1.paraPokemon(r);
        cache.salvarPokemon(paraCache(p));
        return p;
    }

    public Golpe buscarGolpe(String entrada) throws ErroAmigavel {
        String chave = ConversorGen1.normalizarEntrada(entrada);
        if (chave.isEmpty()) throw new ErroAmigavel("Informe o golpe");

        GolpeCache c = cache.golpePorNome(chave);
        if (c != null) {
            Tipo t = Tipo.deApi(c.tipo);
            if (t != null) return new Golpe(c.nome, c.poder, t);
        }

        ApiDtos.MoveResponse m = executar(api.golpe(chave),
                "Golpe \"" + entrada.trim() + "\" não encontrado");
        Golpe g = ConversorGen1.paraGolpe(m);
        GolpeCache novo = new GolpeCache();
        novo.nome = g.nome;
        novo.poder = g.poder;
        novo.tipo = g.tipo.nomeApi;
        cache.salvarGolpe(novo);
        return g;
    }

    /** Executa a chamada HTTP e traduz qualquer falha para uma mensagem amigável. */
    private static <T> T executar(Call<T> call, String msg404) throws ErroAmigavel {
        try {
            Response<T> resp = call.execute();
            if (resp.code() == 404) throw new ErroAmigavel(msg404);
            if (!resp.isSuccessful() || resp.body() == null) {
                throw new ErroAmigavel("A PokéAPI respondeu com erro (" + resp.code() + "). Tente de novo.");
            }
            return resp.body();
        } catch (InterruptedIOException e) { // inclui SocketTimeoutException (timeout)
            throw new ErroAmigavel("A PokéAPI demorou demais para responder. Tente de novo.");
        } catch (UnknownHostException e) {
            throw new ErroAmigavel("Sem conexão com a internet. Pokémon já consultados funcionam offline.");
        } catch (IOException e) {
            throw new ErroAmigavel("Falha de rede. Verifique a internet e tente de novo.");
        } catch (RuntimeException e) {
            // Ex.: JSON inesperado. Nunca deixa o app fechar.
            throw new ErroAmigavel("Não foi possível ler a resposta da PokéAPI.");
        }
    }

    private static Pokemon deCache(PokemonCache c) {
        String[] tipos = c.tipos.split(",");
        Tipo t1 = Tipo.deApi(tipos[0]);
        Tipo t2 = tipos.length > 1 ? Tipo.deApi(tipos[1]) : null;
        if (t1 == null) t1 = Tipo.NORMAL;
        return new Pokemon(c.numero, c.nome, c.hp, c.atk, c.def, c.special, c.speed, t1, t2, c.spriteUrl);
    }

    private static PokemonCache paraCache(Pokemon p) {
        PokemonCache c = new PokemonCache();
        c.numero = p.numero;
        c.nome = p.nome;
        c.hp = p.hp;
        c.atk = p.ataque;
        c.def = p.defesa;
        c.special = p.special;
        c.speed = p.speed;
        c.tipos = p.tipo2 == null ? p.tipo1.nomeApi : p.tipo1.nomeApi + "," + p.tipo2.nomeApi;
        c.spriteUrl = p.spriteUrl;
        return c;
    }
}
