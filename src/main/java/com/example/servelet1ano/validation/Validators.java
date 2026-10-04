package com.example.servelet1ano.validation;

import java.util.regex.Pattern;

/**
 * Classe utilitaria com as validacoes de entrada do sistema (CPF, CNPJ, celular, CEP e email)
 * Cada metodo devolve true se o valor for valido e false caso contrario
 */
public class Validators {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern MOBILE_PHONE = Pattern.compile("^[1-9]{2}9\\d{8}$");

    private static final Pattern CEP = Pattern.compile("^\\d{8}$");

    private static final Pattern CPF = Pattern.compile("^\\d{11}$");

    private static final Pattern CPF_REPEATED = Pattern.compile("^(\\d)\\1{10}$");

    private static final Pattern CNPJ = Pattern.compile("^[A-Z0-9]{12}\\d{2}$");

    /**
     * Valida um email pelo formato
     * @param email O email a validar
     * @return true se for um email valido
     */
    public static boolean isEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL.matcher(email).matches();
    }

    /**
     * Valida um celular com DDD (so os digitos, sem parenteses ou traco)
     * Aceita so celular (com o nono digito 9), nao valida telefone fixo
     * @param phone O numero a validar
     * @return true se for um celular valido
     */
    public static boolean isMobilePhone(String phone) {
        if (phone == null) {
            return false;
        }
        String digits = phone.replaceAll("\\D", "");
        return MOBILE_PHONE.matcher(digits).matches();
    }

    /**
     * Valida um CEP (so os digitos)
     * @param cep O CEP a validar
     * @return true se for um CEP valido
     */
    public static boolean isCep(String cep) {
        if (cep == null) {
            return false;
        }
        String digits = cep.replaceAll("\\D", "");
        return CEP.matcher(digits).matches();
    }

    /**
     * Valida um CPF: confere o formato (11 digitos) e os 2 digitos verificadores (modulo 11)
     * @param cpf O CPF a validar (aceita com ou sem pontuacao)
     * @return true se for um CPF valido
     */
    public static boolean isCpf(String cpf) {
        if (cpf == null) {
            return false;
        }

        String digits = cpf.replaceAll("\\D", "");

        if (!CPF.matcher(digits).matches() || CPF_REPEATED.matcher(digits).matches()) {
            return false;
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (digits.charAt(i) - '0') * (10 - i);
        }
        int remainder = sum % 11;
        int dv1 = (remainder < 2) ? 0 : 11 - remainder;

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (digits.charAt(i) - '0') * (11 - i);
        }
        remainder = sum % 11;
        int dv2 = (remainder < 2) ? 0 : 11 - remainder;

        return (digits.charAt(9) - '0') == dv1 && (digits.charAt(10) - '0') == dv2;
    }

    /**
     * Valida um CNPJ alfanumerico (formato valido desde 2026): confere o formato
     * (12 caracteres alfanumericos + 2 digitos verificadores) e os DVs pelo modulo 11.
     * Tambem aceita CNPJ antigo so numerico, porque numero é um caso particular do alfanumerico.
     * @param cnpj O CNPJ a validar (aceita com ou sem pontuacao)
     * @return true se for um CNPJ valido
     */
    public static boolean isCnpj(String cnpj) {
        if (cnpj == null) {
            return false;
        }

        String cleaned = cnpj.replaceAll("[^A-Za-z0-9]", "").toUpperCase();

        if (!CNPJ.matcher(cleaned).matches()) {
            return false;
        }

        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += charValue(cleaned.charAt(i)) * weights1[i];
        }
        int remainder = sum % 11;
        int dv1 = (remainder < 2) ? 0 : 11 - remainder;

        String base = cleaned.substring(0, 12) + dv1;
        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += charValue(base.charAt(i)) * weights2[i];
        }
        remainder = sum % 11;
        int dv2 = (remainder < 2) ? 0 : 11 - remainder;

        return (cleaned.charAt(12) - '0') == dv1 && (cleaned.charAt(13) - '0') == dv2;
    }

    /**
     * Converte um caractere do CNPJ no seu valor numerico (regra oficial: ASCII - 48)
     * Numeros 0-9 viram 0-9; letras A-Z viram 17-42
     * @param c O caractere
     * @return O valor numerico do caractere
     */
    private static int charValue(char c) {
        return c - 48;
    }

}