package br.ufms.pokecalcrb.data.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/** Endpoints da PokéAPI (gratuita, sem chave). */
public interface PokeApiService {

    String BASE_URL = "https://pokeapi.co/api/v2/";

    @GET("pokemon/{nomeOuId}")
    Call<ApiDtos.PokemonResponse> pokemon(@Path("nomeOuId") String nomeOuId);

    @GET("move/{nome}")
    Call<ApiDtos.MoveResponse> golpe(@Path("nome") String nome);

    /** Cria o cliente com timeouts curtos (evita a tela "travada" e gasto de bateria). */
    static PokeApiService criar() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PokeApiService.class);
    }
}
