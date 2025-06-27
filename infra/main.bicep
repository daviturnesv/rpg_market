@minLength(1)
@maxLength(64)
@description('Name of the environment that can be used as part of naming resource convention')
param environmentName string

@minLength(1)
@description('Primary location for all resources')
param location string

@description('Name of the resource group')
param resourceGroupName string = ''

@description('Connection string for MongoDB database')
param mongoDbConnectionString string = ''

@description('MongoDB database name')
param mongoDbDatabaseName string = 'Cluster0'

@description('Server port for the application')
param serverPort string = '8080'

@description('Spring application name')
param springApplicationName string = 'rpg_market'

// Generate unique resource token based on environment name, resource group and subscription
var resourceToken = toLower(uniqueString(subscription().id, resourceGroup().id, environmentName))

// Construct resource names using naming convention
var appServicePlanName = 'asp-${resourceToken}'
var appServiceName = 'app-${resourceToken}'
var cosmosDbAccountName = 'cosmos-${resourceToken}'
var keyVaultName = 'kv-${resourceToken}'
var logAnalyticsWorkspaceName = 'log-${resourceToken}'
var applicationInsightsName = 'appi-${resourceToken}'
var managedIdentityName = 'id-${resourceToken}'

// Tags for all resources
var tags = {
  'azd-env-name': environmentName
  'azd-service-name': 'rpg-market'
}

// Create User Assigned Managed Identity
resource managedIdentity 'Microsoft.ManagedIdentity/userAssignedIdentities@2023-01-31' = {
  name: managedIdentityName
  location: location
  tags: tags
}

// Create Log Analytics Workspace
resource logAnalyticsWorkspace 'Microsoft.OperationalInsights/workspaces@2022-10-01' = {
  name: logAnalyticsWorkspaceName
  location: location
  tags: tags
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: 30
    features: {
      searchVersion: 1
      legacy: 0
      enableLogAccessUsingOnlyResourcePermissions: true
    }
  }
}

// Create Application Insights
resource applicationInsights 'Microsoft.Insights/components@2020-02-02' = {
  name: applicationInsightsName
  location: location
  tags: tags
  kind: 'web'
  properties: {
    Application_Type: 'web'
    WorkspaceResourceId: logAnalyticsWorkspace.id
    IngestionMode: 'LogAnalytics'
    publicNetworkAccessForIngestion: 'Enabled'
    publicNetworkAccessForQuery: 'Enabled'
  }
}

// Create Key Vault for storing connection strings
resource keyVault 'Microsoft.KeyVault/vaults@2023-02-01' = {
  name: keyVaultName
  location: location
  tags: tags
  properties: {
    sku: {
      family: 'A'
      name: 'standard'
    }
    tenantId: subscription().tenantId
    enabledForTemplateDeployment: true
    enableRbacAuthorization: true
    publicNetworkAccess: 'Enabled'
    accessPolicies: []
  }
}

// Grant Key Vault Secrets Officer role to Managed Identity
resource keyVaultSecretsUserRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  scope: keyVault
  name: guid(keyVault.id, managedIdentity.id, 'Key Vault Secrets User')
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '4633458b-17de-408a-b874-0445c86b69e6') // Key Vault Secrets User
    principalId: managedIdentity.properties.principalId
    principalType: 'ServicePrincipal'
  }
}

// Create Cosmos DB Account for MongoDB
resource cosmosDbAccount 'Microsoft.DocumentDB/databaseAccounts@2024-11-15' = {
  name: cosmosDbAccountName
  location: location
  tags: tags
  kind: 'MongoDB'
  identity: {
    type: 'UserAssigned'
    userAssignedIdentities: {
      '${managedIdentity.id}': {}
    }
  }
  properties: {
    databaseAccountOfferType: 'Standard'
    enableFreeTier: true
    capabilities: [
      {
        name: 'EnableMongo'
      }
    ]
    apiProperties: {
      serverVersion: '4.2'
    }
    locations: [
      {
        locationName: location
        failoverPriority: 0
        isZoneRedundant: false
      }
    ]
    backupPolicy: {
      type: 'Periodic'
      periodicModeProperties: {
        backupIntervalInMinutes: 240
        backupRetentionIntervalInHours: 8
        backupStorageRedundancy: 'Local'
      }
    }
    consistencyPolicy: {
      defaultConsistencyLevel: 'Session'
    }
    networkAclBypass: 'AzureServices'
    publicNetworkAccess: 'Enabled'
    disableLocalAuth: false
  }
}

