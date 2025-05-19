package com.plugin.ollamaai

/**
 * Utility class for the OllamaAI plugin.
 */
class Util {
    /**
     * Truncates a string to a maximum length.
     *
     * @param text The text to truncate
     * @param maxLength The maximum length
     * @return The truncated text
     */
    static String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text
        }
        return text.substring(0, maxLength) + "..."
    }
    
    /**
     * Validates that a string is not null or empty.
     *
     * @param value The string to validate
     * @param name The name of the parameter for error messages
     * @throws IllegalArgumentException If the string is null or empty
     */
    static void validateNotEmpty(String value, String name) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty")
        }
    }
    
    /**
     * Formats the response as JSON.
     *
     * @param text The response text
     * @return A JSON string containing the response
     */
    static String formatJsonResponse(String text) {
        return """{"response": ${groovy.json.JsonOutput.toJson(text)}}"""
    }
}