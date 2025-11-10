package com.example.secondchance.data;

import com.example.secondchance.data.model.RefundRequest;
import java.util.ArrayList;
import java.util.List;

public class RefundRepository {
    private static RefundRepository instance;
    private final List<RefundRequest> refunds = new ArrayList<>();

    private RefundRepository() {}

    public static synchronized RefundRepository getInstance() {
        if (instance == null) instance = new RefundRepository();
        return instance;
    }

    public void addRefund(RefundRequest r) {
        refunds.add(0, r);
    }
    public List<RefundRequest> getRefunds() {
        return refunds;
    }

}
