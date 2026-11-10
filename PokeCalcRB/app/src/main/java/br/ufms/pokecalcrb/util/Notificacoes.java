package br.ufms.pokecalcrb.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import br.ufms.pokecalcrb.R;
import br.ufms.pokecalcrb.ui.main.MainActivity;

/** Canal e envio das notificações do app. */
public final class Notificacoes {

    public static final String CANAL_HISTORICO = "historico";
    private static int proximoId = 1000;

    private Notificacoes() { }

    public static void criarCanal(Context context) {
        NotificationChannel canal = new NotificationChannel(CANAL_HISTORICO,
                context.getString(R.string.canal_historico_nome),
                NotificationManager.IMPORTANCE_DEFAULT);
        canal.setDescription(context.getString(R.string.canal_historico_desc));
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(canal);
    }

    /** true se o app pode mostrar notificações (permissão no Android 13+ e não desativadas pelo usuário). */
    public static boolean podeNotificar(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * Mostra "Cálculo salvo: Charizard -> Venusaur (52–61 HP)".
     * @return false se a permissão foi negada (o app segue normalmente).
     */
    @SuppressLint("MissingPermission") // a permissão é conferida em podeNotificar()
    public static boolean notificarCalculoSalvo(Context context, String texto) {
        if (!podeNotificar(context)) return false;

        Intent abrir = new Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_ABA, MainActivity.ABA_HISTORICO)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, abrir,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CANAL_HISTORICO)
                .setSmallIcon(R.drawable.ic_stat_calc)
                .setContentTitle(context.getString(R.string.notif_titulo))
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        try {
            NotificationManagerCompat.from(context).notify(proximoId++, b.build());
            return true;
        } catch (SecurityException e) {
            // Permissão revogada no meio do caminho: não derruba o app.
            return false;
        }
    }
}
