package com.plugin.ollamaai

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.IOException
import java.util.Map
import java.util.HashMap
import java.util.concurrent.TimeUnit

/**
 * OllamaAIApi handles API calls to Ollama for generating AI responses.
 */
class OllamaAIApi {
    private String model
    private String baseUrl
    
    /**
     * Constructor for OllamaAIApi
     *
     * @param model The model to use
     * @param serverUrl Optional custom server URL
     */
    OllamaAIApi(String model, String serverUrl = null) {
        this.model = model
        
        // Set the base URL
        this.baseUrl = serverUrl ?: OllamaAIConstants.OLLAMA_API_URL
        
        // Validate the model
        validateModel(model)
    }
    
    /**
     * Validate the model
     *
     * @param model The model to validate
     */
    private void validateModel(String model) {
        // For Ollama, we'll be flexible since users can add custom models
        // We'll just check if it's in our predefined list or assume it's a custom model
        if (!OllamaAIConstants.OLLAMA_MODELS.contains(model) && !model.contains(":")) {
            // If it's not in our list and doesn't have a colon (which would indicate a custom model tag),
            // we'll warn but not throw an error
            println "Warning: Model '$model' is not in the predefined list of Ollama models. Assuming it's a custom model."
        }
    }
    
    /**
     * Generate a response from the AI model
     *
     * @param prompt The prompt to send to the AI
     * @param temperature The temperature parameter (0.0 to 1.0)
     * @param maxTokens The maximum number of tokens to generate
     * @param topP The top_p parameter (0.0 to 1.0)
     * @return Map containing the AI response and metadata
     */
    Map<String, Object> generateResponse(
        String prompt,
        Double temperature,
        Integer maxTokens,
        Double topP
    ) throws IOException {
        // Validate prompt
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be empty")
        }
        
        // Build the request body
        String requestBody = buildOllamaRequestBody(prompt, temperature, maxTokens, topP)
        String endpoint = "/generate"
        
        // Build the full URL
        String url = baseUrl + endpoint
        
        // Create connection
        URL apiUrl = new URL(url)
        HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection()
        connection.setRequestMethod("POST")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setDoOutput(true)
        connection.setConnectTimeout(30000)
        connection.setReadTimeout(60000)
        
        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8")
            os.write(input, 0, input.length)
        }
        
        // Get response
        int responseCode = connection.getResponseCode()
        if (responseCode != 200) {
            throw new IOException(String.format(
                OllamaAIConstants.ERROR_API_CALL,
                "HTTP " + responseCode + " - " + connection.getResponseMessage()
            ))
        }
        
        // Read response
        StringBuilder response = new StringBuilder()
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine = null
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim())
            }
        }
        
        // Parse the response
        def jsonSlurper = new JsonSlurper()
        def result = jsonSlurper.parseText(response.toString())
        
        // Extract the response text
        String responseText = result.response
        Map<String, Object> metadata = new HashMap<>()
        metadata.put("model", model)
        
        if (result.total_duration != null) {
            metadata.put("total_duration", result.total_duration)
        }
        if (result.eval_count != null) {
            metadata.put("eval_count", result.eval_count)
        }
        if (result.eval_duration != null) {
            metadata.put("eval_duration", result.eval_duration)
        }
        
        // Format the result
        Map<String, Object> formattedResult = new HashMap<>()
        formattedResult.put("text", responseText)
        formattedResult.put("metadata", metadata)
        formattedResult.put("provider", OllamaAIConstants.PROVIDER_OLLAMA)
        formattedResult.put("model", model)
        formattedResult.put("prompt", prompt)
        
        return formattedResult
    }
    
    /**
     * Build the request body for Ollama API
     */
    private String buildOllamaRequestBody(
        String prompt,
        Double temperature,
        Integer maxTokens,
        Double topP
    ) {
        def requestMap = [
            model: model,
            prompt: prompt,
            stream: false,
            options: [
                temperature: temperature,
                num_predict: maxTokens,
                top_p: topP
            ]
        ]
        
        return JsonOutput.toJson(requestMap)
    }
}