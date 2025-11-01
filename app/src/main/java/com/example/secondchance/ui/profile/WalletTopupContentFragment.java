package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.secondchance.R;

public class WalletTopupContentFragment extends Fragment {
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_wallet_topup_content, container, false);
  }
  
  @Override
  public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(v, savedInstanceState);
    TextView tvBalance = v.findViewById(R.id.tv_balance);
    TextView tvReceived = v.findViewById(R.id.tv_received);
    EditText etName = v.findViewById(R.id.et_name);
    EditText etBank = v.findViewById(R.id.et_bank);
    EditText etNumber = v.findViewById(R.id.et_number);
    
    // demo
    tvBalance.setText("170000");
    tvReceived.setText("170000");
    etName.setText("Cá biết bay");
    etBank.setText("Momo");
    etNumber.setText("0987 999 987 678");
  }
}
