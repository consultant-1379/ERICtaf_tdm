package com.ericsson.cifwk.tdm.api.model;

/**
 * Created by egergle on 06/10/2017.
 */
public enum Month {
    JANUARY(1, "January"),
    FEBRUARY(2, "February"),
    MARCH(3, "March"),
    APRIL(4, "April"),
    MAY(5, "May"),
    JUNE(6, "June"),
    JULY(7, "July"),
    AUGUST(8, "August"),
    SEPTEMBER(9, "September"),
    OCTOBER(10, "October"),
    NOVEMBER(11, "November"),
    DECEMBER(12, "December");

    private int id;
    private String name;

    Month(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static String getMonthName(String value) {
        String result = null;
        int id = Integer.parseInt(value);
        for (Month month : Month.values()) {
            if (month.getId() == id) {
                result = month.getName();
                break;
            }
        }
        return result;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
