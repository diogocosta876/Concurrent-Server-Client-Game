package com.example.assignmentdistributed;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class TokenGenerator {

    private static final String SHA256_ALGORITHM = "SHA-256";

    public static String generateToken(String username, String password) {
        try {
            // Concatenate the username and password
            String input = username + password;

            // Create an instance of the hashing algorithm (e.g., SHA-256)
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);

            // Generate the hash value
            byte[] hash = digest.digest(input.getBytes());

            // Convert the hash value to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // Set the token as the hexadecimal string
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String generateRandomToken() {
        // Generate a random UUID
        UUID uuid = UUID.randomUUID();

        // Convert the UUID to a string and remove hyphens
        return uuid.toString().replaceAll("-", "");
    }

}

