# SEC-6 / FI-SEC6-B: generate a local CA + gateway server cert + dashboard client cert for mTLS.
#
# For LOCAL DEVELOPMENT AND DRILLS ONLY. The generated keystores are secrets and must never be
# committed (SEC-2) — the default output directory is scratch/, which is gitignored. In a cluster
# these come from the `aiqaos-mtls-secret` Secret (deployment/kubernetes/mtls/), not from this script.
#
#   powershell -File deployment/local/generate-mtls-certs.ps1
#   powershell -File deployment/local/generate-mtls-certs.ps1 -OutDir C:\some\dir -Password changeit
#
# Produces, in $OutDir:
#   ca.p12                  the local CA (keep; needed to issue more certs)
#   gateway-keystore.p12    gateway's server identity   (CN=localhost, SAN=localhost/127.0.0.1)
#   gateway-truststore.p12  the CA — who the gateway will accept client certs from
#   dashboard-keystore.p12  dashboard's client identity (CN=ai-qa-os-dashboard)
#   dashboard-truststore.p12 the CA — who the dashboard will accept server certs from

param(
    [string]$OutDir = (Join-Path $PSScriptRoot "..\..\scratch\mtls"),
    [string]$Password = "changeit"
)

$ErrorActionPreference = "Stop"
$keytool = Join-Path $env:JAVA_HOME "bin\keytool.exe"
if (-not (Test-Path $keytool)) { $keytool = (Get-Command keytool -ErrorAction Stop).Source }

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$OutDir = (Resolve-Path $OutDir).Path
Write-Host "Generating mTLS material in $OutDir"

Remove-Item (Join-Path $OutDir "*.p12"), (Join-Path $OutDir "*.crt") -ErrorAction SilentlyContinue

# 1. A local CA. Both sides trust this and nothing else, which is what makes the negative test
#    meaningful: a client without a CA-issued cert cannot complete the handshake.
& $keytool -genkeypair -alias ca -dname "CN=AI-QA-OS Local CA,OU=SEC-6,O=AI-QA-OS" `
    -keyalg RSA -keysize 2048 -validity 365 -ext "bc:c=ca:true" `
    -keystore (Join-Path $OutDir "ca.p12") -storetype PKCS12 -storepass $Password -keypass $Password
& $keytool -exportcert -alias ca -keystore (Join-Path $OutDir "ca.p12") -storepass $Password `
    -rfc -file (Join-Path $OutDir "ca.crt")

function New-SignedIdentity {
    param([string]$Alias, [string]$Dname, [string]$Store, [string]$Ext)
    $ks = Join-Path $OutDir $Store
    & $keytool -genkeypair -alias $Alias -dname $Dname -keyalg RSA -keysize 2048 -validity 365 `
        -keystore $ks -storetype PKCS12 -storepass $Password -keypass $Password
    $csr = Join-Path $OutDir "$Alias.csr"
    $crt = Join-Path $OutDir "$Alias.crt"
    & $keytool -certreq -alias $Alias -keystore $ks -storepass $Password -file $csr
    $signArgs = @("-gencert", "-alias", "ca", "-keystore", (Join-Path $OutDir "ca.p12"),
                  "-storepass", $Password, "-infile", $csr, "-outfile", $crt, "-validity", "365", "-rfc")
    if ($Ext) { $signArgs += @("-ext", $Ext) }
    & $keytool @signArgs
    # Import the CA first so the signed reply chains correctly.
    & $keytool -importcert -noprompt -alias ca -file (Join-Path $OutDir "ca.crt") `
        -keystore $ks -storepass $Password
    & $keytool -importcert -noprompt -alias $Alias -file $crt -keystore $ks -storepass $Password
    Remove-Item $csr, $crt -ErrorAction SilentlyContinue
}

# 2. Gateway server identity. SAN matters — without it the client rejects the hostname.
New-SignedIdentity -Alias "gateway" -Dname "CN=localhost,OU=SEC-6,O=AI-QA-OS" `
    -Store "gateway-keystore.p12" -Ext "san=dns:localhost,ip:127.0.0.1"

# 3. Dashboard client identity.
New-SignedIdentity -Alias "dashboard" -Dname "CN=ai-qa-os-dashboard,OU=SEC-6,O=AI-QA-OS" `
    -Store "dashboard-keystore.p12" -Ext "eku:c=clientAuth"

# 4. Truststores: each side trusts only the CA.
foreach ($ts in @("gateway-truststore.p12", "dashboard-truststore.p12")) {
    & $keytool -importcert -noprompt -alias ca -file (Join-Path $OutDir "ca.crt") `
        -keystore (Join-Path $OutDir $ts) -storetype PKCS12 -storepass $Password
}

Remove-Item (Join-Path $OutDir "ca.crt") -ErrorAction SilentlyContinue
Write-Host "`nDone. Files:"
Get-ChildItem $OutDir -Filter *.p12 | ForEach-Object { Write-Host ("  {0,-26} {1,6} bytes" -f $_.Name, $_.Length) }
Write-Host "`nRun both apps with the 'mtls' profile and AIQAOS_MTLS_DIR=$OutDir"
