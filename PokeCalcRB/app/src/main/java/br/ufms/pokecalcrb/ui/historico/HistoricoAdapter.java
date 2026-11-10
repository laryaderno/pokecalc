package br.ufms.pokecalcrb.ui.historico;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;

import br.ufms.pokecalcrb.R;
import br.ufms.pokecalcrb.data.db.Calculo;

public class HistoricoAdapter extends ListAdapter<Calculo, HistoricoAdapter.VH> {

    public interface AoApagar {
        void apagar(Calculo c);
    }

    private final AoApagar aoApagar;

    public HistoricoAdapter(AoApagar aoApagar) {
        super(DIFF);
        this.aoApagar = aoApagar;
    }

    private static final DiffUtil.ItemCallback<Calculo> DIFF = new DiffUtil.ItemCallback<Calculo>() {
        @Override
        public boolean areItemsTheSame(@NonNull Calculo a, @NonNull Calculo b) {
            return a.id == b.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Calculo a, @NonNull Calculo b) {
            return a.data == b.data && a.danoMin == b.danoMin && a.danoMax == b.danoMax;
        }
    };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calculo, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Calculo c = getItem(position);
        android.content.Context ctx = h.itemView.getContext();
        h.titulo.setText(ctx.getString(R.string.item_titulo, c.atacante, c.defensor));
        h.detalhe.setText(ctx.getString(R.string.item_detalhe, c.golpe, c.nivelAtq, c.nivelDef));
        h.dano.setText(ctx.getString(R.string.item_dano, c.danoMin, c.danoMax, c.critMin, c.critMax, c.hpDefensor));
        h.efetividade.setText(c.efetividade);
        Date d = new Date(c.data);
        h.data.setText(DateFormat.getDateFormat(ctx).format(d) + " " + DateFormat.getTimeFormat(ctx).format(d));
        h.apagar.setContentDescription(ctx.getString(R.string.desc_apagar, c.atacante, c.defensor));
        h.apagar.setOnClickListener(v -> aoApagar.apagar(c));
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView titulo, detalhe, dano, efetividade, data;
        final ImageButton apagar;

        VH(View v) {
            super(v);
            titulo = v.findViewById(R.id.tv_titulo);
            detalhe = v.findViewById(R.id.tv_detalhe);
            dano = v.findViewById(R.id.tv_dano);
            efetividade = v.findViewById(R.id.tv_efetividade);
            data = v.findViewById(R.id.tv_data);
            apagar = v.findViewById(R.id.btn_apagar);
        }
    }
}
