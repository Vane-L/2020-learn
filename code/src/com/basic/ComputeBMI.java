package com.basic;

/**
 * @Author: wenhongliang
 */
public class ComputeBMI {
    public static void main(String[] args) {
        double weight = 153;
        double height = 86;
        final double KG_PER_POUND = 0.45359237;
        final double METER_PER_INCH = 0.0254;

        double weightInKilograms = weight * KG_PER_POUND;
        double heightInMeters = height * METER_PER_INCH;
        double bmi = weightInKilograms / (heightInMeters * heightInMeters);

        System.out.println("BMI is " + bmi);
        if (bmi < 18.5) {
            System.out.println("Underweight");
        } else if (bmi < 25) {
            System.out.println("Normal");
        } else if (bmi < 30) {
            System.out.println("Overweight");
        } else {
            System.out.println("Obees");
        }
    }
}
