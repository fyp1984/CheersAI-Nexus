#==============================================================================
# CheersAI-Nexus Deploy Script (PowerShell)
# Deploy build artifacts to target server
#
# Usage: .\deploy.ps1 [-Package <path>] [-SkipBackup] [-SkipRestart] [-DryRun]
# Example:
#   .\deploy.ps1                      # Interactive deploy
#   .\deploy.ps1 -Package "nexus-uat-latest.zip"  # Use specific package
#   .\deploy.ps1 -SkipBackup         # Skip backup
#   .\deploy.ps1 -SkipRestart        # Upload only, no restart
#==============================================================================

param(
    [string]$Package = "",
    [string]$Server = "175.178.236.183",
    [string]$User = "root",
    [string]$AppDir = "/home/nexus/app",
    [switch]$SkipBackup,
    [switch]$SkipRestart,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

# Colors
function Write-Info { Write-Host "[INFO] $args" -ForegroundColor Cyan }
function Write-Success { Write-Host "[SUCCESS] $args" -ForegroundColor Green }
function Write-Warn { Write-Host "[WARN] $args" -ForegroundColor Yellow }
function Write-Err { Write-Host "[ERROR] $args" -ForegroundColor Red }

# Config
$ScriptDir = Split-Path -Parent $PSMyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$DeployOutputDir = Join-Path $ProjectRoot "deployment\output"
$BackupDir = "/home/nexus/backup"
$MaxBackupCount = 5

Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "  CheersAI-Nexus Deploy Script" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

# Find package
function Find-Package {
    param([string]$PackagePath)

    if ([string]::IsNullOrEmpty($PackagePath)) {
        $packages = Get-ChildItem -Path $DeployOutputDir -Filter "nexus-*.zip" | Sort-Object LastWriteTime -Descending
        if ($packages.Count -eq 0) {
            Write-Err "No package found. Please run build.ps1 first."
            exit 1
        }
        $PackagePath = $packages[0].FullName
    }

    if (-not (Test-Path $PackagePath)) {
        Write-Err "Package not found: $PackagePath"
        exit 1
    }

    Write-Info "Using package: $PackagePath"
    return $PackagePath
}

# Backup current
function Backup-Current {
    if ($SkipBackup) {
        Write-Info "Skipping backup"
        return
    }

    Write-Info "========== Backing up current version =========="

    $backupName = "backup-$(Get-Date -Format 'yyyyMMdd_HHmmss')"
    $backupPath = "$BackupDir/$backupName"

    if ($DryRun) {
        Write-Info "[DRY-RUN] Will create backup: $backupPath"
        return
    }

    $sshCmd = "ssh ${User}@${Server}"

    $checkCmd = "if [ -d '$AppDir/backend' ] || [ -d '$AppDir/frontend' ]; then echo 'EXISTS'; else echo 'EMPTY'; fi"
    $result = & $sshCmd $checkCmd 2>&1

    if ($result -eq "EXISTS") {
        Write-Info "Creating backup..."

        $commands = @"
mkdir -p $backupPath
if [ -d "$AppDir/backend" ]; then cp -r $AppDir/backend $backupPath/; fi
if [ -d "$AppDir/frontend" ]; then cp -r $AppDir/frontend $backupPath/; fi
if [ -d "$AppDir/config" ]; then cp -r $AppDir/config $backupPath/; fi
echo 'Backup done: $backupPath'
cd $BackupDir
ls -dt backup-* | tail -n +$($MaxBackupCount + 1) | xargs -r rm -rf
echo 'Old backups cleaned'
"@

        & $sshCmd $commands
        Write-Success "Backup completed"
    } else {
        Write-Info "App directory is empty, skipping backup"
    }
}

# Upload package
function Upload-Package {
    param([string]$PackagePath)

    Write-Info "========== Uploading package =========="

    $remoteNewZip = "$AppDir/nexus-new.zip"

    if ($DryRun) {
        Write-Info "[DRY-RUN] Will upload: $PackagePath -> ${User}@${Server}:$remoteNewZip"
        return
    }

    Write-Info "Uploading package..."
    scp -o StrictHostKeyChecking=no $PackagePath "${User}@${Server}:$remoteNewZip"

    $commands = @"
cd $AppDir
echo 'Extracting package...'
unzip -o nexus-new.zip -d . 2>/dev/null || python3 -c "import zipfile; zipfile.ZipFile('nexus-new.zip').extractall('.')"
mv nexus-new.zip nexus-current.zip
echo 'Package extracted'
ls -la $AppDir/
"@

    & ssh -o StrictHostKeyChecking=no "${User}@${Server}" $commands
    Write-Success "Upload completed"
}

# Restart services
function Restart-Services {
    if ($SkipRestart) {
        Write-Info "Skipping service restart"
        return
    }

    Write-Info "========== Restarting services =========="

    if ($DryRun) {
        Write-Info "[DRY-RUN] Will restart:"
        Write-Info "  - nexus-auth"
        Write-Info "  - nexus-user-management"
        Write-Info "  - nexus-feedback"
        Write-Info "  - nexus-product"
        Write-Info "  - nginx"
        return
    }

    $commands = @"
echo 'Stopping services...'
systemctl stop nexus-product 2>/dev/null || true
systemctl stop nexus-feedback 2>/dev/null || true
systemctl stop nexus-user-management 2>/dev/null || true
systemctl stop nexus-auth 2>/dev/null || true
echo 'Waiting for services to stop...'
sleep 3
echo 'Starting services...'
systemctl start nexus-auth
sleep 2
systemctl start nexus-user-management
sleep 2
systemctl start nexus-feedback
sleep 2
systemctl start nexus-product
sleep 2
echo 'Restarting Nginx...'
nginx -t && nginx -s reload || nginx
echo 'Checking service status...'
for svc in nexus-auth nexus-user-management nexus-feedback nexus-product; do
    if systemctl is-active --quiet \$svc; then
        echo ""[OK] \$svc is running""
    else
        echo ""[FAIL] \$svc is NOT running""
    fi
done
echo 'Deploy done!'
"@

    & ssh -o StrictHostKeyChecking=no "${User}@${Server}" $commands
    Write-Success "Service restart completed"
}

# Verify deployment
function Verify-Deployment {
    Write-Info "========== Verifying deployment =========="

    if ($DryRun) {
        Write-Info "[DRY-RUN] Skipping verification"
        return
    }

    $domain = "uat-nexus.cheersai.cloud"

    try {
        $response = Invoke-WebRequest -Uri "https://$domain/" -UseBasicParsing -TimeoutSec 10 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Success "Frontend is accessible"
        } else {
            Write-Warn "Frontend returned status: $($response.StatusCode)"
        }
    } catch {
        Write-Warn "Frontend verification failed"
    }

    Write-Info "Checking API endpoints..."
    $endpoints = @("/api/v1/auth/me", "/api/v1/users", "/api/v1/products", "/api/v1/feedbacks")
    foreach ($endpoint in $endpoints) {
        try {
            $statusCode = (Invoke-WebRequest -Uri "https://$domain$endpoint" -UseBasicParsing -TimeoutSec 5 -ErrorAction SilentlyContinue).StatusCode
            if ($statusCode -eq 401 -or $statusCode -eq 200) {
                Write-Success "[OK] $endpoint (HTTP $statusCode)"
            } else {
                Write-Warn "[WARN] $endpoint (HTTP $statusCode)"
            }
        } catch {
            Write-Warn "[FAIL] $endpoint is not accessible"
        }
    }
}

# Show summary
function Show-Summary {
    param([string]$PackagePath)

    Write-Info "========== Deploy Summary =========="
    Write-Host ""
    Write-Host "  Package:   $PackagePath" -ForegroundColor White
    Write-Host "  Server:    ${User}@${Server}" -ForegroundColor White
    Write-Host "  App Dir:   $AppDir" -ForegroundColor White
    Write-Host "  Backup:    $BackupDir" -ForegroundColor White
    Write-Host ""

    if ($DryRun) {
        Write-Host "  [DRY-RUN mode - no actual changes]" -ForegroundColor Yellow
    }

    Write-Host "========================================" -ForegroundColor Magenta
}

function Main {
    $packagePath = Find-Package -PackagePath $Package
    Show-Summary -PackagePath $packagePath

    if (-not $DryRun) {
        Write-Host ""
        $confirm = Read-Host "Confirm deploy? (y/n)"
        if ($confirm -ne "y" -and $confirm -ne "Y") {
            Write-Info "Deploy cancelled"
            exit 0
        }
    }

    Backup-Current
    Upload-Package -PackagePath $packagePath
    Restart-Services
    Verify-Deployment

    Write-Success "Deploy completed!"
}

Main
