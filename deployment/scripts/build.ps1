#==============================================================================
# CheersAI-Nexus Build Script (PowerShell)
# Build frontend and backend for deployment
#
# Usage: .\build.ps1 [-Environment <environment>]
# Example: .\build.ps1 -Environment uat
#==============================================================================

param(
    [string]$Environment = "uat"
)

$ErrorActionPreference = "Stop"

# Colors
function Write-Info { Write-Host "[INFO] $args" -ForegroundColor Cyan }
function Write-Success { Write-Host "[SUCCESS] $args" -ForegroundColor Green }
function Write-Warn { Write-Host "[WARN] $args" -ForegroundColor Yellow }
function Write-Err { Write-Host "[ERROR] $args" -ForegroundColor Red }

# Config
# Project root is 2 levels up from scripts directory
$ProjectRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$DeployDir = Join-Path $ProjectRoot "deployment"
$OutputDir = Join-Path $DeployDir "output"
$BackendDir = Join-Path $ProjectRoot "nexus-backend"
$FrontendDir = Join-Path $ProjectRoot "nexus-frontend"
$BuildTimestamp = Get-Date -Format "yyyyMMdd_HHmmss"

Write-Info "Project Root: $ProjectRoot"

Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "  CheersAI-Nexus Build Script" -ForegroundColor Magenta
Write-Host "  Environment: $Environment" -ForegroundColor Magenta
Write-Host "  Time: $BuildTimestamp" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

# Check dependencies
function Test-Dependencies {
    Write-Info "Checking dependencies..."

    # Check Java
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if (-not $javaCmd) {
        Write-Err "Java not found in PATH. Please install JDK 21 and add it to PATH."
        exit 1
    }
    try {
        $javaVersion = & $javaCmd.Source -version 2>&1 | Out-String
        Write-Info "Java: $javaVersion".Trim()
    } catch {
        Write-Info "Java found at $($javaCmd.Source)"
    }

    # Check Maven
    $mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
    if (-not $mvnCmd) {
        Write-Err "Maven not found in PATH. Please install Maven and add it to PATH."
        exit 1
    }
    try {
        $mvnVersion = & $mvnCmd.Source -version 2>&1 | Out-String
        Write-Info "Maven: $mvnVersion".Trim()
    } catch {
        Write-Info "Maven found at $($mvnCmd.Source)"
    }

    # Check Node.js
    $nodeCmd = Get-Command node -ErrorAction SilentlyContinue
    if (-not $nodeCmd) {
        Write-Err "Node.js not found in PATH. Please install Node.js."
        exit 1
    }
    $nodeVersion = & node --version
    Write-Info "Node.js: $nodeVersion"

    # Check npm
    $npmCmd = Get-Command npm -ErrorAction SilentlyContinue
    if (-not $npmCmd) {
        Write-Err "npm not found in PATH."
        exit 1
    }
    $npmVersion = & npm --version
    Write-Info "npm: $npmVersion"

    Write-Success "All dependencies OK"
}

