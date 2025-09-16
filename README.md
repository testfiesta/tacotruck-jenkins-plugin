# TacoTruck Jenkins Plugin

A Jenkins plugin that provides build step integration with TacoTruck

## Installation

### From Jenkins Plugin Manager

1. Go to **Manage Jenkins** → **Manage Plugins**
2. Search for "TacoTruck" in the Available tab
3. Install the plugin and restart Jenkins

### Manual Installation

1. Download the latest `.hpi` file from the [releases page](https://github.com/testfiesta/tacotruck-jenkins-plugin/releases)
2. Go to **Manage Jenkins** → **Manage Plugins** → **Advanced**
3. Upload the `.hpi` file and restart Jenkins

## Configuration

### Global Configuration

Currently, no global configuration is required for this plugin.

### Job Configuration

#### Freestyle Jobs

1. Add a build step "TacoTruck Integration"
2. Configure the following parameters:
   - **Provider**: Select the provider from the dropdown (currently supported: `testfiesta`)
   - **Run Name**: A descriptive name for this integration step
   - **API URL**: The TacoTruck service endpoint URL
   - **Project**: (Optional) Project identifier
   - **Credentials**: Select appropriate credentials from the dropdown
   - **Results Path**: Path to the test results file
   - **Handle**: username or organization handle

#### Pipeline Jobs

Use the `tacotruck` step in your Jenkinsfile with the nodejs buildwrapper:

```groovy
pipeline {
    agent any
    tools { nodejs 'Node 20.x' }
    stages {
        stage('Submit Test Results') {
            steps {
                    tacotruck(
                        provider: 'testfiesta',
                        runName: 'My TacoTruck Run',
                        apiUrl: 'https://staging.api.testfiesta.com',
                        handle: 'TestHandle',
                        project: 'testProjectKey',
                        credentialsId: 'YOUR_CREDENTIALS_ID',
                        resultsPath: './test-results.xml'
                    )
            }
        }
    }
}
```

**Note**: The `tacotruck` step must be wrapped inside a `nodejs` buildwrapper block to ensure npm and npx are available in the PATH.

### Credentials Setup

This plugin supports both username/password and API token credentials:

1. Go to **Manage Jenkins** → **Manage Credentials**
2. Add credentials of type:
   - **Username with password** for basic authentication
   - **Secret text** for API token authentication

## Requirements

- Jenkins 2.479.3 or later
- Java 11 or later

## Development

### Building the Plugin

```bash
mvn clean package
```

### Running Tests

```bash
mvn test
```

### Local Development

```bash
mvn hpi:run
mvn hpi:run -Dport=5000 (running on port 5000)
```

This will start a Jenkins instance with the plugin loaded at `http://localhost:8080/jenkins`

## Issues and Support

Report issues and feature requests on [GitHub Issues](https://github.com/testfiesta/tacotruck-jenkins-plugin/issues).

### Development Setup

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

Licensed under the MIT License. See [LICENSE](LICENSE.md) for details.
