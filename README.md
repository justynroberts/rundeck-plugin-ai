# 🤖 Rundeck Plugin - Ollama Workflow step

> A workflow step plugin that integrates [Ollama](https://ollama.ai) AI models with Rundeck for intelligent automation and analysis. **This plugin is community supported**
> [![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
> [![Rundeck](https://img.shields.io/badge/Rundeck-3.x+-orange.svg)](https://www.rundeck.com/)
> [![Java](https://img.shields.io/badge/Java-11+-green.svg)](https://www.java.com/)

## 🌟 Features

* 🔌 Connect to local or remote Ollama instances
* 🤖 Support for custom models
* 💬 Configure custom prompts
* 📋 Receive responses in JSON format
* ⚙️ Customize model parameters (temperature, max tokens, etc.)

## 📋 Table of Contents

* [Installation](#-installation)
    * [Installing Ollama](#installing-ollama)
    * [Installing the Plugin](#installing-the-plugin)
        * [UI Installation](#ui-installation)
        * [Manual Installation](#manual-installation)
* [Configuration](#-configuration)
* [Usage](#-usage)
* [Available Models](#-available-models)
* [Parameters](#-parameters)

## 🔧 Installation

### Installing Ollama

1. Install Ollama on your Linux system:

``` bash
curl -fsSL [https://ollama.com/install.sh](https://ollama.com/install.sh) | sh
```

2. Start the Ollama service:

``` bash
systemctl start ollama
```

3. Verify the installation:

``` bash
ollama --version
```

4. Pull your first model:

``` bash
ollama pull llama3
```

For more information, visit the [Ollama documentation](https://github.com/ollama/ollama).

### Installing the Plugin

#### UI Installation

1. Log in to your Rundeck instance as an admin
2. Navigate to System Menu (gear icon) → System Configuration → Plugins
3. Click on the "Upload Plugin" button
4. Either:
    * Drag and drop the `.jar` file into the upload area
    * Click "Choose File" and select the `.jar` file
5. Click "Upload" to install the plugin
6. Restart Rundeck when prompted

#### Manual Installation

1. Download the latest release from the [releases page](https://github.com/yourusername/rundeck-ollama-plugin/releases)
2. Copy the `.jar` file to your Rundeck plugins directory:

``` bash
sudo cp ollama-ai-0.1.0.jar /var/lib/rundeck/libext/
```

3. Restart Rundeck:

``` bash
sudo systemctl restart rundeckd
```

## ⚙️ Configuration

### Ollama Server Setup

By default, the plugin connects to `[http://localhost:11434](http://localhost:11434)`. You can configure a different server URL in the workflow step configuration.

### Requirements

* Rundeck 3.x or later
* Java 11 or later
* A running Ollama instance

## 🚀 Usage

### Basic Usage

1. Add the "OllamaAI - AI Text Generation" step to your workflow
2. Configure the following:
    * Choose your model (or select "custom-model")
    * Enter your prompt
    * Adjust additional parameters as needed

### Sample Job Definition

``` yaml
- defaultTab: nodes
  description: Example job using OllamaAI plugin
  executionEnabled: true
  id: ollama-example
  name: OllamaAI Example
  nodeFilterEditable: false
  plugins:
    ExecutionLifecycle: null
  scheduleEnabled: true
  sequence:
    commands:
    - configuration:
        model: llama3
        prompt: "Analyze this system information:"
        serverUrl: [http://localhost:11434](http://localhost:11434)
        temperature: "0.7"
        maxTokens: "1000"
      type: ollamaai-step
    - configuration:
        content: "${data.ollamaai.response}"
      type: log-data
    keepgoing: false
    strategy: node-first
  uuid: ollama-example
```

### Response Format

``` json
{
  "response": "The AI-generated response text",
  "metadata": {...},
  "model": "llama3"
}
```

Access in subsequent steps:

```
${data.ollamaai.response}
${data.ollamaai.metadata}
${data.ollamaai.model}
```

## 🤖 Available Models

| Model Category | Available Models |
| -------------- | ---------------- |
| **LLaMA Family** | `llama3`, `llama3:8b`, `llama3:70b`, `llama2`, `llama2:13b`, `llama2:70b` |
| **Mistral Series** | `mistral`, `mixtral` |
| **Specialized** | `phi3`, `gemma`, `codellama` |
| **Compact** | `orca-mini` |
| **Other** | `vicuna`, `wizard-vicuna`, `stable-diffusion` |

> 💡 Use any model from your Ollama instance by selecting "custom-model"

## ⚙️ Parameters

| Parameter | Description | Default |
| --------- | ----------- | ------- |
| `Ollama Model` | The Ollama model to use | `llama3` |
| `Custom Model Name` | Name of custom model | - |
| `Ollama Server URL` | Server URL | `[http://localhost:11434](http://localhost:11434)` |
| `Prompt` | AI prompt | - |
| `Temperature` | Randomness (0.0-1.0) | `0.7` |
| `Max Tokens` | Token generation limit | `1000` |
| `Top P` | Nucleus sampling control | `1.0` |

- - -

📝 **License**: MIT License
