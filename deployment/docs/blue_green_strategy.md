# Blue/Green Deployment Strategy

This document details the zero-downtime deployment mechanism implemented for our platform.

## 1. Environments Definition
- **Blue**: The active deployment answering user requests.
- **Green**: The new release deployment currently isolated from public routing.

## 2. Release Routing Strategy
1. Deploy new image tags to the Kubernetes namespace as a new Deployment structure prefixed with -green.
2. Kubernetes readiness probes monitor when the new version is ready.
3. Switch target ports inside the main ingress controller router from Blue to Green.
4. Scale down the Blue resources to 0 replica nodes once traffic is migrated.