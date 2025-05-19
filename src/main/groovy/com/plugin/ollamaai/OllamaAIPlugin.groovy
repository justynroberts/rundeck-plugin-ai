package com.plugin.ollamaai

import com.dtolabs.rundeck.core.common.INodeEntry
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.step.NodeStepPlugin
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty
import com.dtolabs.rundeck.plugins.descriptions.RenderingOption
import com.dtolabs.rundeck.plugins.descriptions.RenderingOptions
import com.dtolabs.rundeck.plugins.descriptions.SelectValues
import groovy.json.JsonOutput
import com.dtolabs.rundeck.core.execution.ExecutionListener

import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.GROUPING
import static com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants.GROUP_NAME

/**
 * OllamaAIPlugin is a Rundeck plugin that connects to Ollama to generate AI responses.
 */
@Plugin(name = OllamaAIConstants.PLUGIN_NAME, service = ServiceNameConstants.WorkflowNodeStep)
@PluginDescription(title = OllamaAIConstants.PLUGIN_TITLE, description = OllamaAIConstants.PLUGIN_DESCRIPTION)
class OllamaAIPlugin implements NodeStepPlugin {
    Map<String, String> meta = Collections.singletonMap("content-data-type", "application/json")
    OllamaAIApi ollamaApi

    /**
     * Ollama model selection
     */
    @PluginProperty(
        title = "Ollama Model",
        description = "Select the Ollama model to use or enter a custom model name.",
        required = true,
        defaultValue = OllamaAIConstants.DEFAULT_OLLAMA_MODEL
    )
    @SelectValues(values = [
        "llama3", "llama3:8b", "llama3:70b",
        "llama2", "llama2:13b", "llama2:70b",
        "mistral", "mixtral", "phi3",
        "gemma", "codellama", "orca-mini",
        "vicuna", "wizard-vicuna", "stable-diffusion",
        "custom-model"
    ])
    @RenderingOption(key = GROUP_NAME, value = "Model Configuration")
    String ollamaModel

    /**
     * Custom model name
     */
    @PluginProperty(
        title = "Custom Model Name",
        description = "If you selected 'custom-model' above, enter the name of your custom model here.",
        required = false
    )
    @RenderingOption(key = GROUP_NAME, value = "Model Configuration")
    String customModelName

    /**
     * Ollama server URL
     */
    @PluginProperty(
        title = "Ollama Server URL",
        description = "The URL of your Ollama server (default: http://localhost:11434).",
        required = false,
        defaultValue = "http://localhost:11434"
    )
    @RenderingOption(key = GROUP_NAME, value = "Server Configuration")
    String ollamaServerUrl

    /**
     * Prompt for the AI
     */
    @PluginProperty(
        title = "Prompt",
        description = "The prompt to send to the AI model.",
        required = true
    )
    @RenderingOptions([
        @RenderingOption(
            key = StringRenderingConstants.DISPLAY_TYPE_KEY,
            value = "MULTI_LINE"
        ),
        @RenderingOption(
            key = StringRenderingConstants.GROUP_NAME,
            value = "Prompt Configuration"
        )
    ])
    String prompt

    /**
     * Log filter output
     */
    @PluginProperty(
        title = "Log Filter Output",
        description = "Optional: Log filter output to include in the prompt. This will be appended to the prompt.",
        required = false
    )
    @RenderingOptions([
        @RenderingOption(
            key = StringRenderingConstants.DISPLAY_TYPE_KEY,
            value = "MULTI_LINE"
        ),
        @RenderingOption(
            key = StringRenderingConstants.GROUP_NAME,
            value = "Prompt Configuration"
        )
    ])
    String logFilterOutput

    /**
     * Temperature parameter
     */
    @PluginProperty(
        title = "Temperature",
        description = "Controls randomness. Lower values are more deterministic, higher values are more creative. Range: 0.0 to 1.0",
        defaultValue = OllamaAIConstants.DEFAULT_TEMPERATURE,
        required = false
    )
    @RenderingOption(key = GROUP_NAME, value = "Model Parameters")
    String temperature