# Clean build
function Clear-Build {
    Write-Info "Cleaning old build files..."
    if (Test-Path $OutputDir) {
        Remove-Item -Path $OutputDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path "$OutputDir\backend" -Force | Out-Null
    New-Item -ItemType Directory -Path "$OutputDir\frontend" -Force | Out-Null
    New-Item -ItemType Directory -Path "$OutputDir\config" -Force | Out-Null
    New-Item -ItemType Directory -Path "$OutputDir\systemd" -Force | Out-Null
    Write-Success "Build directory created: $OutputDir"
}

# Build backend
function Build-Backend {
    Write-Info "========== Building Backend =========="

    Push-Location $BackendDir

    try {
        Write-Info "Building common module..."
        & mvn -pl common -am clean install -DskipTests -q

        Write-Info "Building business modules (auth, user-management, feedback, product)..."
        & mvn clean package -DskipTests -q

        Write-Info "Copying JAR files to output..."

        $jars = @{
            "auth\target\auth-0.0.1-SNAPSHOT.jar" = "nexus-auth.jar"
            "user-management\target\user-management-0.0.1-SNAPSHOT.jar" = "nexus-user-management.jar"
            "feedback\target\feedback-1.0.0.jar" = "nexus-feedback.jar"
            "product\target\product-1.0.0.jar" = "nexus-product.jar"
        }

        foreach ($jar in $jars.GetEnumerator()) {
            $srcPath = Join-Path $BackendDir $jar.Key
            if (Test-Path $srcPath) {
                $dstPath = Join-Path "$OutputDir\backend" $jar.Value
                Copy-Item -Path $srcPath -Destination $dstPath -Force
                Write-Info "Copied: $($jar.Value)"
            } else {
                Write-Warn "Not found: $srcPath"
            }
        }

        Write-Success "Backend build completed"
    } finally {
        Pop-Location
    }
}

# Build frontend
function Build-Frontend {
    Write-Info "========== Building Frontend =========="

    Push-Location $FrontendDir

    try {
        Write-Info "Installing dependencies..."
        & npm install 2>&1 | Out-Null

        Write-Info "Building production version..."
        & npm run build

        Write-Info "Copying build artifacts..."
        $distPath = Join-Path $FrontendDir "dist"
        if (Test-Path $distPath) {
            $dstPath = Join-Path $OutputDir "frontend\dist"
            Copy-Item -Path $distPath -Destination $dstPath -Recurse -Force
            Write-Success "Frontend build completed"
        } else {
            Write-Err "Frontend build failed: dist directory not found"
            exit 1
        }
    } finally {
        Pop-Location
    }
}

# Prepare deployment configs
function Prepare-DeployConfigs {
    Write-Info "========== Preparing deployment configs =========="

    $deployConfigDir = Join-Path $DeployDir "config"
    $deploySystemdDir = Join-Path $DeployDir "systemd"
    Copy-Item -Path "$deployConfigDir\*" -Destination "$OutputDir\config" -Recurse -Force
    Copy-Item -Path "$deploySystemdDir\*" -Destination "$OutputDir\systemd" -Recurse -Force

    Write-Success "Deployment configs prepared"
}

# Create deploy package
function New-DeployPackage {
    Write-Info "========== Creating Deploy Package =========="

    $manifest = @"
Build Time: $BuildTimestamp
Build Version: $Environment
Build Environment: $Environment
Build Host: $env:COMPUTERNAME
Build User: $env:USERNAME

Services:
- nexus-auth.jar      (port: 8082, API: /api/v1/auth)
- nexus-user-management.jar  (port: 8083, API: /api/v1/users)
- nexus-feedback.jar  (port: 8084, API: /api/v1/feedbacks)
- nexus-product.jar    (port: 8085, API: /api/v1/products)

Frontend: dist/
Deployment Configs: config/, systemd/
"@
    $manifestPath = Join-Path $OutputDir "deploy-manifest.txt"
    Set-Content -Path $manifestPath -Value $manifest -Encoding UTF8

    $packageName = "nexus-${Environment}-${BuildTimestamp}"
    $zipPath = Join-Path $DeployDir "$packageName.zip"

    Write-Info "Creating package: $packageName.zip"

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::Open($zipPath, 'Create')

    try {
        $backendPath = Join-Path $OutputDir "backend"
        if (Test-Path $backendPath) {
            Get-ChildItem -Path $backendPath -File | ForEach-Object {
                [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, "backend/$($_.Name)", [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
            }
        }

        $frontendPath = Join-Path $OutputDir "frontend"
        if (Test-Path $frontendPath) {
            Get-ChildItem -Path $frontendPath -Recurse -File | ForEach-Object {
                $relativePath = $_.FullName.Substring($frontendPath.Length + 1)
                [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, "frontend/$relativePath", [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
            }
        }

        $configPath = Join-Path $OutputDir "config"
        if (Test-Path $configPath) {
            Get-ChildItem -Path $configPath -Recurse -Force -File | ForEach-Object {
                $relativePath = $_.FullName.Substring($configPath.Length + 1)
                [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, "config/$relativePath", [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
            }
        }

        $systemdPath = Join-Path $OutputDir "systemd"
        if (Test-Path $systemdPath) {
            Get-ChildItem -Path $systemdPath -Recurse -Force -File | ForEach-Object {
                $relativePath = $_.FullName.Substring($systemdPath.Length + 1)
                [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, "systemd/$relativePath", [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
            }
        }

        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $manifestPath, "deploy-manifest.txt", [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null

    } finally {
        $zip.Dispose()
    }

    $md5Hash = Get-FileHash -Path $zipPath -Algorithm MD5
    $md5Path = "$zipPath.md5"
    Set-Content -Path $md5Path -Value "$($md5Hash.Hash)  $packageName.zip" -Encoding ASCII

    $latestMarker = Join-Path $DeployDir "output\latest.txt"
    Set-Content -Path $latestMarker -Value "$packageName.zip" -Encoding UTF8

    Write-Success "Package created: $zipPath"
    Write-Info "Size: $([math]::Round((Get-Item $zipPath).Length / 1MB, 2)) MB"
    Write-Info "MD5: $($md5Hash.Hash)"
}

# Show summary
function Show-Summary {
    Write-Info "========== Build Complete =========="
    Write-Host ""
    Write-Host "Output: $OutputDir" -ForegroundColor White
    Write-Host "Package: $(Join-Path $DeployDir "nexus-$Environment-$BuildTimestamp.zip")" -ForegroundColor White
    Write-Host ""
    Write-Host "Backend JARs:" -ForegroundColor White
    Get-ChildItem -Path "$OutputDir\backend\*.jar" | ForEach-Object {
        Write-Host "  - $($_.Name) ($([math]::Round($_.Length / 1MB, 2)) MB)" -ForegroundColor Gray
    }
    Write-Host ""
    Write-Host "Frontend:" -ForegroundColor White
    Write-Host "  - $OutputDir\frontend\dist\" -ForegroundColor Gray
    Write-Host ""
}

function Main {
    Test-Dependencies
    Clear-Build
    Build-Backend
    Build-Frontend
    Prepare-DeployConfigs
    New-DeployPackage
    Show-Summary

    Write-Success "Build completed!"
}

Main
