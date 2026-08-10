# AI-QA-OS — Service-to-service mTLS (SEC-6 / FI-SEC6-B, ADR-091)

Mutual TLS on the one call this platform makes between its own services: the dashboard's AI-2
approve/reject proxy → the gateway (`ReviewController` → `POST /api/v1/workflows/{id}/{action}`).
Everything else that speaks HTTP is outbound to external systems (LLM providers, S3/MinIO), which is
a different problem with different trust anchors.

## What is actually verified

The **payload** — the TLS behaviour itself — was drilled natively with two JVMs on localhost
(ADR-091), following ENT-5/ADR-090's rule of testing the thing rather than waiting for the cluster:

| Check | Result |
|---|---|
| Gateway starts with `client-auth: need` | Tomcat on `8443 (https)` |
| Client with **no** certificate | fatal TLS alert — rejected at transport (server trust ruled out with `curl -k`) |
| Dashboard **with** its client certificate | request delivered; the gateway answered at the application layer |
| Named-but-missing SSL bundle | startup fails rather than silently downgrading (`GatewayClientConfigTest`) |

**Not verified:** everything on this page below — Secret projection, volume mounts, cert rotation and
renewal, and any service-mesh alternative. Those need a cluster.

## Enabling it

Both apps opt in via the **`mtls` Spring profile**; without it nothing changes and the plaintext
client/port behave exactly as before.

| Setting | Gateway | Dashboard |
|---|---|---|
| Profile | `mtls` | `mtls` |
| `AIQAOS_MTLS_DIR` | directory holding the mounted `.p12` files | same |
| `AIQAOS_MTLS_PASSWORD` | from `aiqaos-mtls-secret` | same |
| Port | `AIQAOS_MTLS_PORT` (default `8443`, https) | n/a |
| Gateway URL | n/a | `AIQAOS_GATEWAY_BASE_URL=https://<gateway-service>:8443` |

The gateway listens on a **separate port** rather than converting `8082`, so an mTLS listener is
never silently swapped in during a rollout and both can run side by side while clients migrate.

## Deployment wiring

Add to `deployment/kubernetes/{gateway,dashboard}/deployment.yaml`:

```yaml
      volumes:
        - name: mtls
          secret:
            secretName: aiqaos-mtls-secret
      containers:
        - name: <gateway|dashboard>
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "compose,mtls"
            - name: AIQAOS_MTLS_DIR
              value: /etc/aiqaos/mtls
            - name: AIQAOS_MTLS_PASSWORD
              valueFrom:
                secretKeyRef: { name: aiqaos-mtls-secret, key: AIQAOS_MTLS_PASSWORD }
          volumeMounts:
            - name: mtls
              mountPath: /etc/aiqaos/mtls
              readOnly: true
```

The gateway's `Service` must also expose `8443`, and the dashboard needs
`AIQAOS_GATEWAY_BASE_URL=https://<gateway-service>:8443`.

## Certificates

Issue from your own CA in any real environment. The server certificate's **SAN must match the
gateway's in-cluster hostname** — a CN-only certificate is rejected by modern clients, and this is
the most common way a working local setup fails in the cluster.

Local development only:

```
powershell -File deployment/local/generate-mtls-certs.ps1
```

Output goes to `scratch/mtls/` (gitignored): a throwaway CA, a gateway server identity
(`CN=localhost`, SAN `localhost`/`127.0.0.1`), a dashboard client identity, and a truststore per side
containing only that CA.

## Rotation

Not implemented. Spring's SSL bundles support reloading, but nothing here watches the mounted Secret,
so a rotated certificate currently requires a pod restart. Worth addressing before this is relied on
in production.
