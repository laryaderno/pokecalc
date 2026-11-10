package br.ufms.pokecalcrb.ui.calc;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import br.ufms.pokecalcrb.R;
import br.ufms.pokecalcrb.domain.CalculadoraGen1;
import br.ufms.pokecalcrb.domain.Pokemon;
import br.ufms.pokecalcrb.domain.ResultadoDano;
import br.ufms.pokecalcrb.ui.main.MainActivity;
import br.ufms.pokecalcrb.util.NomesGen1;
import br.ufms.pokecalcrb.util.Validacao;

/** Aba 1: calculadora de dano. */
public class CalculadoraFragment extends Fragment {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private CalculadoraViewModel vm;

    private TextInputLayout tilAtacante, tilNivelAtq, tilDefensor, tilNivelDef, tilGolpe;
    private MaterialAutoCompleteTextView etAtacante, etDefensor, etGolpe;
    private TextInputEditText etNivelAtq, etNivelDef;
    private Button btnCalcular, btnSalvar;
    private LinearProgressIndicator progresso;
    private TextView tvErro;
    private MaterialCardView cardResultado;

    private ImageView ivAtq, ivDef;
    private TextView tvNomeAtq, tvTiposAtq, tvNomeDef, tvTiposDef;
    private TextView tvGolpeInfo, tvStats, tvNormal, tvCritico, tvNocaute, tvEfetividade, tvChanceCrit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculadora, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        tilAtacante = v.findViewById(R.id.til_atacante);
        tilNivelAtq = v.findViewById(R.id.til_nivel_atq);
        tilDefensor = v.findViewById(R.id.til_defensor);
        tilNivelDef = v.findViewById(R.id.til_nivel_def);
        tilGolpe = v.findViewById(R.id.til_golpe);
        etAtacante = v.findViewById(R.id.et_atacante);
        etDefensor = v.findViewById(R.id.et_defensor);
        etGolpe = v.findViewById(R.id.et_golpe);
        etNivelAtq = v.findViewById(R.id.et_nivel_atq);
        etNivelDef = v.findViewById(R.id.et_nivel_def);
        btnCalcular = v.findViewById(R.id.btn_calcular);
        btnSalvar = v.findViewById(R.id.btn_salvar);
        progresso = v.findViewById(R.id.progresso);
        tvErro = v.findViewById(R.id.tv_erro);
        cardResultado = v.findViewById(R.id.card_resultado);
        ivAtq = v.findViewById(R.id.iv_sprite_atq);
        ivDef = v.findViewById(R.id.iv_sprite_def);
        tvNomeAtq = v.findViewById(R.id.tv_nome_atq);
        tvTiposAtq = v.findViewById(R.id.tv_tipos_atq);
        tvNomeDef = v.findViewById(R.id.tv_nome_def);
        tvTiposDef = v.findViewById(R.id.tv_tipos_def);
        tvGolpeInfo = v.findViewById(R.id.tv_golpe_info);
        tvStats = v.findViewById(R.id.tv_stats);
        tvNormal = v.findViewById(R.id.tv_dano_normal);
        tvCritico = v.findViewById(R.id.tv_dano_critico);
        tvNocaute = v.findViewById(R.id.tv_nocaute);
        tvEfetividade = v.findViewById(R.id.tv_efetividade);
        tvChanceCrit = v.findViewById(R.id.tv_chance_crit);

