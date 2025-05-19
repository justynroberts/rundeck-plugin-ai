package com.plugin.ollamaai

/**
 * Constants for the OllamaAI plugin.
 */
class OllamaAIConstants {
    // API URLs
    public static final String OLLAMA_API_URL = "http://localhost:11434/api"
    
    // Default models
    public static final String DEFAULT_OLLAMA_MODEL = "llama3"
    
    // Default parameters
    public static final String DEFAULT_TEMPERATURE = "0.7"
    public static final String DEFAULT_MAX_TOKENS = "1000"
    public static final String DEFAULT_TOP_P = "1.0"
    
    // Provider types
    public static final String PROVIDER_OLLAMA = "ollama"
    
    // Ollama models - these can be customized based on what's available in your Ollama instance
    public static final List<String> OLLAMA_MODELS = [
        "llama3",
        "llama3:8b",
        "llama3:70b",
        "mistral",
        "mixtral",
        "phi3",
        "gemma",
        "codellama",
        "llama2",
        "llama2:13b",
        "llama2:70b",
        "orca-mini",
        "vicuna",
        "wizard-vicuna",
        "stable-diffusion",
        "custom-model" // Placeholder for custom models
    ]
    
    // Default prompt template
    public static final String DEFAULT_PROMPT_TEMPLATE =
        "Analyze the following information and provide insights:\n\n{content}"
    
    // Error messages
    public static final String ERROR_INVALID_MODEL = "Invalid model specified for Ollama."
    public static final String ERROR_API_CALL = "Failed to call API: %s"
    public static final String ERROR_CONNECTION = "Failed to connect to Ollama server: %s"
    
    // Plugin information
    public static final String PLUGIN_NAME = "ollamaai-plugin"
    public static final String PLUGIN_TITLE = "OllamaAI - AI Text Generation"
    public static final String PLUGIN_DESCRIPTION = "Connect to Ollama to generate AI responses based on a configurable prompt."
}