package com.codersworld.awesalibs.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationMethod {
    static Matcher m;
    static String emailExpression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
    static Pattern emailPattern = Pattern.compile(emailExpression, Pattern.CASE_INSENSITIVE);
    static String passwordExpression ="((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*\\W).{8,20})";
    static Pattern passwordPattern= Pattern.compile(passwordExpression);

    public static boolean emailValidation(String s)
    {
        if( s == null)
        {
            return false;
        }
        else
        {
            m = emailPattern.matcher(s);
            return m.matches();
        }
    }

    public static boolean passwordValidation(String s)
    {
        if( s == null)
        {
            return false;
        }
        else
        {
            m = passwordPattern.matcher(s);
            return m.matches();
        }
    }

    public static boolean emailValidation2(String s)
    {
        m = emailPattern.matcher(s);
        return m.matches();
    }
    public static boolean isNumberValid(String email)
    {
        boolean isValid = false;
        String expression = "^[789]\\d{9}$";
        CharSequence inputStr = email;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches())
        {
            isValid = true;
        }
        return isValid;
    }

}