        // Autocompletar (usabilidade): nomes da Gen 1 no formato aceito pela API.
        ArrayAdapter<String> pokes = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, NomesGen1.POKEMON);
        etAtacante.setAdapter(pokes);
        etDefensor.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, NomesGen1.POKEMON));
        etGolpe.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, NomesGen1.GOLPES));

        vm = new ViewModelProvider(this).get(CalculadoraViewModel.class);

        btnCalcular.setOnClickListener(x -> tentarCalcular());
        btnSalvar.setOnClickListener(x -> vm.salvarNoHistorico());

        vm.getEstado().observe(getViewLifecycleOwner(), this::renderizar);
        vm.getSalvamento().observe(getViewLifecycleOwner(), ev -> {
            CalculadoraViewModel.Salvamento s = ev.pegarSeNaoTratado();
            if (s == null) return;
            View ancora = ((MainActivity) requireActivity()).getNav();
            if (!s.ok) {
                Snackbar.make(v, s.mensagem, Snackbar.LENGTH_LONG).setAnchorView(ancora).show();
            } else if (s.notificou) {
                Snackbar.make(v, R.string.calculo_salvo, Snackbar.LENGTH_SHORT).setAnchorView(ancora).show();
            } else {
                // Sem permissão de notificação: salva igual e avisa.
                Snackbar.make(v, R.string.calculo_salvo_sem_notif, Snackbar.LENGTH_LONG)
                        .setAnchorView(ancora)
                        .setAction(R.string.ativar, x ->
                                ((MainActivity) requireActivity()).pedirPermissaoNotificacao())
                        .show();
            }
        });
    }

    private void tentarCalcular() {
        String atq = etAtacante.getText() == null ? "" : etAtacante.getText().toString();
        String def = etDefensor.getText() == null ? "" : etDefensor.getText().toString();
        String golpe = etGolpe.getText() == null ? "" : etGolpe.getText().toString();
        String nAtq = etNivelAtq.getText() == null ? "" : etNivelAtq.getText().toString();
        String nDef = etNivelDef.getText() == null ? "" : etNivelDef.getText().toString();

        boolean ok = true;
        ok &= marcar(tilAtacante, atq.trim().isEmpty() ? getString(R.string.erro_pokemon_vazio) : null);
        ok &= marcar(tilDefensor, def.trim().isEmpty() ? getString(R.string.erro_pokemon_vazio) : null);
        ok &= marcar(tilGolpe, golpe.trim().isEmpty() ? getString(R.string.erro_golpe_vazio) : null);
        ok &= marcar(tilNivelAtq, Validacao.lerNivel(nAtq) == null ? getString(R.string.erro_nivel) : null);
        ok &= marcar(tilNivelDef, Validacao.lerNivel(nDef) == null ? getString(R.string.erro_nivel) : null);
        if (!ok) return;

        esconderTeclado();
        vm.calcular(atq, nAtq, def, nDef, golpe);
    }

    private boolean marcar(TextInputLayout til, String erro) {
        til.setError(erro);
        return erro == null;
    }

    private void renderizar(CalculadoraViewModel.Estado e) {
        boolean carregando = e.status == CalculadoraViewModel.Status.CARREGANDO;
        progresso.setVisibility(carregando ? View.VISIBLE : View.GONE);
        btnCalcular.setEnabled(!carregando);

        if (e.status == CalculadoraViewModel.Status.ERRO) {
            tvErro.setText(e.erro);
            tvErro.setVisibility(View.VISIBLE);
        } else {
            tvErro.setVisibility(View.GONE);
        }

        if (e.status != CalculadoraViewModel.Status.SUCESSO) {
            if (!carregando) cardResultado.setVisibility(View.GONE);
            return;
        }

        ResultadoDano r = e.resultado;
        cardResultado.setVisibility(View.VISIBLE);
        mostrarPokemon(e.atacante, e.nivelAtq, ivAtq, tvNomeAtq, tvTiposAtq);
        mostrarPokemon(e.defensor, e.nivelDef, ivDef, tvNomeDef, tvTiposDef);

        tvGolpeInfo.setText(getString(R.string.golpe_info,
                e.golpe.nomeExibicao(), e.golpe.tipo.nomePt, e.golpe.poder,
                getString(r.golpeFisico ? R.string.fisico : R.string.especial),
                r.stab ? getString(R.string.com_stab) : ""));
        tvStats.setText(getString(r.golpeFisico ? R.string.stats_fisico : R.string.stats_especial,
                r.ataqueUsado, r.defesaUsada, r.hpDefensor));
        tvNormal.setText(getString(R.string.dano_normal, r.danoMin, r.danoMax,
                pct(r.porcentagemMin()), pct(r.porcentagemMax())));
        tvCritico.setText(getString(R.string.dano_critico, r.critMin, r.critMax,
                pct(r.porcentagemCritMin()), pct(r.porcentagemCritMax())));
        tvNocaute.setText(CalculadoraGen1.textoNocaute(r.hpDefensor, r.danoMin, r.danoMax));
        tvEfetividade.setText(CalculadoraGen1.textoEfetividade(r.efetividadeCentesimos));
        tvChanceCrit.setText(getString(R.string.chance_critico, pct(r.chanceCritico)));

        btnSalvar.setEnabled(!e.salvo);
        btnSalvar.setText(e.salvo ? R.string.ja_salvo : R.string.salvar_historico);

        // TalkBack lê o resumo do resultado assim que ele aparece.
        cardResultado.announceForAccessibility(getString(R.string.resumo_acessivel,
                e.atacante.nomeExibicao(), e.defensor.nomeExibicao(), r.danoMin, r.danoMax,
                tvEfetividade.getText()));
    }

    private void mostrarPokemon(Pokemon p, int nivel, ImageView iv, TextView nome, TextView tipos) {
        nome.setText(getString(R.string.nome_nivel, p.numero, p.nomeExibicao(), nivel));
        tipos.setText(p.tiposExibicao());
        iv.setContentDescription(getString(R.string.desc_sprite, p.nomeExibicao()));
        Glide.with(this).load(p.spriteUrl)
                .placeholder(R.drawable.ic_pokemon_placeholder)
                .error(R.drawable.ic_pokemon_placeholder)
                .into(iv);
    }

    private static String pct(double v) {
        return String.format(PT_BR, "%.1f%%", v);
    }

    private void esconderTeclado() {
        View foco = requireActivity().getCurrentFocus();
        if (foco == null) return;
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(foco.getWindowToken(), 0);
    }
}
