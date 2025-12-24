rootProject.name = "security-for-microservices"

// Shared modules
include("shared-kernel")
include("security-infrastructure")

// Core microservices
include("customer-service")
include("product-service")
include("order-service")
include("payment-service")
include("logistics-service")
include("sales-service")

// Gateway and Admin
include("api-gateway")
include("admin-portal:backend")

// Enable type-safe project accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
