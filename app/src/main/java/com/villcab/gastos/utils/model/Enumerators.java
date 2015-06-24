package com.villcab.gastos.utils.model;

import java.io.Serializable;

public class Enumerators implements Serializable {

    public static final class Status {
        public static int Delete = 2;
        public static int Enable = 1;
        public static int Disable = 0;
    }

    public static final class AccountType {
        public static int Google = 3;
        public static int Twitter = 2;
        public static int Facebook = 1;
        public static int Email = 0;
    }

}
