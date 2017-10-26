package com.jthink.bill.dto;

/**
 * JThink@JThink
 *
 * @author JThink
 * @version 0.0.1
 * @desc 价格区间dto
 * @date 2016-08-24 18:31:48
 */
public class Interval {

    private int min;
    private int max;

    public Interval(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public Interval setMin(int min) {
        this.min = min;
        return this;
    }

    public int getMax() {
        return max;
    }

    public Interval setMax(int max) {
        this.max = max;
        return this;
    }
}
