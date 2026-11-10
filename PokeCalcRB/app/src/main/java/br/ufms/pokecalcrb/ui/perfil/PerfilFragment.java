package br.ufms.pokecalcrb.ui.perfil;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

import br.ufms.pokecalcrb.R;
import br.ufms.pokecalcrb.data.db.User;
import br.ufms.pokecalcrb.ui.main.MainActivity;
import br.ufms.pokecalcrb.util.Validacao;

/** Aba 3: editar nome, e-mail, senha e foto; e sair (logout). */
public class PerfilFragment extends Fragment {

    private static final String ESTADO_FOTO_TEMP = "foto_temp";

    private PerfilViewModel vm;
    private ImageView ivFoto;
    private TextInputLayout tilNome, tilEmail;
    private TextInputEditText etNome, etEmail;
    private Button btnSalvar;
    private LinearProgressIndicator progresso;
    private File fotoTemp;
    private boolean camposPreenchidos = false;
    private AlertDialog dialogSenha;

    private final ActivityResultLauncher<String> permissaoCamera =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedida -> {
                if (concedida) abrirCamera();
                else cameraNegada();
            });

    private final ActivityResultLauncher<Uri> tirarFoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), sucesso -> {
                if (fotoTemp == null) return;
                if (Boolean.TRUE.equals(sucesso)) {
                    vm.usarFotoDaCamera(fotoTemp);
                } else {
                    //noinspection ResultOfMethodCallIgnored
                    fotoTemp.delete(); // usuário cancelou
                }
                fotoTemp = null;
            });

    // Photo Picker: não precisa de permissão de armazenamento.
    private final ActivityResultLauncher<PickVisualMediaRequest> escolherFoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) vm.usarFotoDaGaleria(uri);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String p = savedInstanceState.getString(ESTADO_FOTO_TEMP);
            if (p != null) fotoTemp = new File(p);
        }
        vm = new ViewModelProvider(this).get(PerfilViewModel.class);

        ivFoto = v.findViewById(R.id.iv_foto);
        tilNome = v.findViewById(R.id.til_nome);
        tilEmail = v.findViewById(R.id.til_email);
        etNome = v.findViewById(R.id.et_nome);
        etEmail = v.findViewById(R.id.et_email);
        btnSalvar = v.findViewById(R.id.btn_salvar_perfil);
        progresso = v.findViewById(R.id.progresso);

        v.findViewById(R.id.btn_camera).setOnClickListener(x -> clicouCamera());
        v.findViewById(R.id.btn_galeria).setOnClickListener(x -> abrirGaleria());
        btnSalvar.setOnClickListener(x -> salvarDados());
        v.findViewById(R.id.btn_trocar_senha).setOnClickListener(x -> abrirDialogSenha());
        v.findViewById(R.id.btn_sair).setOnClickListener(x ->
                ((MainActivity) requireActivity()).confirmarLogout());

        vm.getUsuario().observe(getViewLifecycleOwner(), this::mostrarUsuario);
        vm.getCarregando().observe(getViewLifecycleOwner(), c -> {
            progresso.setVisibility(c ? View.VISIBLE : View.GONE);
            btnSalvar.setEnabled(!c);
        });
        vm.getResultadoDados().observe(getViewLifecycleOwner(), ev -> {
            PerfilViewModel.Resultado r = ev.pegarSeNaoTratado();
            if (r == null) return;
            if (!r.ok && r.mensagem.toLowerCase().contains("e-mail")) tilEmail.setError(r.mensagem);
            else if (!r.ok && r.mensagem.toLowerCase().contains("nome")) tilNome.setError(r.mensagem);
            else aviso(r.mensagem);
        });
        vm.getResultadoFoto().observe(getViewLifecycleOwner(), ev -> {
            PerfilViewModel.Resultado r = ev.pegarSeNaoTratado();
            if (r != null) aviso(r.mensagem);
        });
        vm.getResultadoSenha().observe(getViewLifecycleOwner(), ev -> {
            PerfilViewModel.Resultado r = ev.pegarSeNaoTratado();
            if (r == null) return;
            if (r.ok) {
                if (dialogSenha != null) dialogSenha.dismiss();
                aviso(r.mensagem);
            } else if (dialogSenha != null && dialogSenha.isShowing()) {
                TextInputLayout tilErro = dialogSenha.findViewById(
                        r.mensagem.contains("atual") ? R.id.til_senha_atual
                                : r.mensagem.contains("conferem") || r.mensagem.contains("Confirme")
                                ? R.id.til_confirmar : R.id.til_nova_senha);
                if (tilErro != null) tilErro.setError(r.mensagem);
            } else {
                aviso(r.mensagem);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        if (fotoTemp != null) out.putString(ESTADO_FOTO_TEMP, fotoTemp.getAbsolutePath());
    }

    private void mostrarUsuario(User u) {
        if (u == null) return;
        // Preenche os campos só uma vez, para não apagar o que o usuário está digitando.
        if (!camposPreenchidos) {
            etNome.setText(u.nome);
            etEmail.setText(u.email);
            camposPreenchidos = true;
        }
        if (u.fotoPath != null && new File(u.fotoPath).exists()) {
            Glide.with(this).load(new File(u.fotoPath)).circleCrop()
                    .placeholder(R.drawable.ic_avatar).into(ivFoto);
        } else {
            ivFoto.setImageResource(R.drawable.ic_avatar);
        }
    }

    private void salvarDados() {
        String nome = etNome.getText() == null ? "" : etNome.getText().toString();
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString();
        String eNome = Validacao.erroNome(nome);
        String eEmail = Validacao.erroEmail(email);
        tilNome.setError(eNome);
        tilEmail.setError(eEmail);
        if (eNome != null || eEmail != null) return;
        vm.salvarDados(nome, email);
    }

    // ---------- Câmera ----------

    private void clicouCamera() {
        if (!requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            avisoComGaleria(getString(R.string.sem_camera));
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamera();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // Explica antes de pedir de novo.
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.permissao_camera_titulo)
                    .setMessage(R.string.permissao_camera_explicacao)
                    .setNegativeButton(R.string.usar_galeria, (d, w) -> abrirGaleria())
                    .setPositiveButton(R.string.permitir, (d, w) ->
                            permissaoCamera.launch(Manifest.permission.CAMERA))
                    .show();
        } else {
            permissaoCamera.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamera() {
        fotoTemp = vm.novoArquivoFoto();
        Uri uri = FileProvider.getUriForFile(requireContext(),
                requireContext().getPackageName() + ".fileprovider", fotoTemp);
        try {
            tirarFoto.launch(uri);
        } catch (ActivityNotFoundException e) {
            fotoTemp = null;
            avisoComGaleria(getString(R.string.sem_app_camera));
        }
    }

    private void cameraNegada() {
        boolean negadaDeVez = !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA);
        Snackbar s = Snackbar.make(requireView(), R.string.camera_negada, Snackbar.LENGTH_LONG)
                .setAnchorView(((MainActivity) requireActivity()).getNav());
        if (negadaDeVez) {
            s.setAction(R.string.configuracoes, x -> startActivity(new Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().getPackageName(), null))));
        } else {
            s.setAction(R.string.usar_galeria, x -> abrirGaleria());
        }
        s.show();
    }

    // ---------- Galeria ----------

    private void abrirGaleria() {
        escolherFoto.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    // ---------- Troca de senha ----------

    private void abrirDialogSenha() {
        View conteudo = getLayoutInflater().inflate(R.layout.dialog_trocar_senha, null);
        TextInputEditText atual = conteudo.findViewById(R.id.et_senha_atual);
        TextInputEditText nova = conteudo.findViewById(R.id.et_nova_senha);
        TextInputEditText conf = conteudo.findViewById(R.id.et_confirmar);
        TextInputLayout tilAtual = conteudo.findViewById(R.id.til_senha_atual);
        TextInputLayout tilNova = conteudo.findViewById(R.id.til_nova_senha);
        TextInputLayout tilConf = conteudo.findViewById(R.id.til_confirmar);

        dialogSenha = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.trocar_senha)
                .setView(conteudo)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.salvar, null) // tratado abaixo para não fechar com erro
                .create();
        dialogSenha.setOnShowListener(d -> dialogSenha.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(x -> {
                    String a = atual.getText() == null ? "" : atual.getText().toString();
                    String n = nova.getText() == null ? "" : nova.getText().toString();
                    String c = conf.getText() == null ? "" : conf.getText().toString();
                    String eA = a.isEmpty() ? getString(R.string.erro_senha_atual) : null;
                    String eN = Validacao.erroSenha(n);
                    String eC = Validacao.erroConfirmacao(n, c);
                    tilAtual.setError(eA);
                    tilNova.setError(eN);
                    tilConf.setError(eC);
                    if (eA == null && eN == null && eC == null) vm.trocarSenha(a, n, c);
                }));
        dialogSenha.show();
    }

    // ---------- Mensagens ----------

    private void aviso(String msg) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT)
                .setAnchorView(((MainActivity) requireActivity()).getNav()).show();
    }

    private void avisoComGaleria(String msg) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG)
                .setAnchorView(((MainActivity) requireActivity()).getNav())
                .setAction(R.string.usar_galeria, x -> abrirGaleria()).show();
    }
}
