package com.example.secondchance.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BankProvider {
    public static List<String> getSupportedBanks() {
        List<String> banks = new ArrayList<>();
        banks.add("Vietcombank");
        banks.add("Momo");
//        banks.add("Vietinbank");
//        banks.add("BIDV");
//        banks.add("Agribank");
//        banks.add("Techcombank");
//        banks.add("VPBank");
//        banks.add("MBBank");
//        banks.add("ACB");
//        banks.add("Sacombank");
//        banks.add("HDBank");
//        banks.add("TPBank");
//        banks.add("OceanBank");
//        banks.add("VIB");
//        banks.add("SHB");
//        banks.add("Eximbank");
//        banks.add("MSB");
//        banks.add("ZaloPay");
        Collections.sort(banks);
        return banks;
    }
}
