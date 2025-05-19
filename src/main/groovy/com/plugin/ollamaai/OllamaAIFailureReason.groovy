package com.plugin.ollamaai

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason

/**
 * This enum lists the known reasons the OllamaAI plugin might fail.
 */
enum OllamaAIFailureReason implements FailureReason {
    ApiError,              // Error making API calls to Ollama
    InvalidModelError,     // Invalid model specified
    InvalidPromptError,    // Invalid or empty prompt
    TokenLimitError,       // Token limit exceeded
    ConnectionError,       // Error connecting to Ollama server
    InvalidResponseError,  // Invalid response from API
    ServerConfigError      // Error with server configuration
}