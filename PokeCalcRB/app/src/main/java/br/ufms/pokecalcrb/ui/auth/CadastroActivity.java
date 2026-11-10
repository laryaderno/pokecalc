package br.ufms.pokecalcrb.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import br.ufms.pokecalcrb.R;
import br.ufms.pokecalcrb.ui.main.MainActivity;
import br.ufms.pokecalcrb.util.Validacao;

/** Tela 2 (usuário NÃO autenticado): criar conta com nome, e-mail e senha. */
public class CadastroActivity extends AppCompatActivity {

    private AuthViewModel vm;
    private TextInputLayout tilNome, tilEmail, tilSenha, tilConfirmar;
    private TextInputEditText etNome, etEmail, etSenha, etConfirmar;
    private Button btnCadastrar;
    private TextView tvErro;
    private LinearProgressIndicator progresso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        vm = new ViewModelProvider(this).get(AuthViewModel.class);

        tilNome = findViewById(R.id.til_nome);
        tilEmail = findViewById(R.id.til_email);
        tilSenha = findViewById(R.id.til_senha);
        tilConfirmar = findViewById(R.id.til_confirmar);
        etNome = findViewById(R.id.et_nome);
        etEmail = findViewById(R.id.et_email);
        etSenha = findViewById(R.id.et_senha);
        etConfirmar = findViewById(R.id.et_confirmar);
        btnCadastrar = findViewById(R.id.btn_cadastrar);
        tvErro = findViewById(R.id.tv_erro);
        progresso = findViewById(R.id.progresso);

        btnCadastrar.setOnClickListener(v -> tentarCadastrar());

        vm.getCarregando().observe(this, c -> {
            progresso.setVisibility(c ? View.VISIBLE : View.GONE);
            btnCadastrar.setEnabled(!c);
        });
        vm.getErro().observe(this, ev -> {
            String msg = ev.pegarSeNaoTratado();
            if (msg == null) return;
            // E-mail repetido aparece no próprio campo; o resto, na mensagem geral.
            if (msg.contains("e-mail")) tilEmail.setError(msg);
            else {
                tvErro.setText(msg);
                tvErro.setVisibility(View.VISIBLE);
            }
        });
        vm.getSucesso().observe(this, ev -> {
            if (ev.pegarSeNaoTratado() == null) return;
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    private void tentarCadastrar() {
        String nome = LoginActivity.texto(etNome);
        String email = LoginActivity.texto(etEmail);
        String senha = LoginActivity.texto(etSenha);
        String confirmar = LoginActivity.texto(etConfirmar);
        tvErro.setVisibility(View.GONE);

        String eNome = Validacao.erroNome(nome);
        String eEmail = Validacao.erroEmail(email);
        String eSenha = Validacao.erroSenha(senha);
        String eConf = Validacao.erroConfirmacao(senha, confirmar);
        tilNome.setError(eNome);
        tilEmail.setError(eEmail);
        tilSenha.setError(eSenha);
        tilConfirmar.setError(eConf);
        if (eNome != null || eEmail != null || eSenha != null || eConf != null) return;

        vm.cadastrar(nome, email, senha, confirmar);
    }
}
