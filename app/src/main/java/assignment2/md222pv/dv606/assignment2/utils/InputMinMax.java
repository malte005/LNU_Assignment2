package assignment2.md222pv.dv606.assignment2.utils;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by damma on 01.09.2016.
 */
public class InputMinMax implements InputFilter {

    private int min, max;

    public InputMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public InputMinMax(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            int input = Integer.parseInt(dest.toString() + source.toString());
            if (isInRange(min, max, input)) {
                return null;
            }
        } catch (NumberFormatException nfe) {
        }

        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
