package br.ufms.pokecalcrb.ui.historico;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import br.ufms.pokecalcrb.R;
import br.ufms.pokecalcrb.ui.main.MainActivity;

/** Aba 2: lista dos cálculos salvos pelo usuário logado. */
public class HistoricoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historico, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        HistoricoViewModel vm = new ViewModelProvider(this).get(HistoricoViewModel.class);
        RecyclerView lista = v.findViewById(R.id.lista);
        TextView vazio = v.findViewById(R.id.tv_vazio);

        HistoricoAdapter adapter = new HistoricoAdapter(c ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.apagar_titulo)
                        .setMessage(getString(R.string.apagar_mensagem, c.atacante, c.defensor))
                        .setNegativeButton(R.string.cancelar, null)
                        .setPositiveButton(R.string.apagar, (d, w) -> {
                            vm.apagar(c);
                            Snackbar.make(v, R.string.apagado, Snackbar.LENGTH_SHORT)
                                    .setAnchorView(((MainActivity) requireActivity()).getNav())
                                    .show();
                        })
                        .show());

        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);

        vm.getItens().observe(getViewLifecycleOwner(), itens -> {
            adapter.submitList(itens);
            boolean semItens = itens == null || itens.isEmpty();
            vazio.setVisibility(semItens ? View.VISIBLE : View.GONE);
            lista.setVisibility(semItens ? View.GONE : View.VISIBLE);
        });
    }
}