// Store MongoDB connection string in Key Vault
resource mongoDbConnectionStringSecret 'Microsoft.KeyVault/vaults/secrets@2023-02-01' = {
  parent: keyVault
  name: 'mongodb-connection-string'
  dependsOn: [keyVaultSecretsUserRole]
  properties: {
    value: empty(mongoDbConnectionString) ? cosmosDbAccount.listConnectionStrings().connectionStrings[0].connectionString : mongoDbConnectionString
  }
}

// Store MongoDB database name in Key Vault
resource mongoDbDatabaseNameSecret 'Microsoft.KeyVault/vaults/secrets@2023-02-01' = {
  parent: keyVault
  name: 'mongodb-database-name'
  dependsOn: [keyVaultSecretsUserRole]
  properties: {
    value: mongoDbDatabaseName
  }
}

// Create App Service Plan
resource appServicePlan 'Microsoft.Web/serverfarms@2024-04-01' = {
  name: appServicePlanName
  location: location
  tags: tags
  sku: {
    name: 'B1'
    tier: 'Basic'
  }
  properties: {
    reserved: false // Windows
  }
}

// Create App Service
resource appService 'Microsoft.Web/sites@2024-04-01' = {
  name: appServiceName
  location: location
  tags: tags
  identity: {
    type: 'UserAssigned'
    userAssignedIdentities: {
      '${managedIdentity.id}': {}
    }
  }
  properties: {
    serverFarmId: appServicePlan.id
    httpsOnly: true
    keyVaultReferenceIdentity: managedIdentity.id
    siteConfig: {
      javaVersion: '17'
      javaContainer: 'TOMCAT'
      javaContainerVersion: '10.1'
      appSettings: [
        {
          name: 'SPRING_DATA_MONGODB_URI'
          value: '@Microsoft.KeyVault(VaultName=${keyVault.name};SecretName=mongodb-connection-string)'
        }
        {
          name: 'SPRING_DATA_MONGODB_DATABASE'
          value: '@Microsoft.KeyVault(VaultName=${keyVault.name};SecretName=mongodb-database-name)'
        }
        {
          name: 'SERVER_PORT'
          value: serverPort
        }
        {
          name: 'SPRING_APPLICATION_NAME'
          value: springApplicationName
        }
        {
          name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
          value: applicationInsights.properties.ConnectionString
        }
        {
          name: 'ApplicationInsightsAgent_EXTENSION_VERSION'
          value: '~3'
        }
        {
          name: 'APPINSIGHTS_PROFILERFEATURE_VERSION'
          value: '1.0.0'
        }
        {
          name: 'APPINSIGHTS_SNAPSHOTFEATURE_VERSION'
          value: '1.0.0'
        }
      ]
      cors: {
        allowedOrigins: ['*']
        supportCredentials: false
      }
      healthCheckPath: '/actuator/health'
    }
  }
}

// Configure diagnostic settings for App Service
resource appServiceDiagnosticSettings 'Microsoft.Insights/diagnosticSettings@2021-05-01-preview' = {
  scope: appService
  name: 'appservice-diagnostics'
  properties: {
    workspaceId: logAnalyticsWorkspace.id
    logs: [
      {
        category: 'AppServiceHTTPLogs'
        enabled: true
      }
      {
        category: 'AppServiceConsoleLogs'
        enabled: true
      }
      {
        category: 'AppServiceAppLogs'
        enabled: true
      }
    ]
    metrics: [
      {
        category: 'AllMetrics'
        enabled: true
      }
    ]
  }
}

// App Service Site Extension for Application Insights
resource appServiceSiteExtension 'Microsoft.Web/sites/siteextensions@2024-04-01' = {
  parent: appService
  name: 'Microsoft.ApplicationInsights.AzureWebSites'
}

// Output important values
output RESOURCE_GROUP_ID string = resourceGroup().id
output AZURE_COSMOS_CONNECTION_STRING_KEY string = 'mongodb-connection-string'
output AZURE_COSMOS_DATABASE_NAME string = mongoDbDatabaseName
output APPLICATIONINSIGHTS_CONNECTION_STRING string = applicationInsights.properties.ConnectionString
output AZURE_KEY_VAULT_ENDPOINT string = keyVault.properties.vaultUri
output SERVICE_WEB_URI string = 'https://${appService.properties.defaultHostName}'
output SERVICE_WEB_NAME string = appService.name
