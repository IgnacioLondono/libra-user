Write-Host "Ejecutando Unit Tests locales..." -ForegroundColor Cyan

# Ejecuta las pruebas con Gradle
.\gradlew testDebugUnitTest

# Si todo se ejecuta correctamente, abre el reporte HTML
if ($LASTEXITCODE -eq 0) {
    $reportPath = "app\build\reports\tests\testDebugUnitTest\index.html"
    
    if (Test-Path $reportPath) {
        Write-Host "Pruebas completadas. Abriendo reporte en el navegador..." -ForegroundColor Green
        Start-Process $reportPath
    }
    else {
        Write-Host "No se encontró el archivo de reporte en: $reportPath" -ForegroundColor Yellow
    }
}
else {
    Write-Host "Ocurrió un error al ejecutar los tests." -ForegroundColor Red
}

