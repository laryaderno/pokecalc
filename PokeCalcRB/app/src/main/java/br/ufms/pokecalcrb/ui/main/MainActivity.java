package br.ufms.pokecalcrb.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import br.ufms.pokecalcrb.R;
import br.ufms.pokecalcrb.data.repo.SessionManager;
import br.ufms.pokecalcrb.ui.auth.LoginActivity;
import br.ufms.pokecalcrb.ui.calc.CalculadoraFragment;
import br.ufms.pokecalcrb.ui.historico.HistoricoFragment;
import br.ufms.pokecalcrb.ui.perfil.PerfilFragment;

/**
 * Tela principal (só para usuário AUTENTICADO). Também é a tela de abertura:
 * a Splash Screen API fica na tela enquanto a sessão é verificada, sem atraso
 * artificial, e então vai para o Login ou mostra a Calculadora.
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_ABA = "aba";
    public static final String ABA_HISTORICO = "historico";

    private static final String TAG_CALC = "calc";
    private static final String TAG_HIST = "hist";
    private static final String TAG_PERFIL = "perfil";
    private static final String PREF_PEDIU_NOTIF = "pediu_notificacao";

    private SessionManager sessao;
    private SessaoViewModel sessaoVm;
    private BottomNavigationView nav;
    private volatile boolean pronto = false;

    private final ActivityResultLauncher<String> permissaoNotificacao =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedida -> {
                if (!concedida) {
                    Snackbar.make(nav, R.string.notif_negada, Snackbar.LENGTH_LONG)
                            .setAnchorView(nav)
                            .setAction(R.string.configuracoes, v -> abrirConfigNotificacoes())
                            .show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        sessao = new SessionManager(this);
        if (!sessao.estaLogado()) {
            irParaLogin();
            return;
        }
        // Mantém a splash enquanto o banco confirma que o usuário da sessão existe.
        splash.setKeepOnScreenCondition(() -> !pronto);

        setContentView(R.layout.activity_main);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            mostrarAba(item.getItemId());
            return true;
        });

        sessaoVm = new ViewModelProvider(this).get(SessaoViewModel.class);
        sessaoVm.getUsuario().observe(this, u -> {
            if (u == null) {
                // Sessão aponta para um usuário que não existe mais.
                if (pronto || sessaoVm.verificacaoConcluida()) fazerLogout(false);
                return;
            }
            pronto = true;
            toolbar.setSubtitle(getString(R.string.ola_usuario, u.nome));
        });
        sessaoVm.getSessaoInvalida().observe(this, invalida -> {
            if (Boolean.TRUE.equals(invalida)) fazerLogout(false);
        });

        if (savedInstanceState == null) {
            boolean abrirHistorico = ABA_HISTORICO.equals(getIntent().getStringExtra(EXTRA_ABA));
            nav.setSelectedItemId(abrirHistorico ? R.id.nav_historico : R.id.nav_calc);
            pedirPermissaoNotificacaoUmaVez();
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        if (nav != null && ABA_HISTORICO.equals(intent.getStringExtra(EXTRA_ABA))) {
            nav.setSelectedItemId(R.id.nav_historico);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Proteção extra: se a sessão sumiu (logout), não deixa ficar aqui.
        if (sessao != null && !sessao.estaLogado()) irParaLogin();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.acao_sair) {
            confirmarLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Troca de aba mantendo o estado de cada fragmento (show/hide). */
    private void mostrarAba(int itemId) {
        String tag;
        if (itemId == R.id.nav_historico) tag = TAG_HIST;
        else if (itemId == R.id.nav_perfil) tag = TAG_PERFIL;
        else tag = TAG_CALC;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        for (String t : new String[]{TAG_CALC, TAG_HIST, TAG_PERFIL}) {
            Fragment f = fm.findFragmentByTag(t);
            if (f != null && !t.equals(tag)) tx.hide(f);
        }
        Fragment alvo = fm.findFragmentByTag(tag);
        if (alvo == null) {
            alvo = TAG_HIST.equals(tag) ? new HistoricoFragment()
                    : TAG_PERFIL.equals(tag) ? new PerfilFragment()
                    : new CalculadoraFragment();
            tx.add(R.id.container, alvo, tag);
        } else {
            tx.show(alvo);
        }
        tx.commit();
    }

    public void confirmarLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.sair_titulo)
                .setMessage(R.string.sair_mensagem)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.sair, (d, w) -> fazerLogout(true))
                .show();
    }

    /** Logout: limpa a sessão e volta ao Login sem deixar a Main na pilha (o "voltar" não retorna). */
    public void fazerLogout(boolean avisar) {
        sessao.encerrar();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (avisar) i.putExtra(LoginActivity.EXTRA_MENSAGEM, getString(R.string.logout_ok));
        startActivity(i);
        finish();
    }

    private void irParaLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    /** No Android 13+ pede a permissão de notificação uma vez. Se negar, o app segue normal. */
    private void pedirPermissaoNotificacaoUmaVez() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) return;
        SharedPreferences p = getSharedPreferences("app", MODE_PRIVATE);
        if (p.getBoolean(PREF_PEDIU_NOTIF, false)) return;
        p.edit().putBoolean(PREF_PEDIU_NOTIF, true).apply();
        permissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    /** Chamado pela Calculadora quando o usuário quer ativar as notificações. */
    public void pedirPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
                && shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            permissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            // Já foi negada de vez (ou foi desligada nas configurações): abre as configurações.
            abrirConfigNotificacoes();
        }
    }

    private void abrirConfigNotificacoes() {
        Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        try {
            startActivity(i);
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", getPackageName(), null)));
        }
    }

    public BottomNavigationView getNav() {
        return nav;
    }
}
