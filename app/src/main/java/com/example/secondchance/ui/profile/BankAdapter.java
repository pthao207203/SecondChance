package com.example.secondchance.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.example.secondchance.R;
import java.util.List;

public class BankAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> banks;

    public BankAdapter(Context context, List<String> banks) {
        super(context, R.layout.item_bank_dropdown, banks);
        this.context = context;
        this.banks = banks;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_bank_dropdown, parent, false);
        }

        TextView textView = (TextView) convertView;

        String item = getItem(position);
        if (item != null) {
            textView.setText(item);
        }

        // Kiểm tra nếu là item cuối cùng
        if (position == getCount() - 1) {
             textView.setBackgroundResource(R.drawable.bg_dropdown_item_last);
        } else {
            // Các item khác giữ nguyên background có viền dưới
            textView.setBackgroundResource(R.drawable.bg_bottom_border_light_active);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return super.getItem(position);
    }
}
