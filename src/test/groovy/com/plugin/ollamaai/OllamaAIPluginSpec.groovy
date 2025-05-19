package com.plugin.ollamaai

import com.dtolabs.rundeck.core.common.INodeEntry
import com.dtolabs.rundeck.core.common.NodeEntryImpl
import com.dtolabs.rundeck.core.execution.ExecutionContext
import com.dtolabs.rundeck.core.execution.ExecutionListener
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException
import com.dtolabs.rundeck.plugins.step.PluginStepContext
import spock.lang.Specification

class OllamaAIPluginSpec extends Specification {
    
    def "test execute with standard model"() {
        given:
        def plugin = new OllamaAIPlugin()
        plugin.ollamaModel = "llama3"
        plugin.prompt = "Test prompt"
        plugin.temperature = "0.7"
        plugin.maxTokens = "100"
        plugin.topP = "1.0"
        
        def mockContext = Mock(PluginStepContext)
        def mockExecutionContext = Mock(ExecutionContext)
        def mockOutputContext = Mock(ExecutionContext.OutputContext)
        def mockListener = Mock(ExecutionListener)
        def mockNode = new NodeEntryImpl("testnode")
        
        mockContext.getExecutionContext() >> mockExecutionContext
        mockExecutionContext.getOutputContext() >> mockOutputContext
        mockExecutionContext.getExecutionListener() >> mockListener
        
        // Mock the OllamaAIApi to avoid actual API calls
        def mockApi = Mock(OllamaAIApi)
        plugin.ollamaApi = mockApi
        
        when:
        plugin.executeNodeStep(mockContext, [:], mockNode)
        
        then:
        1 * mockApi.generateResponse("Test prompt", 0.7, 100, 1.0) >> [
            text: "This is a test response",
            metadata: [model: "llama3"],
            model: "llama3",
            prompt: "Test prompt"
        ]
        1 * mockOutputContext.addOutput("ollamaai", "response", '{"response": "This is a test response"}')
        1 * mockOutputContext.addOutput("ollamaai", "metadata", _)
        1 * mockOutputContext.addOutput("ollamaai", "model", "llama3")
        0 * _._  // No other interactions
    }
    
    def "test execute with custom model"() {
        given:
        def plugin = new OllamaAIPlugin()
        plugin.ollamaModel = "custom-model"
        plugin.customModelName = "my-fine-tuned-model"
        plugin.prompt = "Test prompt"
        plugin.temperature = "0.7"
        plugin.maxTokens = "100"
        plugin.topP = "1.0"
        
        def mockContext = Mock(PluginStepContext)
        def mockExecutionContext = Mock(ExecutionContext)
        def mockOutputContext = Mock(ExecutionContext.OutputContext)
        def mockListener = Mock(ExecutionListener)
        def mockNode = new NodeEntryImpl("testnode")
        
        mockContext.getExecutionContext() >> mockExecutionContext
        mockExecutionContext.getOutputContext() >> mockOutputContext
        mockExecutionContext.getExecutionListener() >> mockListener
        
        // Mock the OllamaAIApi to avoid actual API calls
        def mockApi = Mock(OllamaAIApi)
        plugin.ollamaApi = mockApi
        
        when:
        plugin.executeNodeStep(mockContext, [:], mockNode)
        
        then:
        1 * mockApi.generateResponse("Test prompt", 0.7, 100, 1.0) >> [
            text: "This is a test response from custom model",
            metadata: [model: "my-fine-tuned-model"],
            model: "my-fine-tuned-model",
            prompt: "Test prompt"
        ]
        1 * mockOutputContext.addOutput("ollamaai", "response", '{"response": "This is a test response from custom model"}')
        1 * mockOutputContext.addOutput("ollamaai", "metadata", _)
        1 * mockOutputContext.addOutput("ollamaai", "model", "my-fine-tuned-model")
        0 * _._  // No other interactions
    }
    
    def "test execute with log filter output"() {
        given:
        def plugin = new OllamaAIPlugin()
        plugin.ollamaModel = "llama3"
        plugin.prompt = "Analyze this log:"
        plugin.logFilterOutput = "ERROR: Connection refused"
        plugin.temperature = "0.7"
        plugin.maxTokens = "100"
        plugin.topP = "1.0"
        
        def mockContext = Mock(PluginStepContext)
        def mockExecutionContext = Mock(ExecutionContext)
        def mockOutputContext = Mock(ExecutionContext.OutputContext)
        def mockListener = Mock(ExecutionListener)
        def mockNode = new NodeEntryImpl("testnode")
        
        mockContext.getExecutionContext() >> mockExecutionContext
        mockExecutionContext.getOutputContext() >> mockOutputContext
        mockExecutionContext.getExecutionListener() >> mockListener
        
        // Mock the OllamaAIApi to avoid actual API calls
        def mockApi = Mock(OllamaAIApi)
        plugin.ollamaApi = mockApi
        
        when:
        plugin.executeNodeStep(mockContext, [:], mockNode)
        
        then:
        1 * mockApi.generateResponse("Analyze this log:\n\nLog Output:\nERROR: Connection refused", 0.7, 100, 1.0) >> [
            text: "The log shows a connection refused error, which typically indicates that the server is not accepting connections.",
            metadata: [model: "llama3"],
            model: "llama3",
            prompt: "Analyze this log:\n\nLog Output:\nERROR: Connection refused"
        ]
        1 * mockOutputContext.addOutput("ollamaai", "response", _)
        1 * mockOutputContext.addOutput("ollamaai", "metadata", _)
        1 * mockOutputContext.addOutput("ollamaai", "model", "llama3")
        0 * _._  // No other interactions
    }
    
    def "test invalid parameters"() {
        given:
        def plugin = new OllamaAIPlugin()
        plugin.ollamaModel = "llama3"
        plugin.prompt = "Test prompt"
        plugin.temperature = "2.0"  // Invalid temperature
        plugin.maxTokens = "100"
        plugin.topP = "1.0"
        
        def mockContext = Mock(PluginStepContext)
        def mockExecutionContext = Mock(ExecutionContext)
        def mockListener = Mock(ExecutionListener)
        def mockNode = new NodeEntryImpl("testnode")
        
        mockContext.getExecutionContext() >> mockExecutionContext
        mockExecutionContext.getExecutionListener() >> mockListener
        
        when:
        plugin.executeNodeStep(mockContext, [:], mockNode)
        
        then:
        def e = thrown(NodeStepException)
        e.failureReason == OllamaAIFailureReason.ApiError
        e.message.contains("Temperature must be between 0.0 and 1.0")
    }
}