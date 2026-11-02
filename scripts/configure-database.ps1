# Called by start.bat on the first run (Windows).
# Asks for the MySQL username and password and writes src\main\resources\db.properties.
# The file stays on this computer: it is listed in .gitignore and never uploaded to GitHub.
$ErrorActionPreference = 'Stop'
$config = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\src\main\resources\db.properties'))

Write-Host ''
Write-Host 'First run: enter the username and password you use to log in to MySQL'
Write-Host '(the same ones you use in MySQL Workbench).'
$user = Read-Host 'MySQL username (just press Enter for root)'
if ([string]::IsNullOrWhiteSpace($user)) { $user = 'root' }
$secure = Read-Host 'MySQL password (shown as *)' -AsSecureString
$bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try { $password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }

$lines = @(
    '# Created by start.bat - your local database settings (this file is not uploaded to GitHub)',
    'db.url=jdbc:mysql://localhost:3306/student_registration_db?useSSL=false&allowPublicKeyRetrieval=true',
    ('db.user=' + $user.Trim()),
    ('db.password=' + $password.Replace('\', '\\'))   # a backslash must be doubled in .properties files
)
[System.IO.File]::WriteAllLines($config, [string[]]$lines)
Write-Host 'Saved in src\main\resources\db.properties  (edit or delete this file to change the settings).'
