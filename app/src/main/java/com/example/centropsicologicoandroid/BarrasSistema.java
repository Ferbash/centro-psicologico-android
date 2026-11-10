package com.example.centropsicologicoandroid;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Respeta las barras del sistema y el teclado sumando los insets al padding original. */
final class BarrasSistema {

    private BarrasSistema() { }

    static void aplicar(View raiz) {
        final int l = raiz.getPaddingLeft();
        final int t = raiz.getPaddingTop();
        final int r = raiz.getPaddingRight();
        final int b = raiz.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets s = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(l + s.left, t + s.top, r + s.right, b + s.bottom);
            return insets;
        });
    }
}