    /**
     * Max tokens parameter
     */
    @PluginProperty(
        title = "Max Tokens",
        description = "The maximum number of tokens to generate in the response.",
        defaultValue = OllamaAIConstants.DEFAULT_MAX_TOKENS,
        required = false
    )
    @RenderingOption(key = GROUP_NAME, value = "Model Parameters")
    String maxTokens

    /**
     * Top P parameter
     */
    @PluginProperty(
        title = "Top P",
        description = "Controls diversity via nucleus sampling. Range: 0.0 to 1.0",
        defaultValue = OllamaAIConstants.DEFAULT_TOP_P,
        required = false
    )
    @RenderingOption(key = GROUP_NAME, value = "Model Parameters")
    @RenderingOption(key = GROUPING, value = "secondary")
    String topP

    /**
     * Execute the plugin step
     */
    @Override
    void executeNodeStep(final PluginStepContext context,
                         final Map<String, Object> configuration,
                         final INodeEntry entry) throws NodeStepException {

        ExecutionListener logger = context.getExecutionContext().getExecutionListener()
        logger.log(3, "Starting OllamaAI plugin execution")
        
        // Determine which model to use
        String model = ollamaModel
        if (model == "custom-model" && customModelName) {
            model = customModelName
        }
        
        // Parse parameters
        Double temperatureValue
        Integer maxTokensValue
        Double topPValue
        
        try {
            temperatureValue = Double.parseDouble(temperature)
            maxTokensValue = Integer.parseInt(maxTokens)
            topPValue = Double.parseDouble(topP)
            
            // Validate parameter ranges
            if (temperatureValue < 0.0 || temperatureValue > 1.0) {
                throw new IllegalArgumentException("Temperature must be between 0.0 and 1.0")
            }
            if (topPValue < 0.0 || topPValue > 1.0) {
                throw new IllegalArgumentException("Top P must be between 0.0 and 1.0")
            }
        } catch (NumberFormatException e) {
            throw new NodeStepException(
                "Invalid parameter format: " + e.getMessage(),
                OllamaAIFailureReason.ApiError,
                entry.getNodename()
            )
        }
        
        // Combine prompt with log filter output if provided
        String fullPrompt = prompt
        if (logFilterOutput && !logFilterOutput.trim().isEmpty()) {
            fullPrompt += "\n\nLog Output:\n" + logFilterOutput
        }
        
        // Initialize API client
        try {
            // Create server URL
            String serverUrl = ollamaServerUrl
            if (!serverUrl.endsWith("/api")) {
                serverUrl = serverUrl + "/api"
            }
            
            // Create API client
            if (ollamaApi == null) {
                ollamaApi = new OllamaAIApi(model, serverUrl)
            }
            
            // Generate AI response
            Map<String, Object> result = ollamaApi.generateResponse(
                fullPrompt,
                temperatureValue,
                maxTokensValue,
                topPValue
            )
            
            // Format the response as JSON
            String jsonResponse = Util.formatJsonResponse(result.text)
            
            // Add the result to the output context
            context.getExecutionContext().getOutputContext().addOutput("ollamaai", "response", jsonResponse)
            context.getExecutionContext().getOutputContext().addOutput("ollamaai", "metadata", JsonOutput.toJson(result.metadata))
            context.getExecutionContext().getOutputContext().addOutput("ollamaai", "model", result.model)
            
            // Log the result
           // logger.log(2, "AI response generated successfully")
            logger.log(2, jsonResponse, meta)
            
        } catch (Exception e) {
            throw new NodeStepException(
                "Failed to generate AI response: " + e.getMessage(),
                OllamaAIFailureReason.ApiError,
                entry.getNodename()
            )
        }
    }
}