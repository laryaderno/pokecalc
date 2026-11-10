package br.ufms.pokecalcrb.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import br.ufms.pokecalcrb.R;
import br.ufms.pokecalcrb.data.repo.SessionManager;
import br.ufms.pokecalcrb.ui.main.MainActivity;
import br.ufms.pokecalcrb.util.Validacao;

/** Tela 1 (usuário NÃO autenticado): entrar com e-mail e senha. */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_MENSAGEM = "mensagem";

    private AuthViewModel vm;
    private TextInputLayout tilEmail, tilSenha;
    private TextInputEditText etEmail, etSenha;
    private Button btnEntrar;
    private TextView tvErro;
    private LinearProgressIndicator progresso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Se já existe sessão, não faz sentido mostrar o login.
        if (new SessionManager(this).estaLogado()) {
            abrirPrincipal();
            return;
        }

        setContentView(R.layout.activity_login);
        vm = new ViewModelProvider(this).get(AuthViewModel.class);

        tilEmail = findViewById(R.id.til_email);
        tilSenha = findViewById(R.id.til_senha);
        etEmail = findViewById(R.id.et_email);
        etSenha = findViewById(R.id.et_senha);
        btnEntrar = findViewById(R.id.btn_entrar);
        tvErro = findViewById(R.id.tv_erro);
        progresso = findViewById(R.id.progresso);

        btnEntrar.setOnClickListener(v -> tentarEntrar());
        etSenha.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { tentarEntrar(); return true; }
            return false;
        });
        findViewById(R.id.btn_ir_cadastro).setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));

        vm.getCarregando().observe(this, c -> {
            progresso.setVisibility(c ? View.VISIBLE : View.GONE);
            btnEntrar.setEnabled(!c);
        });
        vm.getErro().observe(this, ev -> {
            String msg = ev.pegarSeNaoTratado();
            if (msg != null) mostrarErro(msg);
        });
        vm.getSucesso().observe(this, ev -> {
            if (ev.pegarSeNaoTratado() != null) abrirPrincipal();
        });

        // Ex.: "Você saiu da conta." depois do logout.
        String msg = getIntent().getStringExtra(EXTRA_MENSAGEM);
        if (msg != null && savedInstanceState == null) {
            com.google.android.material.snackbar.Snackbar
                    .make(btnEntrar, msg, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        }
    }

    private void tentarEntrar() {
        String email = texto(etEmail);
        String senha = texto(etSenha);
        tilEmail.setError(null);
        tilSenha.setError(null);
        tvErro.setVisibility(View.GONE);

        // Validação local, campo a campo, com mensagem clara.
        String erroEmail = Validacao.erroEmail(email);
        String erroSenha = senha.isEmpty() ? getString(R.string.erro_informe_senha) : null;
        if (erroEmail != null) tilEmail.setError(erroEmail);
        if (erroSenha != null) tilSenha.setError(erroSenha);
        if (erroEmail != null || erroSenha != null) return;

        vm.entrar(email, senha);
    }

    private void mostrarErro(String msg) {
        tvErro.setText(msg);
        tvErro.setVisibility(View.VISIBLE);
    }

    private void abrirPrincipal() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    static String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString();
    }
}
